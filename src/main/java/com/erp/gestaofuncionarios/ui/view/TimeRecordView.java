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
import com.erp.gestaofuncionarios.dao.TimeRecordDao;
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.TimeRecord;
import com.erp.gestaofuncionarios.ui.util.UiHelper;
import com.erp.gestaofuncionarios.ui.util.UiIcon;
import com.erp.gestaofuncionarios.util.PdfExportUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class TimeRecordView {

    private final TimeRecordDao dao = new TimeRecordDao();
    private final EmployeeDao empDao = new EmployeeDao();

    private TableView<TimeRecord> table;
    private ObservableList<TimeRecord> masterData = FXCollections.observableArrayList();
    private FilteredList<TimeRecord> filteredData;

    public Node build() {
        VBox root = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(2,
                lbl("Controle de Ponto", "section-title"),
                lbl("Histórico de entradas, saídas e horas trabalhadas de colaboradores", "section-subtitle"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNew = new Button("Registrar Ponto");
        btnNew.getStyleClass().addAll("button", "accent");

        StackPane plusIcon = UiIcon.get(UiIcon.PLUS, 13, "#ffffff");
        btnNew.setGraphic(plusIcon);
        btnNew.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titleBlock, spacer, btnNew);

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("form-section");

        List<Employee> emps;
        try {
            emps = empDao.findAll();
        } catch (Exception e) {
            emps = List.of();
        }

        ComboBox<Employee> cbEmpFilter = new ComboBox<>(FXCollections.observableArrayList(emps));
        cbEmpFilter.setPromptText("Selecione o Colaborador");
        cbEmpFilter.setPrefWidth(240);

        DatePicker dpStart = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker dpEnd = new DatePicker(LocalDate.now());
        dpStart.setPrefWidth(140);
        dpEnd.setPrefWidth(140);

        Button btnFilter = new Button("Filtrar");
        btnFilter.getStyleClass().add("button");
        btnFilter.setOnAction(e -> applyFilter(cbEmpFilter.getValue(), dpStart.getValue(), dpEnd.getValue()));

        Button btnClear = new Button("Limpar");
        btnClear.getStyleClass().add("button");
        btnClear.setOnAction(e -> {
            cbEmpFilter.setValue(null);
            dpStart.setValue(LocalDate.now().withDayOfMonth(1));
            dpEnd.setValue(LocalDate.now());
            applyFilter(null, dpStart.getValue(), dpEnd.getValue());
        });

        Button btnExportPdf = new Button("Exportar Espelho (PDF)");
        btnExportPdf.getStyleClass().addAll("button", "accent");
        btnExportPdf.setOnAction(e -> exportPdf(cbEmpFilter.getValue(), dpStart.getValue(), dpEnd.getValue(), root));

        filterBar.getChildren().addAll(
                new Label("Filtros:"), cbEmpFilter,
                new Label("Início"), dpStart,
                new Label("Fim"), dpEnd,
                btnFilter, btnClear, btnExportPdf);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nenhum registro de ponto encontrado para o filtro ativo."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<TimeRecord, String> colEmp = col("Funcionário", 200, t -> t.getEmployee().getFullName());
        TableColumn<TimeRecord, String> colDate = col("Data", 110,
                t -> t.getDate() != null ? t.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");
        TableColumn<TimeRecord, String> colIn = col("Entrada", 100,
                t -> t.getClockIn() != null ? t.getClockIn().toString() : "—");
        TableColumn<TimeRecord, String> colOut = col("Saída", 100,
                t -> t.getClockOut() != null ? t.getClockOut().toString() : "—");
        TableColumn<TimeRecord, String> colNotes = col("Observações", 200,
                t -> t.getNotes() != null ? t.getNotes() : "—");

        TableColumn<TimeRecord, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button edit = btn(UiIcon.EDIT, "action-button", "-color-fg-default");
            private final Button del = btn(UiIcon.TRASH, "action-button-danger", "#bf616a");
            {
                edit.setOnAction(e -> {
                    TimeRecord t = getTableRow().getItem();
                    if (t != null)
                        showForm(t);
                });
                del.setOnAction(e -> {
                    TimeRecord t = getTableRow().getItem();
                    if (t != null)
                        confirmDelete(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, edit, del));
            }
        });

        table.getColumns().addAll(Arrays.asList(colEmp, colDate, colIn, colOut, colNotes, colActions));

        loadMasterData();
        applyFilter(null, dpStart.getValue(), dpEnd.getValue());

        root.getChildren().addAll(header, filterBar, table);
        return root;
    }

    private void applyFilter(Employee emp, LocalDate start, LocalDate end) {
        filteredData.setPredicate(record -> {
            if (emp != null && !record.getEmployee().getId().equals(emp.getId())) {
                return false;
            }
            if (start != null && record.getDate().isBefore(start)) {
                return false;
            }
            if (end != null && record.getDate().isAfter(end)) {
                return false;
            }
            return true;
        });
        table.setItems(filteredData);
    }

    private void loadMasterData() {
        try {
            masterData.setAll(dao.findAllOrderByDateDesc());
        } catch (Exception e) {
            masterData.clear();
        }
        filteredData = new FilteredList<>(masterData);
    }

    private void exportPdf(Employee emp, LocalDate start, LocalDate end, Node parent) {
        Stage owner = (Stage) parent.getScene().getWindow();
        if (emp == null) {
            UiHelper.showError(owner,
                    "Por favor, selecione um Colaborador no filtro antes de exportar o Espelho de Ponto.");
            return;
        }
        if (start == null || end == null) {
            UiHelper.showError(owner, "Defina as datas de início e fim para a exportação.");
            return;
        }

        List<TimeRecord> records = filteredData.stream()
                .filter(r -> r.getEmployee().getId().equals(emp.getId()))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .toList();

        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar Espelho de Ponto");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
        fc.setInitialFileName("Espelho_Ponto_" + emp.getFullName().replace(" ", "_") + "_" + start.getMonthValue() + "_"
                + start.getYear() + ".pdf");

        File file = fc.showSaveDialog(parent.getScene().getWindow());
        if (file != null) {
            try {
                PdfExportUtil.exportTimeSheetToPdf(file, emp, start, end, records);
                UiHelper.showInfo(owner, "Sucesso", "Espelho de Ponto exportado em PDF com sucesso!");
            } catch (Exception ex) {
                UiHelper.showError(owner, "Falha ao gerar PDF de Ponto: " + ex.getMessage());
            }
        }
    }

    private void showForm(TimeRecord record) {
        Stage owner = (Stage) table.getScene().getWindow();
        Stage formStage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-min-width: 420px; -fx-min-height: 300px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(120);
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
        if (record != null)
            cbEmp.setValue(record.getEmployee());

        DatePicker dpDate = new DatePicker(record != null ? record.getDate() : LocalDate.now());
        dpDate.setMaxWidth(Double.MAX_VALUE);
        TextField tfIn = new TextField(
                record != null && record.getClockIn() != null ? record.getClockIn().toString() : "08:00");
        tfIn.setMaxWidth(Double.MAX_VALUE);
        TextField tfOut = new TextField(
                record != null && record.getClockOut() != null ? record.getClockOut().toString() : "17:00");
        tfOut.setMaxWidth(Double.MAX_VALUE);

        tfIn.setPromptText("HH:MM");
        tfOut.setPromptText("HH:MM");
        TextField tfNotes = new TextField(record != null && record.getNotes() != null ? record.getNotes() : "");
        tfNotes.setMaxWidth(Double.MAX_VALUE);

        form.addRow(0, new Label("Funcionário *"), cbEmp);
        form.addRow(1, new Label("Data *"), dpDate);
        form.addRow(2, new Label("Hora Entrada"), tfIn);
        form.addRow(3, new Label("Hora Saída"), tfOut);
        form.addRow(4, new Label("Observações"), tfNotes);

        Button btnSave = new Button("Salvar");
        btnSave.getStyleClass().add("accent");
        btnSave.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            if (cbEmp.getValue() == null || dpDate.getValue() == null) {
                UiHelper.showError(owner, "Por favor, preencha todos os campos obrigatórios.");
                return;
            }

            TimeRecord t = record != null ? record : new TimeRecord();
            t.setEmployee(cbEmp.getValue());
            t.setDate(dpDate.getValue());

            try {
                t.setClockIn(LocalTime.parse(tfIn.getText().trim()));
            } catch (Exception ex) {
                t.setClockIn(null);
            }

            try {
                t.setClockOut(LocalTime.parse(tfOut.getText().trim()));
            } catch (Exception ex) {
                t.setClockOut(null);
            }

            t.setNotes(tfNotes.getText().trim());

            try {
                if (record == null)
                    dao.save(t);
                else
                    dao.update(t);
                loadMasterData();
                applyFilter(cbEmp.getValue(), LocalDate.now().withDayOfMonth(1), LocalDate.now());
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
                record == null ? "Registrar Ponto" : "Editar Ponto",
                root,
                false,
                false,
                javafx.stage.Modality.APPLICATION_MODAL,
                owner);

        formStage.showAndWait();
    }

    private void confirmDelete(TimeRecord record) {
        Stage owner = (Stage) table.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Exclusão", "Remover registro de ponto?", () -> {
            dao.delete(record);
            loadMasterData();
            table.setItems(filteredData);
        });
    }

    private TableColumn<TimeRecord, String> col(String t, int w, Function<TimeRecord, String> fn) {
        TableColumn<TimeRecord, String> c = new TableColumn<>(t);
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
}