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
import com.erp.gestaofuncionarios.dao.PayrollDao;
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.Payroll;
import com.erp.gestaofuncionarios.model.PayrollStatus;
import com.erp.gestaofuncionarios.service.PayrollCalculator;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class PayrollView {

    private final PayrollDao dao = new PayrollDao();
    private final EmployeeDao empDao = new EmployeeDao();
    private TableView<Payroll> table;

    public Node build() {
        VBox root = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBlock = new VBox(2,
                lbl("Folha de Pagamento", "section-title"),
                lbl("Processamento de holerites com cálculo de INSS e IRRF", "section-subtitle"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnGen = new Button("Gerar Holerite");
        btnGen.getStyleClass().addAll("button", "accent");

        StackPane dollarIcon = UiIcon.get(UiIcon.DOLLAR, 13, "#ffffff");
        btnGen.setGraphic(dollarIcon);
        btnGen.setOnAction(e -> showGenerateDialog());
        header.getChildren().addAll(titleBlock, spacer, btnGen);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nenhum holerite gerado."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Payroll, String> colEmp = col("Funcionário", 180, p -> p.getEmployee().getFullName());
        TableColumn<Payroll, String> colPeriod = col("Período", 100, p -> p.getPeriod());
        TableColumn<Payroll, String> colBase = col("Salário Base", 120, p -> fmt(p.getBaseSalary()));
        TableColumn<Payroll, String> colDeductions = col("Descontos", 120, p -> fmt(p.getTotalDeductions()));
        TableColumn<Payroll, String> colNetSalary = col("Líquido", 120, p -> fmt(p.getNetSalary()));
        TableColumn<Payroll, String> colPaymentDate = col("Pagamento", 110,
                p -> p.getPaymentDate() != null ? p.getPaymentDate().toString() : "—");

        UiHelper.makeColumnBold(colEmp);
        UiHelper.makeColumnBold(colNetSalary);

        TableColumn<Payroll, Void> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(110);
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Payroll p = getTableRow().getItem();
                String desc = switch (p.getStatus()) {
                    case PAID -> "Pago";
                    case DRAFT -> "Rascunho";
                    case CANCELLED -> "Cancelado";
                    default -> p.getStatus().name();
                };
                Label lbl = UiHelper.createBadge(desc, badgeClass(p.getStatus()));
                setAlignment(Pos.CENTER);
                setGraphic(lbl);
            }
        });

        TableColumn<Payroll, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button detail = new Button();
            private final Button approve = new Button();
            {
                StackPane eyeIcon = UiIcon.get(UiIcon.EYE, 13, "-color-fg-default");
                StackPane checkIcon = UiIcon.get(UiIcon.CHECK, 13, "#a3be8c");

                detail.setGraphic(eyeIcon);
                detail.getStyleClass().addAll("action-button");
                detail.setTooltip(new Tooltip("Ver Holerite"));

                approve.setGraphic(checkIcon);
                approve.getStyleClass().addAll("action-button");
                approve.setTooltip(new Tooltip("Marcar como Pago"));

                detail.setOnAction(e -> {
                    Payroll p = getTableRow().getItem();
                    if (p != null)
                        showDetail(p);
                });
                approve.setOnAction(e -> {
                    Payroll p = getTableRow().getItem();
                    if (p != null)
                        markPaid(p);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Payroll p = getTableRow().getItem();
                approve.setVisible(p.getStatus() == PayrollStatus.DRAFT);
                setGraphic(new HBox(6, detail, approve));
            }
        });

        table.getColumns()
                .addAll(Arrays.asList(colEmp, colPeriod, colBase, colDeductions, colNetSalary, colPaymentDate,
                        colStatus, colActions));
        refresh();
        root.getChildren().addAll(header, table);
        return root;
    }

    private void showGenerateDialog() {
        Stage owner = (Stage) table.getScene().getWindow();
        Stage formStage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-min-width: 420px; -fx-min-height: 250px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(140);
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
        String currentPeriod = String.format("%02d/%d", LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        Label lblPeriodVal = new Label(currentPeriod);
        lblPeriodVal.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-accent-fg;");

        TextField tfAllowance = new TextField("0.00");
        tfAllowance.setPromptText("Ex: 500.00");
        tfAllowance.setMaxWidth(Double.MAX_VALUE);

        form.addRow(0, new Label("Funcionário *"), cbEmp);
        form.addRow(1, new Label("Período"), lblPeriodVal);
        form.addRow(2, new Label("Proventos Extras (R$)"), tfAllowance);

        Button btnSave = new Button("Gerar");
        btnSave.getStyleClass().add("accent");
        btnSave.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            if (cbEmp.getValue() == null) {
                UiHelper.showError(owner, "Por favor, selecione o funcionário.");
                return;
            }
            BigDecimal allowance;
            try {
                allowance = new BigDecimal(tfAllowance.getText().replace(",", "."));
            } catch (Exception ex) {
                allowance = BigDecimal.ZERO;
            }

            Payroll p = PayrollCalculator.generatePayroll(cbEmp.getValue(), currentPeriod, allowance);
            try {
                dao.save(p);
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
                "Gerar Holerite",
                root,
                false,
                false,
                javafx.stage.Modality.APPLICATION_MODAL,
                owner);

        formStage.showAndWait();
    }

    private void showDetail(Payroll p) {
        Stage detailStage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        StringBuilder sb = new StringBuilder();
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("FUNCIONÁRIO:  ").append(p.getEmployee().getFullName()).append("\n");
        if (p.getEmployee().getPosition() != null)
            sb.append("CARGO:        ").append(p.getEmployee().getPosition().getName()).append("\n");
        if (p.getEmployee().getDepartment() != null)
            sb.append("DEPTO:        ").append(p.getEmployee().getDepartment().getName()).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("PROVENTOS\n");
        sb.append(String.format("  Salário Base:      R$ %,10.2f%n", p.getBaseSalary()));
        if (p.getAllowances() != null && p.getAllowances().compareTo(BigDecimal.ZERO) > 0)
            sb.append(String.format("  Proventos Extras:  R$ %,10.2f%n", p.getAllowances()));
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("DESCONTOS\n");

        BigDecimal inss = PayrollCalculator.calculateInss(p.getBaseSalary());
        BigDecimal baseIrrf = p.getBaseSalary().subtract(inss);
        BigDecimal irrf = PayrollCalculator.calculateIrrf(baseIrrf);

        sb.append(String.format("  INSS:              R$ %,10.2f%n", inss));
        sb.append(String.format("  IRRF:              R$ %,10.2f%n", irrf));
        sb.append(String.format("  Total Descontos:   R$ %,10.2f%n", p.getTotalDeductions()));
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format("  SALÁRIO LÍQUIDO:   R$ %,10.2f%n", p.getNetSalary()));
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("Status: ").append(p.getStatus());

        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");
        ta.setPrefSize(500, 350);

        Button btnOk = new Button("Fechar");
        btnOk.getStyleClass().add("accent");
        btnOk.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnOk.setOnAction(e -> detailStage.close());

        HBox footer = new HBox(btnOk);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(ta, footer);

        Stage owner = (Stage) table.getScene().getWindow();
        UiHelper.prepareCustomWindow(
                detailStage,
                "Holerite — " + p.getEmployee().getFullName(),
                root,
                false,
                false,
                javafx.stage.Modality.APPLICATION_MODAL,
                owner);

        detailStage.showAndWait();
    }

    private void markPaid(Payroll p) {
        Stage owner = (Stage) table.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Pagamento", "Marcar este holerite como pago?", () -> {
            p.setStatus(PayrollStatus.PAID);
            p.setPaymentDate(LocalDate.now());
            try {
                dao.update(p);
                refresh();
            } catch (Exception ex) {
                UiHelper.showError(owner, ex.getMessage());
            }
        });
    }

    private void refresh() {
        try {
            table.setItems(FXCollections.observableArrayList(dao.findAll()));
        } catch (Exception e) {
            table.setItems(FXCollections.emptyObservableList());
        }
    }

    private String badgeClass(PayrollStatus s) {
        return switch (s) {
            case PAID -> "badge-success";
            case DRAFT -> "badge-warning";
            case CANCELLED -> "badge-danger";
            default -> "badge-info";
        };
    }

    private String fmt(BigDecimal v) {
        if (v == null)
            return "R$ 0,00";
        return "R$ " + String.format("%,.2f", v).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    private TableColumn<Payroll, String> col(String t, int w, Function<Payroll, String> fn) {
        TableColumn<Payroll, String> c = new TableColumn<>(t);
        c.setPrefWidth(w);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        return c;
    }

    private Label lbl(String t, String s) {
        Label l = new Label(t);
        l.getStyleClass().add(s);
        return l;
    }
}
