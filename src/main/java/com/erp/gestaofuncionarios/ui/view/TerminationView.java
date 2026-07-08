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
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.EmployeeStatus;
import com.erp.gestaofuncionarios.util.PdfExportUtil;
import com.erp.gestaofuncionarios.ui.util.UiHelper;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TerminationView {

    private final EmployeeDao empDao = new EmployeeDao();
    private ComboBox<Employee> cbEmp;
    private ComboBox<String> cbType;
    private DatePicker dpDate;
    private CheckBox cbExcusedNotice;
    private TextField tfFgtsBalance;

    private TextArea taResult;
    private Button btnConfirm;
    private Button btnExportPdf;
    private Employee selectedEmp;

    private BigDecimal balanceSalaryVal = BigDecimal.ZERO;
    private BigDecimal proportional13Val = BigDecimal.ZERO;
    private BigDecimal vacationVal = BigDecimal.ZERO;
    private BigDecimal noticePayVal = BigDecimal.ZERO;
    private BigDecimal fgtsBalanceVal = BigDecimal.ZERO;
    private BigDecimal fgtsFineVal = BigDecimal.ZERO;
    private BigDecimal totalRescision = BigDecimal.ZERO;
    private int calculatedNoticeDays = 0;

    public Node build() {
        VBox root = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(2,
                lbl("Cálculo de Rescisão", "section-title"),
                lbl("Assistente de processamento de demissões CLT", "section-subtitle"));
        header.getChildren().add(titleBlock);

        GridPane form = new GridPane();
        form.getStyleClass().add("form-section");
        form.setHgap(14);
        form.setVgap(12);

        List<Employee> emps;
        try {
            emps = empDao.findAll().stream()
                    .filter(e -> e.getStatus() != EmployeeStatus.OFF && e.getStatus() != EmployeeStatus.INACTIVE)
                    .toList();
        } catch (Exception e) {
            emps = List.of();
        }
        cbEmp = new ComboBox<>(FXCollections.observableArrayList(emps));
        cbEmp.setMaxWidth(Double.MAX_VALUE);

        tfFgtsBalance = new TextField("0.00");
        tfFgtsBalance.setMaxWidth(Double.MAX_VALUE);

        cbEmp.setOnAction(e -> {
            Employee emp = cbEmp.getValue();
            if (emp != null && emp.getSalary() != null && emp.getHireDate() != null) {
                long months = ChronoUnit.MONTHS.between(emp.getHireDate(), LocalDate.now());
                BigDecimal estimatedFgts = emp.getSalary().multiply(BigDecimal.valueOf(0.08))
                        .multiply(BigDecimal.valueOf(Math.max(months, 1)));
                tfFgtsBalance.setText(String.format("%.2f", estimatedFgts).replace(",", "."));
            }
        });

        cbType = new ComboBox<>(FXCollections.observableArrayList(
                "Sem justa causa", "Com justa causa", "Pedido de demissão", "Acordo comum"));
        cbType.setValue("Sem justa causa");
        cbType.setMaxWidth(Double.MAX_VALUE);

        dpDate = new DatePicker(LocalDate.now());
        dpDate.setMaxWidth(Double.MAX_VALUE);

        cbExcusedNotice = new CheckBox("Aviso Prévio Indenizado (Pago pela empresa)");
        cbExcusedNotice.setSelected(true);

        form.addRow(0, new Label("Funcionário *"), cbEmp);
        form.addRow(1, new Label("Tipo de Rescisão *"), cbType);
        form.addRow(2, new Label("Data de Desligamento *"), dpDate);
        form.addRow(3, new Label("Aviso Prévio"), cbExcusedNotice);
        form.addRow(4, new Label("Saldo FGTS para Fins Rescisórios (R$)"), tfFgtsBalance);

        ColumnConstraints c1 = new ColumnConstraints(220);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        Button btnSimulate = new Button("Calcular Rescisão");
        btnSimulate.getStyleClass().addAll("button", "accent");
        btnSimulate.setMaxWidth(180);
        btnSimulate.setOnAction(e -> simulate());

        btnConfirm = new Button("Efetivar Desligamento");
        btnConfirm.getStyleClass().addAll("button", "danger");
        btnConfirm.setMaxWidth(200);
        btnConfirm.setDisable(true);
        btnConfirm.setOnAction(e -> confirmTermination());

        btnExportPdf = new Button("Exportar PDF");
        btnExportPdf.getStyleClass().addAll("button");
        btnExportPdf.setMaxWidth(150);
        btnExportPdf.setDisable(true);
        btnExportPdf.setOnAction(e -> exportPdf(root));

        taResult = new TextArea();
        taResult.setEditable(false);
        taResult.setPromptText("Os cálculos detalhados aparecerão aqui...");
        taResult.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");
        VBox.setVgrow(taResult, Priority.ALWAYS);

        HBox actions = new HBox(12, btnSimulate, btnConfirm, btnExportPdf);
        actions.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(header, form, actions, taResult);
        return root;
    }

    private void simulate() {
        selectedEmp = cbEmp.getValue();
        if (selectedEmp == null || dpDate.getValue() == null) {
            Stage owner = (Stage) cbEmp.getScene().getWindow();
            UiHelper.showError(owner, "Selecione o funcionário e a data de desligamento.");
            return;
        }

        LocalDate admission = selectedEmp.getHireDate();
        LocalDate exit = dpDate.getValue();

        if (exit.isBefore(admission)) {
            Stage owner = (Stage) cbEmp.getScene().getWindow();
            UiHelper.showError(owner, "A data de desligamento não pode ser anterior à data de admissão.");
            return;
        }

        String type = cbType.getValue();
        BigDecimal salary = selectedEmp.getSalary() != null ? selectedEmp.getSalary() : BigDecimal.ZERO;

        // Salary Balance
        int exitDay = exit.getDayOfMonth();
        BigDecimal dailySalary = salary.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        balanceSalaryVal = dailySalary.multiply(BigDecimal.valueOf(exitDay)).setScale(2, RoundingMode.HALF_UP);

        // Proportional 13th Salary
        int months13 = exit.getMonthValue();
        if (exit.getDayOfMonth() < 15) {
            months13 -= 1;
        }
        proportional13Val = salary.multiply(BigDecimal.valueOf(months13))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        // Proportional Vacations + 1/3
        long totalDaysWorked = ChronoUnit.DAYS.between(admission, exit);
        long yearsWorked = totalDaysWorked / 365;
        LocalDate lastAcquisitionStart = admission.plusYears(yearsWorked);
        long daysInAcquisition = ChronoUnit.DAYS.between(lastAcquisitionStart, exit);
        int monthsVacation = (int) (daysInAcquisition / 30);
        if (daysInAcquisition % 30 >= 15) {
            monthsVacation += 1;
        }

        BigDecimal vacationBase = salary.multiply(BigDecimal.valueOf(monthsVacation))
                .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
        BigDecimal vacationOneThird = vacationBase.divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP);
        vacationVal = vacationBase.add(vacationOneThird).setScale(2, RoundingMode.HALF_UP);

        // Indemnified Prior Notice
        noticePayVal = BigDecimal.ZERO;
        calculatedNoticeDays = 0;
        if ("Sem justa causa".equals(type) && cbExcusedNotice.isSelected()) {
            calculatedNoticeDays = 30 + (3 * (int) yearsWorked);
            if (calculatedNoticeDays > 90)
                calculatedNoticeDays = 90;
            noticePayVal = dailySalary.multiply(BigDecimal.valueOf(calculatedNoticeDays)).setScale(2,
                    RoundingMode.HALF_UP);
        } else if ("Acordo comum".equals(type) && cbExcusedNotice.isSelected()) {
            calculatedNoticeDays = 30 + (3 * (int) yearsWorked);
            if (calculatedNoticeDays > 90)
                calculatedNoticeDays = 90;
            noticePayVal = dailySalary.multiply(BigDecimal.valueOf(calculatedNoticeDays))
                    .multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
        }

        if ("Com justa causa".equals(type)) {
            proportional13Val = BigDecimal.ZERO;
            vacationVal = BigDecimal.ZERO;
            noticePayVal = BigDecimal.ZERO;
        }

        // FGTS Balance and Fine
        try {
            fgtsBalanceVal = new BigDecimal(tfFgtsBalance.getText().trim().replace(",", "."));
        } catch (Exception ex) {
            fgtsBalanceVal = BigDecimal.ZERO;
        }

        fgtsFineVal = BigDecimal.ZERO;
        if ("Sem justa causa".equals(type)) {
            fgtsFineVal = fgtsBalanceVal.multiply(BigDecimal.valueOf(0.40)).setScale(2, RoundingMode.HALF_UP);
        } else if ("Acordo comum".equals(type)) {
            fgtsFineVal = fgtsBalanceVal.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
        }

        totalRescision = balanceSalaryVal.add(proportional13Val).add(vacationVal).add(noticePayVal).add(fgtsFineVal);

        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================================\n");
        sb.append("                      DEMONSTRATIVO DE RESCISÃO CLT                      \n");
        sb.append("=========================================================================\n");
        sb.append(String.format("COLABORADOR:     %s%n", selectedEmp.getFullName()));
        sb.append(String.format("MATRÍCULA:       %s%n", selectedEmp.getRegistrationNumber()));
        sb.append(String.format("CARGO:           %s%n",
                selectedEmp.getPosition() != null ? selectedEmp.getPosition().getName() : "—"));
        sb.append(String.format("ADMISSÃO:        %s%n", admission));
        sb.append(String.format("AFASTAMENTO:     %s%n", exit));
        sb.append(String.format("TEMPO SERVIÇO:   %d ano(s) e %d dia(s)%n", yearsWorked, daysInAcquisition % 365));
        sb.append(String.format("MOTIVO:          %s%n", type));
        sb.append("-------------------------------------------------------------------------\n");
        sb.append(
                String.format("  (+) Saldo de Salário (%d dias):            R$ %,10.2f%n", exitDay, balanceSalaryVal));
        if (proportional13Val.compareTo(BigDecimal.ZERO) > 0)
            sb.append(String.format("  (+) 13º Salário Proporcional (%d/12):      R$ %,10.2f%n", months13,
                    proportional13Val));
        if (vacationVal.compareTo(BigDecimal.ZERO) > 0)
            sb.append(String.format("  (+) Férias Proporcionais + 1/3 (%d/12):    R$ %,10.2f%n", monthsVacation,
                    vacationVal));
        if (noticePayVal.compareTo(BigDecimal.ZERO) > 0)
            sb.append(String.format("  (+) Aviso Prévio Indenizado (%d dias):      R$ %,10.2f%n", calculatedNoticeDays,
                    noticePayVal));
        if (fgtsFineVal.compareTo(BigDecimal.ZERO) > 0)
            sb.append(String.format("  (+) Multa Rescisória do FGTS (Acumulado):  R$ %,10.2f%n", fgtsFineVal));
        sb.append("-------------------------------------------------------------------------\n");
        sb.append(String.format("  (=) TOTAL LÍQUIDO A RECEBER:                R$ %,10.2f%n", totalRescision));
        sb.append("=========================================================================\n");
        sb.append(String.format("* Base do Saldo do FGTS Informado:           R$ %,10.2f%n", fgtsBalanceVal));

        taResult.setText(sb.toString());
        btnConfirm.setDisable(false);
        btnExportPdf.setDisable(false);
    }

    private void confirmTermination() {
        if (selectedEmp == null)
            return;

        Stage owner = (Stage) cbEmp.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Desligamento",
                "Tem certeza que deseja efetivar o desligamento de " + selectedEmp.getFullName()
                        + "?\nEsta ação marcará o cadastro como inativo e não poderá ser desfeita.",
                () -> {
                    selectedEmp.setStatus(EmployeeStatus.OFF);
                    selectedEmp.setTerminationDate(dpDate.getValue());
                    selectedEmp.setTerminationReason(cbType.getValue());
                    try {
                        empDao.update(selectedEmp);
                        UiHelper.showInfo(owner, "Sucesso", "Colaborador desligado com sucesso!");
                        btnConfirm.setDisable(true);

                        List<Employee> emps = empDao.findAll().stream()
                                .filter(e -> e.getStatus() != EmployeeStatus.OFF
                                        && e.getStatus() != EmployeeStatus.INACTIVE)
                                .toList();
                        cbEmp.setItems(FXCollections.observableArrayList(emps));
                    } catch (Exception ex) {
                        UiHelper.showError(owner, "Erro ao atualizar registro: " + ex.getMessage());
                    }
                });
    }

    private void exportPdf(Node parent) {
        if (selectedEmp == null)
            return;

        Stage owner = (Stage) parent.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar Termo de Rescisão");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
        fc.setInitialFileName("TRCT_" + selectedEmp.getFullName().replace(" ", "_") + ".pdf");

        File file = fc.showSaveDialog(parent.getScene().getWindow());
        if (file != null) {
            try {
                PdfExportUtil.exportTerminationToPdf(
                        file,
                        selectedEmp,
                        cbType.getValue(),
                        dpDate.getValue(),
                        calculatedNoticeDays,
                        balanceSalaryVal,
                        proportional13Val,
                        vacationVal,
                        fgtsBalanceVal,
                        fgtsFineVal,
                        totalRescision);
                UiHelper.showInfo(owner, "Sucesso", "Termo de Rescisão (TRCT) exportado em PDF com sucesso!");
            } catch (Exception ex) {
                UiHelper.showError(owner, "Falha ao gerar PDF: " + ex.getMessage());
            }
        }
    }

    private Label lbl(String t, String s) {
        Label l = new Label(t);
        l.getStyleClass().add(s);
        return l;
    }
}
