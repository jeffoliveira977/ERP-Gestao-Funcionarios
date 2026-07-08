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
import com.erp.gestaofuncionarios.model.*;
import com.erp.gestaofuncionarios.ui.util.UiHelper;
import com.erp.gestaofuncionarios.ui.util.UiIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import atlantafx.base.controls.CustomTextField;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class EmployeeListView {

    private final EmployeeDao empDao = new EmployeeDao();
    private TableView<Employee> table;
    private ObservableList<Employee> data;
    private FilteredList<Employee> filtered;

    public Node build() {
        VBox root = new VBox(16);
        root.setFillWidth(true);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBlock = new VBox(2);
        Label title = new Label("Funcionários");
        title.getStyleClass().add("section-title");
        Label sub = new Label("Cadastro completo de colaboradores");
        sub.getStyleClass().add("section-subtitle");
        titleBlock.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        CustomTextField search = new CustomTextField();
        search.setPromptText("Buscar funcionário...");
        search.getStyleClass().add("search-field");
        search.setLeft(UiIcon.get(UiIcon.SEARCH, 14, "-color-fg-muted"));
        search.setPrefWidth(280);

        Button btnNew = new Button("Novo Funcionário");
        btnNew.getStyleClass().addAll("button", "accent");

        StackPane addIcon = UiIcon.get(UiIcon.PLUS, 13, "#ffffff");
        btnNew.setGraphic(addIcon);
        btnNew.setOnAction(e -> openForm(null));

        ComboBox<String> filterCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Todos", "Ativos", "Inativos", "CLT", "PJ"));
        filterCombo.setValue("Todos");
        filterCombo.setPrefWidth(120);
        filterCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null)
                return;
            switch (newV) {
                case "Todos" -> applyFilter(null, null);
                case "Ativos" -> applyFilter(EmployeeStatus.ACTIVE, null);
                case "Inativos" -> applyFilter(EmployeeStatus.INACTIVE, null);
                case "CLT" -> applyFilter(null, ContractType.CLT);
                case "PJ" -> applyFilter(null, ContractType.PJ);
            }
        });

        header.getChildren().addAll(titleBlock, spacer, search, filterCombo, btnNew);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        refresh();

        data = FXCollections.observableArrayList(loadData());
        filtered = new FilteredList<>(data, p -> true);
        search.textProperty().addListener((obs, oldV, newV) -> {
            filtered.setPredicate(emp -> {

                if (newV == null || newV.isBlank())
                    return true;

                String lower = newV.toLowerCase();
                return emp.getFullName().toLowerCase().contains(lower)
                        || (emp.getCpf() != null && emp.getCpf().contains(lower))
                        || (emp.getEmail() != null && emp.getEmail().toLowerCase().contains(lower))
                        || (emp.getDepartment() != null && emp.getDepartment().getName().toLowerCase().contains(lower));
            });
            table.setItems(filtered);
        });

        table.setItems(data);

        root.getChildren().addAll(header, table);
        return root;
    }

    private TableView<Employee> buildTable() {
        TableView<Employee> tv = new TableView<>();
        tv.setPlaceholder(new Label("Nenhum funcionário encontrado."));
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tv.setRowFactory(tv2 -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 2) {
                    Employee emp = row.getItem();
                    if (emp != null)
                        openForm(emp);
                }
            });
            row.setStyle("-fx-cursor: hand;");
            return row;
        });

        TableColumn<Employee, Void> colName = new TableColumn<>("Funcionário");
        colName.setPrefWidth(220);
        colName.setCellFactory(col -> new TableCell<>() {
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

        TableColumn<Employee, String> colDept = tableCol("Departamento", 180,
                e -> e.getDepartment() != null ? e.getDepartment().getName() : "—");

        TableColumn<Employee, String> colPos = tableCol("Cargo", 180,
                e -> e.getPosition() != null ? e.getPosition().getName() : "—");

        TableColumn<Employee, String> colContract = tableCol("Contrato", 100,
                e -> e.getContractType() != null ? e.getContractType().name() : "—");

        TableColumn<Employee, String> colSalary = tableCol("Salário", 120,
                e -> e.getSalary() != null
                        ? "R$ " + String.format("%,.2f", e.getSalary()).replace(",", "X").replace(".", ",").replace("X",
                                ".")
                        : "—");

        TableColumn<Employee, String> colHire = tableCol("Admissão", 100,
                e -> e.getHireDate() != null ? e.getHireDate().toString() : "—");

        TableColumn<Employee, Void> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Employee e = getTableRow().getItem();

                setGraphic(UiHelper.buildStatusBadge(e.getStatus()));
                setAlignment(Pos.CENTER);
            }
        });

        tv.getColumns()
                .addAll(Arrays.asList(colName, colDept, colPos, colContract, colSalary, colHire, colStatus));
        return tv;
    }

    private TableColumn<Employee, String> tableCol(String header, int width,
            Function<Employee, String> extractor) {
        TableColumn<Employee, String> col = new TableColumn<>(header);
        col.setPrefWidth(width);
        col.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        return col;
    }

    private void applyFilter(EmployeeStatus status, ContractType contract) {
        filtered.setPredicate(emp -> {
            if (status != null && emp.getStatus() != status)
                return false;
            if (contract != null && emp.getContractType() != contract)
                return false;
            return true;
        });
        table.setItems(filtered);
    }

    private void openForm(Employee employee) {
        EmployeeFormView form = new EmployeeFormView(employee, () -> refresh());
        form.showDialog();
    }

    private void refresh() {
        List<Employee> list = loadData();
        if (data != null) {
            data.setAll(list);
        }
    }

    private List<Employee> loadData() {
        try {
            return empDao.findAll();
        } catch (Exception e) {
            return List.of();
        }
    }

}
