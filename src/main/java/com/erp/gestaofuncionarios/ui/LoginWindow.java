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

package com.erp.gestaofuncionarios.ui;

import com.erp.gestaofuncionarios.dao.UserAccountDao;
import com.erp.gestaofuncionarios.model.UserAccount;
import com.erp.gestaofuncionarios.ui.util.UiIcon;
import com.erp.gestaofuncionarios.util.SecurityUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginWindow {

    private final Stage loginStage;
    private final UserAccountDao userDao = new UserAccountDao();
    private double xOffset = 0;
    private double yOffset = 0;

    public LoginWindow(Stage stage) {
        this.loginStage = stage;
    }

    public Scene buildScene() {
        StackPane container = new StackPane();
        container.getStyleClass().add("theme-dark");
        container.setStyle(
                "-fx-border-color: -color-border-default; -fx-border-width: 1px; -fx-background-color: -color-bg-default;");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            loginStage.setX(event.getScreenX() - xOffset);
            loginStage.setY(event.getScreenY() - yOffset);
        });

        StackPane shield = UiIcon.get(UiIcon.SHIELD, 48, "#88c0d0");

        Label lblTitle = new Label("Gestão de Funcionários");
        lblTitle.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label lblSubtitle = new Label("Controle de Funcionários e Recursos Humanos");
        lblSubtitle.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");

        VBox header = new VBox(8, shield, lblTitle, lblSubtitle);
        header.setAlignment(Pos.CENTER);

        VBox form = new VBox(12);
        form.setPrefWidth(300);
        form.setMaxWidth(300);

        Label lblUser = new Label("Usuário");
        lblUser.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 12px;");
        TextField tfUser = new TextField();
        tfUser.setPromptText("Digite seu usuário...");

        Label lblPass = new Label("Senha");
        lblPass.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 12px;");
        PasswordField pfPass = new PasswordField();
        pfPass.setPromptText("Digite sua senha...");

        VBox fields = new VBox(6, lblUser, tfUser, lblPass, pfPass);

        Label lblError = new Label("");
        lblError.setStyle("-fx-text-fill: #cf6969; -fx-font-size: 12px; -fx-font-weight: bold;");
        lblError.setWrapText(true);
        lblError.setAlignment(Pos.CENTER);

        Button btnLogin = new Button("Entrar");
        btnLogin.getStyleClass().addAll("button", "accent");
        btnLogin.setMaxWidth(Double.MAX_VALUE);

        btnLogin.setOnAction(e -> {
            String username = tfUser.getText().trim();
            String password = pfPass.getText();

            if (username.isEmpty() || password.isEmpty()) {
                lblError.setText("Por favor, preencha todos os campos.");
                return;
            }

            try {
                UserAccount user = userDao.findByUsername(username);
                if (user != null && user.getPassword().equals(SecurityUtil.hashPassword(password))) {

                    Stage mainStage = new Stage();
                    mainStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
                    MainWindow mainWindow = new MainWindow(mainStage);
                    Scene mainScene = mainWindow.buildScene();
                    mainScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    mainStage.setScene(mainScene);

                    mainStage.setWidth(1280);
                    mainStage.setHeight(800);
                    mainStage.setMinWidth(1000);
                    mainStage.setMinHeight(640);
                    mainStage.show();

                    loginStage.close();
                } else {
                    lblError.setText("Usuário ou senha inválidos.");
                }
            } catch (Exception ex) {
                lblError.setText("Erro ao conectar ao banco de dados: " + ex.getMessage());
            }
        });

        tfUser.setOnAction(e -> btnLogin.fire());
        pfPass.setOnAction(e -> btnLogin.fire());

        form.getChildren().addAll(fields, lblError, btnLogin);
        root.getChildren().addAll(header, form);

        Button btnClose = new Button();
        btnClose.setGraphic(UiIcon.get(UiIcon.CLOSE, 12, "-color-fg-muted"));
        btnClose.getStyleClass().addAll("action-button");
        btnClose.setStyle("-fx-padding: 8; -fx-background-color: transparent; -fx-cursor: hand;");
        btnClose.setOnAction(e -> loginStage.close());
        StackPane.setAlignment(btnClose, Pos.TOP_RIGHT);
        StackPane.setMargin(btnClose, new Insets(10));

        container.getChildren().addAll(root, btnClose);

        Scene scene = new Scene(container, 400, 480);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        return scene;
    }
}
