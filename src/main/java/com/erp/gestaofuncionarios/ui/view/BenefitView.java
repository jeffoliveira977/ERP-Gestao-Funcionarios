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

import com.erp.gestaofuncionarios.dao.BenefitDao;
import com.erp.gestaofuncionarios.model.Benefit;
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
import java.util.function.Function;

public class BenefitView {

    private final BenefitDao dao = new BenefitDao();
    private TableView<Benefit> table;

    public Node build() {
        VBox root = new VBox(16);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(2,
                lbl("Benefícios", "section-title"),
                lbl("Catálogo de benefícios oferecidos pela empresa", "section-subtitle"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnNew = new Button("Novo Benefício");
        btnNew.getStyleClass().addAll("button", "accent");

        StackPane plusIcon = UiIcon.get(UiIcon.PLUS, 13, "#ffffff");
        btnNew.setGraphic(plusIcon);
        btnNew.setOnAction(e -> showForm(null));
        header.getChildren().addAll(titleBlock, spacer, btnNew);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nenhum benefício cadastrado."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Benefit, String> colName = col("Nome", 200, b -> b.getName());
        com.erp.gestaofuncionarios.ui.util.UiHelper.makeColumnBold(colName);

        TableColumn<Benefit, String> colDesc = col("Descrição", 280,
                b -> b.getDescription() != null ? b.getDescription() : "—");
        TableColumn<Benefit, String> colDefaultValue = col("Valor Padrão (R$)", 150, b -> b.getDefaultValue() != null
                ? "R$ " + String.format("%,.2f", b.getDefaultValue()).replace(",", "X").replace(".", ",").replace("X",
                        ".")
                : "—");

        TableColumn<Benefit, Void> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(90);
        colStatus.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                boolean active = getTableRow().getItem().getActive();

                Label lbl = UiHelper.createBadge(
                        active ? "Ativo" : "Inativo",
                        active ? "badge-success" : "badge-warning");
                setGraphic(lbl);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        TableColumn<Benefit, Void> colActions = new TableColumn<>("Ações");
        colActions.setPrefWidth(110);
        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button edit = btn(UiIcon.EDIT, "action-button", "-color-fg-default");
            private final Button del = btn(UiIcon.TRASH, "action-button-danger", "#bf616a");
            {
                edit.setOnAction(e -> {
                    Benefit b = getTableRow().getItem();
                    if (b != null)
                        showForm(b);
                });
                del.setOnAction(e -> {
                    Benefit b = getTableRow().getItem();
                    if (b != null)
                        confirmDelete(b);
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

        table.getColumns().addAll(Arrays.asList(colName, colDesc, colDefaultValue, colStatus, colActions));
        refresh();
        root.getChildren().addAll(header, table);
        return root;
    }

    private void showForm(Benefit benefit) {
        Stage owner = (Stage) table.getScene().getWindow();
        Stage formStage = new Stage();

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-min-width: 400px; -fx-min-height: 250px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(130);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        TextField tfName = new TextField(benefit != null ? benefit.getName() : "");
        tfName.setMaxWidth(Double.MAX_VALUE);
        TextField tfDesc = new TextField(
                benefit != null && benefit.getDescription() != null ? benefit.getDescription() : "");
        tfDesc.setMaxWidth(Double.MAX_VALUE);
        TextField tfVal = new TextField(
                benefit != null && benefit.getDefaultValue() != null ? benefit.getDefaultValue().toPlainString() : "");
        tfVal.setMaxWidth(Double.MAX_VALUE);
        CheckBox cbActive = new CheckBox("Ativo");
        cbActive.setSelected(benefit == null || benefit.getActive());

        form.addRow(0, new Label("Nome *"), tfName);
        form.addRow(1, new Label("Descrição"), tfDesc);
        form.addRow(2, new Label("Valor Padrão"), tfVal);
        form.addRow(3, new Label(""), cbActive);

        Button btnSave = new Button("Salvar");
        btnSave.getStyleClass().add("accent");
        btnSave.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            if (tfName.getText().isBlank()) {
                UiHelper.showError(owner, "O nome do benefício é obrigatório.");
                return;
            }

            Benefit b = benefit != null ? benefit : new Benefit();
            b.setName(tfName.getText().trim());
            b.setDescription(tfDesc.getText().trim());
            try {
                b.setDefaultValue(new BigDecimal(tfVal.getText().replace(",", ".")));
            } catch (Exception ex) {
                b.setDefaultValue(BigDecimal.ZERO);
            }
            b.setActive(cbActive.isSelected());

            try {
                if (benefit == null)
                    dao.save(b);
                else
                    dao.update(b);
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
                benefit == null ? "Novo Benefício" : "Editar Benefício",
                root,
                false,
                false,
                javafx.stage.Modality.APPLICATION_MODAL,
                owner);

        formStage.showAndWait();
    }

    private void confirmDelete(Benefit b) {
        Stage owner = (Stage) table.getScene().getWindow();
        UiHelper.showConfirm(owner, "Confirmar Exclusão", "Remover benefício " + b.getName() + "?", () -> {
            dao.delete(b);
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

    private TableColumn<Benefit, String> col(String t, int w, Function<Benefit, String> fn) {
        TableColumn<Benefit, String> c = new TableColumn<>(t);
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