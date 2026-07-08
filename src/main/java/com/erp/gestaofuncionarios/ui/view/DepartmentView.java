/*
 * MIT License
 * 
 * Copyright (c) 2026 - Jeff Oliveira
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.erp.gestaofuncionarios.ui.view;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.erp.gestaofuncionarios.dao.DepartmentDao;
import com.erp.gestaofuncionarios.dao.EmployeeDao;
import com.erp.gestaofuncionarios.model.Department;
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.ui.util.UiHelper;
import com.erp.gestaofuncionarios.ui.util.UiIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DepartmentView {

    private final DepartmentDao dao = new DepartmentDao();
    private final EmployeeDao empDao = new EmployeeDao();
    private TableView<Department> table;

    private StackPane container;
    private VBox listView;
    private VBox detailsView;

    private Label lblDetailsTitle;
    private Label lblDetailsDesc;
    private Label lblDetailsCount;
    private TableView<Employee> tvEmployees;

    public Node build() {
        container = new StackPane();

        listView = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(2,
                labeled("Departamentos", "section-title"),
                labeled("Gerenciamento de departamentos da empresa", "section-subtitle"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNew = accentBtn("Novo Departamento", UiIcon.PLUS);
        btnNew.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titleBlock, spacer, btnNew);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nenhum departamento cadastrado."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Department, String> colCode = col("Código", 80, d -> d.getCode());
        UiHelper.makeColumnBold(colCode);

        TableColumn<Department, String> colName = col("Nome", 200, d -> d.getName());
        UiHelper.makeColumnBold(colName);

        TableColumn<Department, String> colDesc = col("Descrição", 300,
                d -> d.getDescription() != null ? d.getDescription() : "—");

        TableColumn<Department, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button edit = iconBtn(UiIcon.EDIT, "action-button", "-color-fg-default");
            private final Button del = iconBtn(UiIcon.TRASH, "action-button-danger", "#bf616a");
            {
                edit.setOnAction(e -> {
                    Department d = getTableRow().getItem();
                    if (d != null)
                        showForm(d);
                });
                del.setOnAction(e -> {
                    Department d = getTableRow().getItem();
                    if (d != null)
                        confirmDelete(d);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, edit, del);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        table.getColumns().addAll(Arrays.asList(colCode, colName, colDesc, colActions));
        refresh();

        table.setRowFactory(tv -> {
            TableRow<Department> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showDetails(row.getItem());
                }
            });
            return row;
        });

        listView.getChildren().addAll(header, table);

        detailsView = buildDetailsPanel();

        container.getChildren().setAll(listView);
        return container;
    }

    private VBox buildDetailsPanel() {
        VBox pane = new VBox(16);
        pane.setFillWidth(true);

        Button btnBack = new Button("Voltar para Departamentos");
        btnBack.getStyleClass().addAll("flat");
        btnBack.setGraphic(UiIcon.get(UiIcon.ARROW_LEFT, 14, "-color-accent-emphasis"));
        btnBack.setStyle(
                "-fx-text-fill: -color-accent-emphasis; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0;");
        btnBack.setOnAction(e -> {
            container.getChildren().setAll(listView);
        });

        HBox titleContainer = new HBox(12);
        titleContainer.setAlignment(Pos.CENTER_LEFT);

        lblDetailsTitle = new Label();
        lblDetailsTitle.getStyleClass().add("section-title");

        lblDetailsCount = new Label();
        lblDetailsCount
                .setStyle("-fx-font-size: 12px; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-weight: bold;");

        titleContainer.getChildren().addAll(lblDetailsTitle, lblDetailsCount);

        lblDetailsDesc = new Label();
        lblDetailsDesc.getStyleClass().add("section-subtitle");
        lblDetailsDesc.setWrapText(true);

        VBox headerBlock = new VBox(4, btnBack, titleContainer, lblDetailsDesc);
        headerBlock.setStyle("-fx-padding: 0 0 8 0;");

        tvEmployees = new TableView<>();
        tvEmployees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tvEmployees.setPlaceholder(new Label("Nenhum colaborador cadastrado neste departamento."));
        VBox.setVgrow(tvEmployees, Priority.ALWAYS);

        TableColumn<Employee, Void> colEmpName = new TableColumn<>("Colaborador");
        colEmpName.setPrefWidth(220);
        colEmpName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Employee e = getTableRow().getItem();
                setGraphic(UiHelper.buildEmployeeNameCell(e));
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        TableColumn<Employee, String> colEmpPos = new TableColumn<>("Cargo");
        colEmpPos.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPosition() != null ? d.getValue().getPosition().getName() : "—"));
        colEmpPos.setPrefWidth(180);

        TableColumn<Employee, Void> colEmpStatus = new TableColumn<>("Status");
        colEmpStatus.setPrefWidth(100);
        colEmpStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Employee e = getTableRow().getItem();
                setGraphic(UiHelper.buildStatusBadge(e.getStatus()));
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        tvEmployees.getColumns().addAll(Arrays.asList(colEmpName, colEmpPos, colEmpStatus));

        pane.getChildren().addAll(headerBlock, tvEmployees);
        return pane;
    }

    private void showDetails(Department dept) {
        if (dept == null)
            return;

        lblDetailsTitle.setText("[" + dept.getCode() + "] " + dept.getName());
        lblDetailsDesc.setText(dept.getDescription() != null ? dept.getDescription() : "Sem descrição cadastrada.");

        List<Employee> list;
        try {
            list = empDao.search(null, dept.getId(), null, null);
        } catch (Exception e) {
            list = List.of();
        }

        lblDetailsCount.setText(list.size() + (list.size() == 1 ? " colaborador" : " colaboradores"));
        if (list.isEmpty()) {
            lblDetailsCount.setStyle(
                    "-fx-font-size: 12px; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-weight: bold; -fx-background-color: -color-bg-subtle; -fx-text-fill: -color-fg-muted;");
        } else {
            lblDetailsCount.setStyle(
                    "-fx-font-size: 12px; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-weight: bold; -fx-background-color: -color-accent-subtle; -fx-text-fill: -color-accent-emphasis;");
        }

        tvEmployees.setItems(FXCollections.observableArrayList(list));

        container.getChildren().setAll(detailsView);
    }

    private void showForm(Department dept) {
        Stage formStage = new Stage();
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-min-width: 400px; -fx-min-height: 250px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);

        TextField tfCode = new TextField(dept != null ? dept.getCode() : "");
        tfCode.setMaxWidth(Double.MAX_VALUE);
        TextField tfName = new TextField(dept != null ? dept.getName() : "");
        tfName.setMaxWidth(Double.MAX_VALUE);
        TextField tfDesc = new TextField(dept != null && dept.getDescription() != null ? dept.getDescription() : "");
        tfDesc.setMaxWidth(Double.MAX_VALUE);

        form.addRow(0, new Label("Código *"), tfCode);
        form.addRow(1, new Label("Nome *"), tfName);
        form.addRow(2, new Label("Descrição"), tfDesc);

        ColumnConstraints c1 = new ColumnConstraints(100);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        Button btnSave = new Button("Salvar");
        btnSave.getStyleClass().add("accent");
        btnSave.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {

            if (tfCode.getText().isBlank() || tfName.getText().isBlank()) {
                showError("Por favor, preencha os campos obrigatórios.");
                return;
            }

            Department d = dept != null ? dept : new Department();
            d.setCode(tfCode.getText().trim());
            d.setName(tfName.getText().trim());
            d.setDescription(tfDesc.getText().trim());
            try {
                if (dept == null)
                    dao.save(d);
                else
                    dao.update(d);
                refresh();
                formStage.close();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().add("flat");
        btnCancel.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> formStage.close());

        HBox footer = new HBox(10, btnCancel, btnSave);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(form, footer);

        Stage owner = (Stage) container.getScene().getWindow();
        UiHelper.prepareCustomWindow(
            formStage,
            dept == null ? "Novo Departamento" : "Editar Departamento",
            root,
            false,
            false,
            javafx.stage.Modality.APPLICATION_MODAL,
            owner
        );

        formStage.showAndWait();
    }

    private void confirmDelete(Department dept) {
        Stage owner = (Stage) container.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Exclusão", "Remover o departamento " + dept.getName() + "?", () -> {
            dao.delete(dept);
            refresh();
        });
    }

    private void refresh() {
        try {
            table.setItems(FXCollections.observableArrayList(dao.findAll()));
        } catch (Exception e) {
            table.setItems(FXCollections.emptyObservableList());
        }
    }

    private TableColumn<Department, String> col(String title, int w,
            Function<Department, String> fn) {
        TableColumn<Department, String> c = new TableColumn<>(title);
        c.setPrefWidth(w);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }

    private Button accentBtn(String label, String iconData) {
        StackPane fi = UiIcon.get(iconData, 13, "#ffffff");
        Button btn = new Button(label, fi);
        btn.getStyleClass().addAll("button", "accent");
        return btn;
    }

    private Button iconBtn(String iconData, String extraClass, String colorWeb) {
        StackPane fi = UiIcon.get(iconData, 13, colorWeb);
        Button btn = new Button();
        btn.setGraphic(fi);
        btn.getStyleClass().addAll(extraClass);
        return btn;
    }

    private Label labeled(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private void showError(String msg) {
        Stage owner = (Stage) container.getScene().getWindow();
        UiHelper.showError(owner, msg);
    }
}
