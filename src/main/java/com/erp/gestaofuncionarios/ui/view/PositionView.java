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

import com.erp.gestaofuncionarios.dao.PositionDao;
import com.erp.gestaofuncionarios.dao.EmployeeDao;
import com.erp.gestaofuncionarios.model.Position;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class PositionView {

    private final PositionDao dao = new PositionDao();
    private final EmployeeDao empDao = new EmployeeDao();
    private TableView<Position> table;

    private StackPane container;
    private VBox listView;
    private VBox detailsView;

    private Label lblDetailsTitle;
    private Label lblDetailsDesc;
    private Label lblDetailsSalary;
    private Label lblDetailsCount;
    private TableView<Employee> tvEmployees;

    public Node build() {
        container = new StackPane();

        listView = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(2,
                label("Cargos & Funções", "section-title"),
                label("Estrutura de cargos e faixas salariais", "section-subtitle"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNew = new Button("Novo Cargo");
        btnNew.getStyleClass().addAll("button", "accent");

        StackPane plusIcon = UiIcon.get(UiIcon.PLUS, 13, "#ffffff");
        btnNew.setGraphic(plusIcon);
        btnNew.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titleBlock, spacer, btnNew);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nenhum cargo cadastrado."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Position, String> colName = col("Cargo", 220, p -> p.getName());
        UiHelper.makeColumnBold(colName);

        TableColumn<Position, String> colDesc = col("Descrição", 280,
                p -> p.getDescription() != null ? p.getDescription() : "—");

        TableColumn<Position, String> colSal = col("Salário Base", 130,
                p -> p.getBaseSalary() != null ? "R$ " + String.format("%,.2f", p.getBaseSalary()).replace(",", "X")
                        .replace(".", ",").replace("X", ".") : "—");

        TableColumn<Position, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button edit = btn(UiIcon.EDIT, "action-button", "-color-fg-default");
            private final Button del = btn(UiIcon.TRASH, "action-button-danger", "#bf616a");
            {
                edit.setOnAction(e -> {
                    Position p = getTableRow().getItem();
                    if (p != null)
                        showForm(p);
                });
                del.setOnAction(e -> {
                    Position p = getTableRow().getItem();
                    if (p != null)
                        confirmDelete(p);
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

        table.getColumns().addAll(Arrays.asList(colName, colDesc, colSal, colActions));
        refresh();

        table.setRowFactory(tv -> {
            TableRow<Position> row = new TableRow<>();
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

        Button btnBack = new Button("Voltar para Cargos");
        btnBack.getStyleClass().addAll("flat");
        btnBack.setGraphic(UiIcon.get(UiIcon.ARROW_LEFT, 14, "-color-accent-emphasis"));
        btnBack.setStyle("-fx-text-fill: -color-accent-emphasis; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0;");
        btnBack.setOnAction(e -> {
            container.getChildren().setAll(listView);
        });

        HBox titleContainer = new HBox(12);
        titleContainer.setAlignment(Pos.CENTER_LEFT);

        lblDetailsTitle = new Label();
        lblDetailsTitle.getStyleClass().add("section-title");

        lblDetailsCount = new Label();
        lblDetailsCount.setStyle("-fx-font-size: 12px; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-weight: bold;");

        titleContainer.getChildren().addAll(lblDetailsTitle, lblDetailsCount);

        lblDetailsDesc = new Label();
        lblDetailsDesc.getStyleClass().add("section-subtitle");
        lblDetailsDesc.setWrapText(true);

        lblDetailsSalary = new Label();
        lblDetailsSalary.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        VBox headerBlock = new VBox(6, btnBack, titleContainer, lblDetailsDesc, lblDetailsSalary);
        headerBlock.setStyle("-fx-padding: 0 0 8 0;");

        tvEmployees = new TableView<>();
        tvEmployees.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tvEmployees.setPlaceholder(new Label("Nenhum colaborador cadastrado neste cargo."));
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

        TableColumn<Employee, String> colEmpDept = new TableColumn<>("Departamento");
        colEmpDept.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDepartment() != null ? d.getValue().getDepartment().getName() : "—"));
        colEmpDept.setPrefWidth(180);

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

        tvEmployees.getColumns().addAll(Arrays.asList(colEmpName, colEmpDept, colEmpStatus));

        pane.getChildren().addAll(headerBlock, tvEmployees);
        return pane;
    }

    private void showDetails(Position pos) {
        if (pos == null) return;
        lblDetailsTitle.setText(pos.getName());
        lblDetailsDesc.setText(pos.getDescription() != null ? pos.getDescription() : "Sem descrição cadastrada.");
        lblDetailsSalary.setText("Salário Base: R$ " + (pos.getBaseSalary() != null ? String.format("%,.2f", pos.getBaseSalary()).replace(",", "X").replace(".", ",").replace("X", ".") : "—"));

        List<Employee> list;
        try {
            list = empDao.search(null, null, pos.getId(), null);
        } catch (Exception e) {
            list = List.of();
        }

        lblDetailsCount.setText(list.size() + (list.size() == 1 ? " colaborador" : " colaboradores"));
        if (list.isEmpty()) {
            lblDetailsCount.setStyle("-fx-font-size: 12px; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-weight: bold; -fx-background-color: -color-bg-subtle; -fx-text-fill: -color-fg-muted;");
        } else {
            lblDetailsCount.setStyle("-fx-font-size: 12px; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-weight: bold; -fx-background-color: -color-accent-subtle; -fx-text-fill: -color-accent-emphasis;");
        }

        tvEmployees.setItems(FXCollections.observableArrayList(list));

        container.getChildren().setAll(detailsView);
    }

    private void showForm(Position pos) {
        Stage formStage = new Stage();
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-min-width: 400px; -fx-min-height: 250px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(120);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        TextField tfName = new TextField(pos != null ? pos.getName() : "");
        tfName.setMaxWidth(Double.MAX_VALUE);
        TextField tfDesc = new TextField(pos != null && pos.getDescription() != null ? pos.getDescription() : "");
        tfDesc.setMaxWidth(Double.MAX_VALUE);
        TextField tfSal = new TextField(
                pos != null && pos.getBaseSalary() != null ? pos.getBaseSalary().toPlainString() : "");
        tfSal.setMaxWidth(Double.MAX_VALUE);

        form.addRow(0, new Label("Nome *"), tfName);
        form.addRow(1, new Label("Descrição"), tfDesc);
        form.addRow(2, new Label("Salário Base"), tfSal);

        Button btnSave = new Button("Salvar");
        btnSave.getStyleClass().add("accent");
        btnSave.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            if (tfName.getText().isBlank()) {
                Stage owner = (Stage) container.getScene().getWindow();
                UiHelper.showError(owner, "O nome do cargo é obrigatório.");
                return;
            }
            Position p = pos != null ? pos : new Position();
            p.setName(tfName.getText().trim());
            p.setDescription(tfDesc.getText().trim());

            String salary = tfSal.getText().trim();
            if (!salary.isBlank() && !salary.matches("\\d+(?:[.,]\\d+)?")) {
                Stage owner = (Stage) container.getScene().getWindow();
                UiHelper.showError(owner, "Salário inválido.");
                return;
            }

            p.setBaseSalary(salary.isBlank() ? BigDecimal.ZERO : new BigDecimal(salary.replace(",", ".")));

            try {
                if (pos == null)
                    dao.save(p);
                else
                    dao.update(p);
                refresh();
                formStage.close();
            } catch (Exception ex) {
                Stage owner = (Stage) container.getScene().getWindow();
                UiHelper.showError(owner, ex.getMessage());
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
            pos == null ? "Novo Cargo" : "Editar Cargo",
            root,
            false,
            false,
            javafx.stage.Modality.APPLICATION_MODAL,
            owner
        );

        formStage.showAndWait();
    }

    private void confirmDelete(Position pos) {
        Stage owner = (Stage) container.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Exclusão", "Remover cargo " + pos.getName() + "?", () -> {
            dao.delete(pos);
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

    private TableColumn<Position, String> col(String title, int w, Function<Position, String> fn) {
        TableColumn<Position, String> c = new TableColumn<>(title);
        c.setPrefWidth(w);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }

    private Button btn(String iconData, String extraClass, String colorWeb) {
        StackPane icon = UiIcon.get(iconData, 13, colorWeb);
        Button b = new Button();
        b.setGraphic(icon);
        b.getStyleClass().addAll(extraClass);
        return b;
    }

    private Label label(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }
}
