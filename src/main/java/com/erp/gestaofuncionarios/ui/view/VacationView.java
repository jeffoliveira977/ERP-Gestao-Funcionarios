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

import com.erp.gestaofuncionarios.dao.EmployeeDao;
import com.erp.gestaofuncionarios.dao.VacationDao;
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.Vacation;
import com.erp.gestaofuncionarios.model.VacationStatus;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class VacationView {

    private final VacationDao dao = new VacationDao();
    private final EmployeeDao empDao = new EmployeeDao();
    private TableView<Vacation> table;

    public Node build() {
        VBox root = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(2,
                label("Gestão de Férias", "section-title"),
                label("Controle de períodos de férias e saldos CLT", "section-subtitle"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNew = new Button("Agendar Férias");
        btnNew.getStyleClass().addAll("button", "accent");

        StackPane plusIcon = UiIcon.get(UiIcon.PLUS, 13, "#ffffff");
        btnNew.setGraphic(plusIcon);
        btnNew.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titleBlock, spacer, btnNew);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nenhuma férias registrada."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Vacation, String> colEmp = col("Funcionário", 200, v -> v.getEmployee().getFullName());
        TableColumn<Vacation, String> colStart = col("Início", 110, v -> fmt(v.getStartDate()));
        TableColumn<Vacation, String> colEnd = col("Fim", 110, v -> fmt(v.getEndDate()));
        TableColumn<Vacation, String> colDays = col("Dias", 70, v -> v.getDaysCount() + " dias");
        TableColumn<Vacation, String> colPer = col("Período Aquisitivo", 160,
                v -> v.getAcquisitionPeriod() != null ? v.getAcquisitionPeriod() : "—");

        UiHelper.makeColumnBold(colEmp);

        TableColumn<Vacation, Void> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Vacation vac = getTableRow().getItem();
                Label lbl = UiHelper.createBadge(vac.getStatus().getDescription(), badgeClass(vac.getStatus()));
                setAlignment(Pos.CENTER);
                setGraphic(lbl);
            }
        });

        TableColumn<Vacation, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button edit = btn(UiIcon.EDIT, "action-button", "-color-fg-default");
            private final Button del = btn(UiIcon.TRASH, "action-button-danger", "#bf616a");
            {
                edit.setOnAction(e -> {
                    Vacation v = getTableRow().getItem();
                    if (v != null)
                        showForm(v);
                });
                del.setOnAction(e -> {
                    Vacation v = getTableRow().getItem();
                    if (v != null)
                        confirmDelete(v);
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
                    setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(Arrays.asList(colEmp, colStart, colEnd, colDays, colPer, colStatus, colActions));
        refresh();

        root.getChildren().addAll(header, table);
        return root;
    }

    private void showForm(Vacation vacation) {
        Stage owner = (Stage) table.getScene().getWindow();
        Stage formStage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-min-width: 450px; -fx-min-height: 300px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);

        ColumnConstraints c1 = new ColumnConstraints(160);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        List<Employee> emps;
        try {
            emps = empDao.findAll();
        } catch (Exception e) {
            emps = List.of();
        }

        ComboBox<Employee> cbEmp = new ComboBox<>(FXCollections.observableArrayList(emps));
        cbEmp.setMaxWidth(Double.MAX_VALUE);
        DatePicker dpStart = new DatePicker(vacation != null ? vacation.getStartDate() : LocalDate.now());
        dpStart.setMaxWidth(Double.MAX_VALUE);
        DatePicker dpEnd = new DatePicker(vacation != null ? vacation.getEndDate() : LocalDate.now().plusDays(29));
        dpEnd.setMaxWidth(Double.MAX_VALUE);
        TextField tfPer = new TextField(
                vacation != null && vacation.getAcquisitionPeriod() != null ? vacation.getAcquisitionPeriod() : "");
        tfPer.setPromptText("Ex: 2023/2024");
        tfPer.setMaxWidth(Double.MAX_VALUE);

        ComboBox<VacationStatus> cbStatus = new ComboBox<>(FXCollections.observableArrayList(VacationStatus.values()));
        cbStatus.setValue(vacation != null ? vacation.getStatus() : VacationStatus.SCHEDULED);
        cbStatus.setMaxWidth(Double.MAX_VALUE);

        if (vacation != null)
            cbEmp.setValue(vacation.getEmployee());

        form.addRow(0, new Label("Funcionário *"), cbEmp);
        form.addRow(1, new Label("Data Início *"), dpStart);
        form.addRow(2, new Label("Data Fim *"), dpEnd);
        form.addRow(3, new Label("Período Aquisitivo"), tfPer);
        form.addRow(4, new Label("Status"), cbStatus);

        Button btnSave = new Button("Salvar");
        btnSave.getStyleClass().add("accent");
        btnSave.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            if (cbEmp.getValue() == null || dpStart.getValue() == null || dpEnd.getValue() == null) {
                UiHelper.showError(owner, "Por favor, preencha todos os campos obrigatórios.");
                return;
            }

            Vacation v = vacation != null ? vacation : new Vacation();
            v.setEmployee(cbEmp.getValue());
            v.setStartDate(dpStart.getValue());
            v.setEndDate(dpEnd.getValue());
            v.setDaysCount((int) (dpEnd.getValue().toEpochDay() - dpStart.getValue().toEpochDay()) + 1);
            v.setAcquisitionPeriod(tfPer.getText().trim());
            v.setStatus(cbStatus.getValue());

            try {
                if (vacation == null)
                    dao.save(v);
                else
                    dao.update(v);
                refresh();
                formStage.close();
            } catch (Exception ex) {
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

        UiHelper.prepareCustomWindow(
                formStage,
                vacation == null ? "Agendar Férias" : "Editar Férias",
                root,
                false,
                false,
                javafx.stage.Modality.APPLICATION_MODAL,
                owner);

        formStage.showAndWait();
    }

    private void confirmDelete(Vacation v) {
        Stage owner = (Stage) table.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Exclusão", "Remover registro de férias?", () -> {
            dao.delete(v);
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

    private String badgeClass(VacationStatus s) {
        return switch (s) {
            case SCHEDULED -> "badge-info";
            case IN_PROGRESS -> "badge-warning";
            case COMPLETED -> "badge-success";
            case CANCELED -> "badge-danger";
        };
    }

    private TableColumn<Vacation, String> col(String t, int w, Function<Vacation, String> fn) {
        TableColumn<Vacation, String> c = new TableColumn<>(t);
        c.setPrefWidth(w);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }

    private Button btn(String iconData, String extra, String colorWeb) {
        StackPane fi = UiIcon.get(iconData, 13, colorWeb);
        Button b = new Button();
        b.setGraphic(fi);
        b.getStyleClass().addAll(extra);
        return b;
    }

    private Label label(String t, String s) {
        Label l = new Label(t);
        l.getStyleClass().add(s);
        return l;
    }

    private String fmt(LocalDate d) {
        return d != null ? d.toString() : "—";
    }
}
