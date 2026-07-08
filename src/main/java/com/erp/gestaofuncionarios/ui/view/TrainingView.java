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
import com.erp.gestaofuncionarios.dao.TrainingRecordDao;
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.TrainingRecord;
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

public class TrainingView {

    private final TrainingRecordDao dao = new TrainingRecordDao();
    private final EmployeeDao empDao = new EmployeeDao();
    private TableView<TrainingRecord> table;

    public Node build() {
        VBox root = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBlock = new VBox(2,
                lbl("Treinamentos", "section-title"),
                lbl("Histórico de cursos e certificações dos colaboradores", "section-subtitle"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNew = new Button("Registrar Treinamento");
        btnNew.getStyleClass().addAll("button", "accent");

        StackPane plusIcon = UiIcon.get(UiIcon.PLUS, 13, "#ffffff");
        btnNew.setGraphic(plusIcon);
        btnNew.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titleBlock, spacer, btnNew);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nenhum treinamento registrado."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<TrainingRecord, String> colEmp = col("Funcionário", 180, t -> t.getEmployee().getFullName());
        TableColumn<TrainingRecord, String> colCourse = col("Curso / Treinamento", 220, TrainingRecord::getCourseName);
        TableColumn<TrainingRecord, String> colInst = col("Instituição", 160,
                t -> t.getInstitution() != null ? t.getInstitution() : "—");

        TableColumn<TrainingRecord, String> colDate = col("Conclusão", 110, t -> fmt(t.getCompletionDate()));

        UiHelper.makeColumnBold(colEmp);

        TableColumn<TrainingRecord, Void> colHours = new TableColumn<>("Carga (h)");
        colHours.setPrefWidth(90);
        colHours.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                TrainingRecord t = getTableRow().getItem();
                Label lbl = new Label(t.getWorkloadHours() != null ? t.getWorkloadHours() + "h" : "—");
                lbl.getStyleClass().add("badge-info");
                setAlignment(Pos.CENTER);
                setGraphic(lbl);
            }
        });

        TableColumn<TrainingRecord, Void> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                TrainingRecord t = getTableRow().getItem();
                boolean completed = t.getCompletionDate() != null && !t.getCompletionDate().isAfter(LocalDate.now());
                Label lbl = UiHelper.createBadge(
                        completed ? "Concluído" : "Em Andamento",
                        completed ? "badge-success" : "badge-warning");
                setAlignment(Pos.CENTER);
                setGraphic(lbl);
            }
        });

        TableColumn<TrainingRecord, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button edit = btn(UiIcon.EDIT, "action-button", "-color-fg-default");
            private final Button del = btn(UiIcon.TRASH, "action-button-danger", "#bf616a");
            {
                edit.setOnAction(e -> {
                    TrainingRecord t = getTableRow().getItem();
                    if (t != null)
                        showForm(t);
                });
                del.setOnAction(e -> {
                    TrainingRecord t = getTableRow().getItem();
                    if (t != null)
                        confirmDelete(t);
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

        table.getColumns().addAll(Arrays.asList(colEmp, colCourse, colInst, colDate, colHours, colStatus, colActions));
        refresh();
        root.getChildren().addAll(header, table);
        return root;
    }

    private void showForm(TrainingRecord training) {
        Stage owner = (Stage) table.getScene().getWindow();
        Stage formStage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-min-width: 440px; -fx-min-height: 360px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(145);
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

        if (training != null)
            cbEmp.setValue(training.getEmployee());

        TextField tfCourse = new TextField(training != null ? training.getCourseName() : "");
        tfCourse.setMaxWidth(Double.MAX_VALUE);

        TextField tfInst = new TextField(
                training != null && training.getInstitution() != null ? training.getInstitution() : "");
        tfInst.setMaxWidth(Double.MAX_VALUE);

        DatePicker dpDate = new DatePicker(training != null ? training.getCompletionDate() : LocalDate.now());
        dpDate.setMaxWidth(Double.MAX_VALUE);

        TextField tfHours = new TextField(
                training != null && training.getWorkloadHours() != null ? training.getWorkloadHours().toString() : "");
        tfHours.setMaxWidth(Double.MAX_VALUE);

        TextField tfCert = new TextField(
                training != null && training.getCertificateUrl() != null ? training.getCertificateUrl() : "");
        tfCert.setMaxWidth(Double.MAX_VALUE);

        TextArea taNotes = new TextArea(training != null && training.getNotes() != null ? training.getNotes() : "");
        taNotes.setPrefRowCount(2);
        taNotes.setMaxWidth(Double.MAX_VALUE);

        form.addRow(0, new Label("Funcionário *"), cbEmp);
        form.addRow(1, new Label("Curso / Treinamento *"), tfCourse);
        form.addRow(2, new Label("Instituição"), tfInst);
        form.addRow(3, new Label("Data de Conclusão"), dpDate);
        form.addRow(4, new Label("Carga Horária (h)"), tfHours);
        form.addRow(5, new Label("URL do Certificado"), tfCert);
        form.addRow(6, new Label("Observações"), taNotes);

        Button btnSave = new Button("Salvar");
        btnSave.getStyleClass().add("accent");
        btnSave.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            if (cbEmp.getValue() == null || tfCourse.getText().isBlank()) {
                UiHelper.showError(owner, "Por favor, preencha todos os campos obrigatórios.");
                return;
            }

            TrainingRecord t = training != null ? training : new TrainingRecord();
            t.setEmployee(cbEmp.getValue());
            t.setCourseName(tfCourse.getText().trim());
            t.setInstitution(tfInst.getText().trim());
            t.setCompletionDate(dpDate.getValue());

            try {
                t.setWorkloadHours(Integer.parseInt(tfHours.getText().trim()));
            } catch (Exception ex) {
                t.setWorkloadHours(0);
            }

            t.setCertificateUrl(tfCert.getText().trim());
            t.setNotes(taNotes.getText().trim());

            try {
                if (training == null)
                    dao.save(t);
                else
                    dao.update(t);
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
                training == null ? "Registrar Treinamento" : "Editar Treinamento",
                root,
                false,
                false,
                javafx.stage.Modality.APPLICATION_MODAL,
                owner);

        formStage.showAndWait();
    }

    private void confirmDelete(TrainingRecord t) {
        Stage owner = (Stage) table.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Exclusão", "Remover registro de treinamento?", () -> {
            dao.delete(t);
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

    private TableColumn<TrainingRecord, String> col(String t, int w,
            Function<TrainingRecord, String> fn) {
        TableColumn<TrainingRecord, String> c = new TableColumn<>(t);
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
