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
import com.erp.gestaofuncionarios.dao.AbsenceDao;
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.Absence;
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

public class AbsenceView {

    private final AbsenceDao dao = new AbsenceDao();
    private final EmployeeDao empDao = new EmployeeDao();
    private TableView<Absence> table;

    public Node build() {
        VBox root = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(2,
                lbl("Ausências & Atestados", "section-title"),
                lbl("Controle de faltas, atestados médicos e afastamentos", "section-subtitle"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNew = new Button("Registrar Ausência");
        btnNew.getStyleClass().addAll("button", "accent");

        StackPane plusIcon = UiIcon.get(UiIcon.PLUS, 13, "#ffffff");
        btnNew.setGraphic(plusIcon);
        btnNew.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titleBlock, spacer, btnNew);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nenhuma ausência registrada."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Absence, String> colEmp = col("Funcionário", 180, a -> a.getEmployee().getFullName());
        TableColumn<Absence, String> colStart = col("Início", 110, a -> fmt(a.getStartDate()));
        TableColumn<Absence, String> colEnd = col("Fim", 110, a -> fmt(a.getEndDate()));
        TableColumn<Absence, String> colDays = col("Dias", 70, a -> a.getDaysCount() + " dias");
        TableColumn<Absence, String> colReason = col("Motivo / Justificativa", 160, Absence::getReason);

        UiHelper.makeColumnBold(colEmp);

        TableColumn<Absence, Void> colExcused = new TableColumn<>("Abonado");
        colExcused.setPrefWidth(90);
        colExcused.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Absence a = getTableRow().getItem();
                Label lbl = UiHelper.createBadge(
                        a.getExcused() ? "Sim" : "Descontar",
                        a.getExcused() ? "badge-success" : "badge-danger");
                setAlignment(Pos.CENTER);
                setGraphic(lbl);
            }
        });

        TableColumn<Absence, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button edit = btn(UiIcon.EDIT, "action-button", "-color-fg-default");
            private final Button del = btn(UiIcon.TRASH, "action-button-danger", "#bf616a");
            {
                edit.setOnAction(e -> {
                    Absence a = getTableRow().getItem();
                    if (a != null)
                        showForm(a);
                });
                del.setOnAction(e -> {
                    Absence a = getTableRow().getItem();
                    if (a != null)
                        confirmDelete(a);
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

        table.getColumns().addAll(Arrays.asList(colEmp, colStart, colEnd, colDays, colReason, colExcused, colActions));
        refresh();

        root.getChildren().addAll(header, table);
        return root;
    }

    private void showForm(Absence absence) {
        Stage owner = (Stage) table.getScene().getWindow();
        Stage formStage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-min-width: 450px; -fx-min-height: 300px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(150);
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
        DatePicker dpStart = new DatePicker(absence != null ? absence.getStartDate() : LocalDate.now());
        dpStart.setMaxWidth(Double.MAX_VALUE);
        DatePicker dpEnd = new DatePicker(absence != null ? absence.getEndDate() : LocalDate.now());
        dpEnd.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> cbReason = new ComboBox<>(FXCollections.observableArrayList(
                "Atestado Médico", "Falta Não Justificada", "Licença Maternidade/Paternidade", "Folga/Banco",
                "Outros"));
        cbReason.setMaxWidth(Double.MAX_VALUE);
        cbReason.setValue(absence != null ? absence.getReason() : "Atestado Médico");

        CheckBox cbExcused = new CheckBox("Abonar ausência (Não descontar da Folha)");
        cbExcused.setSelected(absence == null || absence.getExcused());
        TextField tfNotes = new TextField(absence != null && absence.getNotes() != null ? absence.getNotes() : "");
        tfNotes.setMaxWidth(Double.MAX_VALUE);

        if (absence != null)
            cbEmp.setValue(absence.getEmployee());

        form.addRow(0, new Label("Funcionário *"), cbEmp);
        form.addRow(1, new Label("Data Início *"), dpStart);
        form.addRow(2, new Label("Data Fim *"), dpEnd);
        form.addRow(3, new Label("Motivo *"), cbReason);
        form.addRow(4, new Label("Abonado"), cbExcused);
        form.addRow(5, new Label("Observações"), tfNotes);

        Button btnSave = new Button("Salvar");
        btnSave.getStyleClass().add("accent");
        btnSave.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            if (cbEmp.getValue() == null || dpStart.getValue() == null || dpEnd.getValue() == null) {
                UiHelper.showError(owner, "Por favor, preencha todos os campos obrigatórios.");
                return;
            }

            Absence a = absence != null ? absence : new Absence();
            a.setEmployee(cbEmp.getValue());
            a.setStartDate(dpStart.getValue());
            a.setEndDate(dpEnd.getValue());
            a.setReason(cbReason.getValue());
            a.setExcused(cbExcused.isSelected());
            a.setNotes(tfNotes.getText().trim());

            try {
                if (absence == null)
                    dao.save(a);
                else
                    dao.update(a);
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
                absence == null ? "Registrar Ausência" : "Editar Ausência",
                root,
                false,
                false,
                javafx.stage.Modality.APPLICATION_MODAL,
                owner);

        formStage.showAndWait();
    }

    private void confirmDelete(Absence a) {
        Stage owner = (Stage) table.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Exclusão", "Remover registro de ausência?", () -> {
            dao.delete(a);
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

    private TableColumn<Absence, String> col(String t, int w, Function<Absence, String> fn) {
        TableColumn<Absence, String> c = new TableColumn<>(t);
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

    private Label lbl(String t, String s) {
        Label l = new Label(t);
        l.getStyleClass().add(s);
        return l;
    }

    private String fmt(LocalDate d) {
        return d != null ? d.toString() : "—";
    }
}
