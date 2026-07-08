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

import com.erp.gestaofuncionarios.dao.DepartmentDao;
import com.erp.gestaofuncionarios.dao.EmployeeDao;
import com.erp.gestaofuncionarios.dao.PositionDao;
import com.erp.gestaofuncionarios.model.*;
import com.erp.gestaofuncionarios.ui.util.UiHelper;
import com.erp.gestaofuncionarios.ui.util.UiIcon;
import com.erp.gestaofuncionarios.util.ValidationUtil;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.UnaryOperator;

public class EmployeeFormView {

    private final Employee employee;
    private final Runnable onSaved;
    private Stage stage;

    private TextField tfFirstName, tfLastName, tfCpf, tfRg, tfPis, tfEmail,
            tfPhone, tfMobile, tfNationality;

    private DatePicker dpBirthDate;
    private ComboBox<Gender> cbGender;
    private ComboBox<MaritalStatus> cbMarital;

    private TextField tfStreet, tfNumber, tfComplement, tfNeighborhood, tfCity, tfState, tfZip;

    private DatePicker dpHireDate, dpTermination;
    private ComboBox<EmployeeStatus> cbStatus;
    private ComboBox<ContractType> cbContract;
    private ComboBox<com.erp.gestaofuncionarios.model.Department> cbDept;
    private ComboBox<Position> cbPosition;
    private TextField tfSalary, tfHours;
    private TextArea taNotes;

    private CheckBox cbDocCpf, cbDocRg, cbDocWorkCard, cbDocAddress, cbDocAso, cbDocPhoto;

    private TextField tfBank, tfAgency, tfAccount, tfAccountType, tfPix;

    private TextField tfEmergName, tfEmergPhone, tfEmergRel;

    private final DepartmentDao depDao = new DepartmentDao();
    private final PositionDao posDao = new PositionDao();
    private final EmployeeDao empDao = new EmployeeDao();

    public EmployeeFormView(Employee employee, Runnable onSaved) {
        this.employee = employee;
        this.onSaved = onSaved;
    }

    public void showDialog() {
        stage = new Stage();
        stage.setWidth(1000);
        stage.setHeight(500);
        stage.setMinWidth(1000);
        stage.setMinHeight(500);
   

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                tabPessoal(),
                tabEndereco(),
                tabProfissional(),
                tabBancario(),
                tabEmergencia(),
                tabDocumentos(),
                tabNotes());

        if (employee != null)
            populate();

        HBox footer = buildFooter();

        VBox root = new VBox(0, tabs, footer);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        UiHelper.prepareCustomWindow(
                stage, 
                employee == null ? "Novo Funcionário" : "Editar: " + employee.getFullName(), 
                root, 
                false,
                false,
                Modality.APPLICATION_MODAL, 
                null
        );

        stage.show();
    }

    private Tab tabPessoal() {
        Tab tab = new Tab("Dados Pessoais");
        tab.setGraphic(UiIcon.get(UiIcon.USER, 14, "-color-fg-muted"));
        GridPane grid = formGrid();

        tfFirstName = field();
        tfLastName = field();
        tfCpf = field();
        tfRg = field();
        tfPis = field();
        tfEmail = field();
        tfPhone = field();
        tfMobile = field();
        tfNationality = field();
        dpBirthDate = new DatePicker();
        cbGender = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        cbMarital = new ComboBox<>(FXCollections.observableArrayList(MaritalStatus.values()));

        int row = 0;
        addRow(grid, row++, "Nome *", tfFirstName, "Sobrenome *", tfLastName);
        addRow(grid, row++, "CPF *", tfCpf, "RG", tfRg);
        addRow(grid, row++, "PIS/PASEP", tfPis, "E-mail *", tfEmail);
        addRow(grid, row++, "Telefone Fixo", tfPhone, "Celular", tfMobile);
        addRow(grid, row++, "Data de Nascimento", dpBirthDate, "Nacionalidade", tfNationality);
        addRow(grid, row++, "Gênero", cbGender, "Estado Civil", cbMarital);

        cbGender.setMaxWidth(Double.MAX_VALUE);
        cbMarital.setMaxWidth(Double.MAX_VALUE);

        tab.setContent(buildTabScrollPane(grid));
        return tab;
    }

    private Tab tabEndereco() {
        Tab tab = new Tab("Endereço");
        tab.setGraphic(UiIcon.get(UiIcon.HOME, 14, "-color-fg-muted"));
        GridPane grid = formGrid();

        tfZip = field();
        tfStreet = field();
        tfNumber = field();
        tfComplement = field();
        tfNeighborhood = field();
        tfCity = field();
        tfState = field();

        int row = 0;
        addRow(grid, row++, "CEP", tfZip, "Rua / Logradouro", tfStreet);
        addRow(grid, row++, "Número", tfNumber, "Complemento", tfComplement);
        addRow(grid, row++, "Bairro", tfNeighborhood, "Cidade", tfCity);
        addRow(grid, row++, "Estado (UF)", tfState, "", new Label(""));

        tab.setContent(buildTabScrollPane(grid));
        return tab;
    }

    private Tab tabProfissional() {
        Tab tab = new Tab("Profissional");
        tab.setGraphic(UiIcon.get(UiIcon.BRIEFCASE, 14, "-color-fg-muted"));
        GridPane grid = formGrid();

        dpHireDate = new DatePicker();
        dpTermination = new DatePicker();
        cbStatus = new ComboBox<>(FXCollections.observableArrayList(EmployeeStatus.values()));
        cbContract = new ComboBox<>(FXCollections.observableArrayList(ContractType.values()));
        tfSalary = field();

        UnaryOperator<TextFormatter.Change> decimalFilter = change -> {
            String newText = change.getControlNewText();
            String regex = "^(\\d*(\\.\\d*)?(,\\d*)?|\\d*(,\\d*)?(\\.\\d*)?)$";

            if (newText.isEmpty() || newText.matches(regex)) {
                return change;
            }

            return null;
        };
        tfSalary.setTextFormatter(new TextFormatter<>(decimalFilter));

        tfHours = field();

        List<com.erp.gestaofuncionarios.model.Department> depts;
        List<Position> positions;
        try {
            depts = depDao.findAll();
        } catch (Exception e) {
            depts = List.of();
        }
        try {
            positions = posDao.findAll();
        } catch (Exception e) {
            positions = List.of();
        }

        cbDept = new ComboBox<>(FXCollections.observableArrayList(depts));
        cbPosition = new ComboBox<>(FXCollections.observableArrayList(positions));
        cbStatus.setMaxWidth(Double.MAX_VALUE);
        cbContract.setMaxWidth(Double.MAX_VALUE);
        cbDept.setMaxWidth(Double.MAX_VALUE);
        cbPosition.setMaxWidth(Double.MAX_VALUE);

        taNotes = new TextArea();
        taNotes.setPromptText("Observações sobre o funcionário...");
        taNotes.setPrefRowCount(3);

        int row = 0;
        addRow(grid, row++, "Data de Admissão *", dpHireDate, "Status *", cbStatus);
        addRow(grid, row++, "Tipo de Contrato *", cbContract, "Carga Horária (h/sem)", tfHours);
        addRow(grid, row++, "Departamento *", cbDept, "Cargo *", cbPosition);
        addRow(grid, row++, "Salário (R$) *", tfSalary, "Data de Demissão", dpTermination);
        grid.add(new Label("Observações"), 0, row);
        grid.add(taNotes, 1, row, 3, 1);

        tab.setContent(buildTabScrollPane(grid));
        return tab;
    }

    private Tab tabBancario() {
        Tab tab = new Tab("Dados Bancários");
        tab.setGraphic(UiIcon.get(UiIcon.CREDIT_CARD, 14, "-color-fg-muted"));
        GridPane grid = formGrid();

        tfBank = field();
        tfAgency = field();
        tfAccount = field();
        tfAccountType = field();
        tfPix = field();
        tfAccountType.setPromptText("Corrente / Poupança");

        int row = 0;
        addRow(grid, row++, "Banco", tfBank, "Agência", tfAgency);
        addRow(grid, row++, "Conta", tfAccount, "Tipo de Conta", tfAccountType);
        addRow(grid, row++, "Chave PIX", tfPix, "", new Label(""));

        tab.setContent(buildTabScrollPane(grid));
        return tab;
    }

    private Tab tabEmergencia() {
        Tab tab = new Tab("Emergência");
        tab.setGraphic(UiIcon.get(UiIcon.ALERT_CIRCLE, 14, "-color-fg-muted"));
        GridPane grid = formGrid();

        tfEmergName = field();
        tfEmergPhone = field();
        tfEmergRel = field();
        tfEmergRel.setPromptText("Ex: Cônjuge, Pai, Mãe...");

        int row = 0;
        addRow(grid, row++, "Nome do Contato", tfEmergName, "", new Label(""));
        addRow(grid, row++, "Telefone", tfEmergPhone, "", new Label(""));
        addRow(grid, row++, "Parentesco", tfEmergRel, "", new Label(""));

        tab.setContent(buildTabScrollPane(grid));
        return tab;
    }

    private Tab tabNotes() {
        Tab tab = new Tab("Anotações");
        tab.setGraphic(UiIcon.get(UiIcon.FILE_TEXT, 14, "-color-fg-muted"));
        taNotes = new TextArea();
        taNotes.setPromptText("Anotações gerais...");
        VBox box = new VBox(taNotes);
        VBox.setVgrow(taNotes, Priority.ALWAYS);
        box.setPadding(new Insets(16));
        tab.setContent(box);
        return tab;
    }

    private Tab tabDocumentos() {
        Tab tab = new Tab("Documentação");
        tab.setGraphic(UiIcon.get(UiIcon.FOLDER, 14, "-color-fg-muted"));
        VBox box = new VBox(14);
        box.setPadding(new Insets(24));

        Label lblTitle = new Label("Checklist de Documentos de Admissão");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e0e0e0;");

        cbDocCpf = new CheckBox("Cópia do CPF");
        cbDocRg = new CheckBox("Cópia do RG");
        cbDocWorkCard = new CheckBox("Carteira de Trabalho (CTPS)");
        cbDocAddress = new CheckBox("Comprovante de Residência");
        cbDocAso = new CheckBox("ASO (Atestado de Saúde Ocupacional)");
        cbDocPhoto = new CheckBox("Foto 3x4");

        box.getChildren().addAll(lblTitle, cbDocCpf, cbDocRg, cbDocWorkCard, cbDocAddress, cbDocAso, cbDocPhoto);
        tab.setContent(buildTabScrollPane(box));
        return tab;
    }

    private HBox buildFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 12, 20));
        footer.getStyleClass().add("form-footer");

        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().add("button");
        btnCancel.setOnAction(e -> stage.close());

        Button btnSave = new Button(employee == null ? "Salvar" : "Atualizar");
        btnSave.getStyleClass().addAll("button", "accent");
        btnSave.setOnAction(e -> save());

        footer.getChildren().addAll(btnCancel, btnSave);
        return footer;
    }

    private void populate() {
        tfFirstName.setText(nullSafe(employee.getFirstName()));
        tfLastName.setText(nullSafe(employee.getLastName()));
        tfCpf.setText(nullSafe(employee.getCpf()));
        tfRg.setText(nullSafe(employee.getRg()));
        tfPis.setText(nullSafe(employee.getPisPasep()));
        tfEmail.setText(nullSafe(employee.getEmail()));
        tfPhone.setText(nullSafe(employee.getPhone()));
        tfMobile.setText(nullSafe(employee.getMobilePhone()));
        tfNationality.setText(nullSafe(employee.getNationality()));
        dpBirthDate.setValue(employee.getBirthDate());
        cbGender.setValue(employee.getGender());
        cbMarital.setValue(employee.getMaritalStatus());

        Address addr = employee.getAddress();
        tfStreet.setText(nullSafe(addr.getStreet()));
        tfNumber.setText(nullSafe(addr.getNumber()));
        tfComplement.setText(nullSafe(addr.getComplement()));
        tfNeighborhood.setText(nullSafe(addr.getNeighborhood()));
        tfCity.setText(nullSafe(addr.getCity()));
        tfState.setText(nullSafe(addr.getState()));
        tfZip.setText(nullSafe(addr.getZipCode()));

        dpHireDate.setValue(employee.getHireDate());
        dpTermination.setValue(employee.getTerminationDate());
        cbStatus.setValue(employee.getStatus());
        cbContract.setValue(employee.getContractType());
        cbDept.setValue(employee.getDepartment());
        cbPosition.setValue(employee.getPosition());
        tfSalary.setText(employee.getSalary() != null ? employee.getSalary().toPlainString() : "");
        tfHours.setText(employee.getWeeklyHours() != null ? employee.getWeeklyHours().toString() : "");
        taNotes.setText(nullSafe(employee.getNotes()));

        BankInfo bank = employee.getBankInfo();
        tfBank.setText(nullSafe(bank.getBankName()));
        tfAgency.setText(nullSafe(bank.getAgency()));
        tfAccount.setText(nullSafe(bank.getAccount()));
        tfAccountType.setText(nullSafe(bank.getAccountType()));
        tfPix.setText(nullSafe(bank.getPixKey()));

        EmergencyContact ec = employee.getEmergencyContact();
        tfEmergName.setText(nullSafe(ec.getName()));
        tfEmergPhone.setText(nullSafe(ec.getPhone()));
        tfEmergRel.setText(nullSafe(ec.getRelationship()));

        DocumentChecklist docs = employee.getDocuments();
        cbDocCpf.setSelected(Boolean.TRUE.equals(docs.getCpf()));
        cbDocRg.setSelected(Boolean.TRUE.equals(docs.getRg()));
        cbDocWorkCard.setSelected(Boolean.TRUE.equals(docs.getWorkCard()));
        cbDocAddress.setSelected(Boolean.TRUE.equals(docs.getAddress()));
        cbDocAso.setSelected(Boolean.TRUE.equals(docs.getAso()));
        cbDocPhoto.setSelected(Boolean.TRUE.equals(docs.getPhoto()));
    }

    private void save() {
        StringBuilder errors = new StringBuilder();
        if (tfFirstName.getText().isBlank())
            errors.append("- Nome\n");
        if (tfLastName.getText().isBlank())
            errors.append("- Sobrenome\n");

        if (tfCpf.getText().isBlank()) {
            errors.append("- CPF\n");
        } else if (!ValidationUtil.isValidCpf(tfCpf.getText())) {
            errors.append("- CPF inválido\n");
        }

        if (tfEmail.getText().isBlank()) {
            errors.append("- E-mail\n");
        } else if (!ValidationUtil.isValidEmail(tfEmail.getText())) {
            errors.append("- E-mail inválido\n");
        }

        if (dpHireDate.getValue() == null)
            errors.append("- Data de Admissão\n");
        if (cbStatus.getValue() == null)
            errors.append("- Status\n");
        if (cbContract.getValue() == null)
            errors.append("- Tipo de Contrato\n");
        if (cbDept.getValue() == null)
            errors.append("- Departamento\n");
        if (cbPosition.getValue() == null)
            errors.append("- Cargo\n");

        BigDecimal salVal = BigDecimal.ZERO;
        if (tfSalary.getText().isBlank()) {
            errors.append("- Salário\n");
        } else {
            try {
                salVal = ValidationUtil.parseSalary(tfSalary.getText());
                if (salVal.compareTo(BigDecimal.ZERO) < 0) {
                    errors.append("- Salário (deve ser maior ou igual a zero)\n");
                }
            } catch (Exception ex) {
                errors.append("- Salário (Valor numérico inválido)\n");
            }
        }

        if (errors.length() > 0) {
            showError("Por favor, preencha todos os campos obrigatórios:\n" + errors.toString());
            return;
        }

        Employee emp = employee != null ? employee : new Employee();
        emp.setFirstName(tfFirstName.getText().trim());
        emp.setLastName(tfLastName.getText().trim());
        emp.setCpf(tfCpf.getText().trim());
        emp.setRg(tfRg.getText().trim());
        emp.setPisPasep(tfPis.getText().trim());
        emp.setEmail(tfEmail.getText().trim());
        emp.setPhone(tfPhone.getText().trim());
        emp.setMobilePhone(tfMobile.getText().trim());
        emp.setNationality(tfNationality.getText().trim());
        emp.setBirthDate(dpBirthDate.getValue());
        emp.setGender(cbGender.getValue());
        emp.setMaritalStatus(cbMarital.getValue());

        Address addr = emp.getAddress();
        addr.setStreet(tfStreet.getText().trim());
        addr.setNumber(tfNumber.getText().trim());
        addr.setComplement(tfComplement.getText().trim());
        addr.setNeighborhood(tfNeighborhood.getText().trim());
        addr.setCity(tfCity.getText().trim());
        addr.setState(tfState.getText().trim());
        addr.setZipCode(tfZip.getText().trim());

        emp.setHireDate(dpHireDate.getValue());
        emp.setTerminationDate(dpTermination.getValue());
        emp.setStatus(cbStatus.getValue());
        emp.setContractType(cbContract.getValue());
        emp.setDepartment(cbDept.getValue());
        emp.setPosition(cbPosition.getValue());
        emp.setNotes(taNotes.getText().trim());
        emp.setSalary(salVal);

        try {
            emp.setWeeklyHours(Integer.parseInt(tfHours.getText().trim()));
        } catch (Exception e) {
            emp.setWeeklyHours(40);
        }

        DocumentChecklist docs = emp.getDocuments();
        docs.setCpf(cbDocCpf.isSelected());
        docs.setRg(cbDocRg.isSelected());
        docs.setWorkCard(cbDocWorkCard.isSelected());
        docs.setAddress(cbDocAddress.isSelected());
        docs.setAso(cbDocAso.isSelected());
        docs.setPhoto(cbDocPhoto.isSelected());

        BankInfo bank = emp.getBankInfo();
        bank.setBankName(tfBank.getText().trim());
        bank.setAgency(tfAgency.getText().trim());
        bank.setAccount(tfAccount.getText().trim());
        bank.setAccountType(tfAccountType.getText().trim());
        bank.setPixKey(tfPix.getText().trim());

        EmergencyContact ec = emp.getEmergencyContact();
        ec.setName(tfEmergName.getText().trim());
        ec.setPhone(tfEmergPhone.getText().trim());
        ec.setRelationship(tfEmergRel.getText().trim());

        if (emp.getRegistrationNumber() == null || emp.getRegistrationNumber().isBlank()) {
            emp.setRegistrationNumber(String.format("EMP%04d", (int) (Math.random() * 9000) + 1000));
        }

        try {
            if (employee == null) {
                empDao.save(emp);
            }

            stage.close();
            if (onSaved != null)
                onSaved.run();
        } catch (Exception e) {
            showError("Erro ao salvar: " + e.getMessage());
        }
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(18);
        grid.setPadding(new Insets(24));

        ColumnConstraints c1 = new ColumnConstraints(140);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);

        ColumnConstraints c3 = new ColumnConstraints(140);
        ColumnConstraints c4 = new ColumnConstraints();
        c4.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(c1, c2, c3, c4);
        return grid;
    }

    private void addRow(GridPane grid, int row, String lbl1, Node ctrl1,
            String lbl2, Node ctrl2) {
        Label l1 = new Label(lbl1);
        l1.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12px;");
        Label l2 = new Label(lbl2);
        l2.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 12px;");

        if (ctrl1 instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(r, true);
        }

        if (ctrl2 instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(r, true);
        }

        grid.add(l1, 0, row);
        grid.add(ctrl1, 1, row);
        if (!lbl2.isBlank()) {
            grid.add(l2, 2, row);
            grid.add(ctrl2, 3, row);
        }
    }

    private ScrollPane buildTabScrollPane(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("edge-to-edge");
        sp.setStyle("-fx-background-color: transparent;");
        return sp;
    }

    private TextField field() {
        TextField tf = new TextField();
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private void showError(String msg) {
        UiHelper.showError(stage, msg);
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }
}
