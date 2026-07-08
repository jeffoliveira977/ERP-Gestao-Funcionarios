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

package com.erp.gestaofuncionarios.ui.util;

import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.EmployeeStatus;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;

public class UiHelper {

    private static double xOffset = 0;
    private static double yOffset = 0;

    public static void prepareCustomWindow(Stage stage, String title, Parent content, boolean showMinMax,
            boolean isResizable, Modality modality, Stage owner) {
        stage.initStyle(StageStyle.TRANSPARENT);
        if (modality != null) {
            stage.initModality(modality);
        }
        if (owner != null) {
            stage.initOwner(owner);
        }

        HBox topbar = new HBox();
        topbar.setStyle(
                "-fx-background-color: -color-bg-subtle; -fx-padding: 8 16; -fx-border-color: transparent transparent -color-border-default transparent; -fx-border-width: 0 0 1 0; -fx-background-radius: 6 6 0 0;");
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setSpacing(10);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controls = new HBox(2);
        controls.setAlignment(Pos.CENTER_LEFT);

        if (showMinMax) {
            Button btnMinimize = new Button("—");
            btnMinimize.getStyleClass().addAll("button", "flat");
            btnMinimize.setStyle(
                    "-fx-font-size: 10px; -fx-padding: 4 8; -fx-cursor: hand; -fx-text-fill: -color-fg-muted;");
            btnMinimize.setOnAction(e -> stage.setIconified(true));

            Button btnMaximize = new Button("⬜");
            btnMaximize.getStyleClass().addAll("button", "flat");
            btnMaximize.setStyle(
                    "-fx-font-size: 9px; -fx-padding: 4 8; -fx-cursor: hand; -fx-text-fill: -color-fg-muted;");
            btnMaximize.setOnAction(e -> {
                Boolean isMaximized = (Boolean) stage.getProperties().getOrDefault("custom-maximized", Boolean.FALSE);
                if (isMaximized) {
                    double prevX = (double) stage.getProperties().getOrDefault("prev-x", 100.0);
                    double prevY = (double) stage.getProperties().getOrDefault("prev-y", 100.0);
                    double prevW = (double) stage.getProperties().getOrDefault("prev-w", 1280.0);
                    double prevH = (double) stage.getProperties().getOrDefault("prev-h", 800.0);
                    stage.setX(prevX);
                    stage.setY(prevY);
                    stage.setWidth(prevW);
                    stage.setHeight(prevH);
                    stage.getProperties().put("custom-maximized", Boolean.FALSE);
                    btnMaximize.setText("⬜");
                } else {
                    stage.getProperties().put("prev-x", stage.getX());
                    stage.getProperties().put("prev-y", stage.getY());
                    stage.getProperties().put("prev-w", stage.getWidth());
                    stage.getProperties().put("prev-h", stage.getHeight());

                    javafx.stage.Screen screen = javafx.stage.Screen
                            .getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                            .get(0);
                    javafx.geometry.Rectangle2D visualBounds = screen.getVisualBounds();

                    stage.setX(visualBounds.getMinX());
                    stage.setY(visualBounds.getMinY());
                    stage.setWidth(visualBounds.getWidth());
                    stage.setHeight(visualBounds.getHeight());
                    stage.getProperties().put("custom-maximized", Boolean.TRUE);
                    btnMaximize.setText("🗗");
                }
            });

            controls.getChildren().addAll(btnMinimize, btnMaximize);
        }

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().addAll("button", "flat");
        btnClose.setStyle("-fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand; -fx-text-fill: -color-fg-muted;");
        btnClose.setOnMouseEntered(evt -> btnClose.setStyle(
                "-fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand; -fx-background-color: #bf616a; -fx-text-fill: white;"));
        btnClose.setOnMouseExited(evt -> btnClose
                .setStyle("-fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand; -fx-text-fill: -color-fg-muted;"));
        btnClose.setOnAction(e -> stage.close());

        controls.getChildren().add(btnClose);

        topbar.getChildren().addAll(titleLabel, spacer, controls);

        topbar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topbar.setOnMouseDragged(event -> {
            Boolean isMaximized = (Boolean) stage.getProperties().getOrDefault("custom-maximized", Boolean.FALSE);
            if (!isMaximized) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        BorderPane layout = new BorderPane();
        if (com.erp.gestaofuncionarios.ui.MainWindow.isDarkTheme()) {
            layout.getStyleClass().add("theme-dark");
        } else {
            layout.getStyleClass().add("theme-light");
        }
        layout.setStyle(
                "-fx-border-color: -color-border-default; -fx-border-width: 1px; -fx-background-color: -color-bg-default; -fx-background-radius: 6; -fx-border-radius: 6;");
        layout.setTop(topbar);
        layout.setCenter(content);

        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.45));
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(4);
        layout.setEffect(shadow);

        StackPane outerContainer = new StackPane(layout);
        outerContainer.setStyle("-fx-background-color: transparent; -fx-padding: 15;");

        Scene scene = new Scene(outerContainer);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(UiHelper.class.getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);

        if (isResizable) {
            javafx.application.Platform.runLater(() -> {
                scene.setOnMouseMoved(e -> {
                    Boolean isMaximized = (Boolean) stage.getProperties().getOrDefault("custom-maximized",
                            Boolean.FALSE);
                    if (isMaximized) {
                        scene.setCursor(javafx.scene.Cursor.DEFAULT);
                        return;
                    }
                    double mouseX = e.getSceneX();
                    double mouseY = e.getSceneY();
                    double layoutW = layout.getWidth();
                    double layoutH = layout.getHeight();
                    javafx.scene.Cursor cursor = javafx.scene.Cursor.DEFAULT;

                    boolean nearRight = mouseX > layoutW + 15 - 5 && mouseX < layoutW + 15 + 5;
                    boolean nearBottom = mouseY > layoutH + 15 - 5 && mouseY < layoutH + 15 + 5;

                    if (nearRight && nearBottom) {
                        cursor = javafx.scene.Cursor.SE_RESIZE;
                    } else if (nearRight) {
                        cursor = javafx.scene.Cursor.E_RESIZE;
                    } else if (nearBottom) {
                        cursor = javafx.scene.Cursor.S_RESIZE;
                    }
                    scene.setCursor(cursor);
                });

                scene.setOnMouseDragged(e -> {
                    Boolean isMaximized = (Boolean) stage.getProperties().getOrDefault("custom-maximized",
                            Boolean.FALSE);
                    if (isMaximized)
                        return;

                    double screenX = e.getScreenX();
                    double screenY = e.getScreenY();

                    double minW = stage.getMinWidth() > 0 ? stage.getMinWidth() : 300;
                    double minH = stage.getMinHeight() > 0 ? stage.getMinHeight() : 200;

                    boolean nearRight = scene.getCursor() == javafx.scene.Cursor.E_RESIZE
                            || scene.getCursor() == javafx.scene.Cursor.SE_RESIZE;
                    boolean nearBottom = scene.getCursor() == javafx.scene.Cursor.S_RESIZE
                            || scene.getCursor() == javafx.scene.Cursor.SE_RESIZE;

                    if (nearRight && nearBottom) {
                        stage.setWidth(Math.max(minW, screenX - stage.getX() + 15));
                        stage.setHeight(Math.max(minH, screenY - stage.getY() + 15));
                    } else if (nearRight) {
                        stage.setWidth(Math.max(minW, screenX - stage.getX() + 15));
                    } else if (nearBottom) {
                        stage.setHeight(Math.max(minH, screenY - stage.getY() + 15));
                    }
                });
            });
        }
    }

    public static <S> void makeColumnBold(TableColumn<S, String> col) {
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
                }
            }
        });
    }

    public static StackPane buildAvatar(String first, String last) {
        StackPane sp = new StackPane();
        sp.setMinSize(32, 32);
        sp.setPrefSize(32, 32);
        sp.setMaxSize(32, 32);
        sp.setStyle("-fx-background-color: -color-accent-subtle; -fx-background-radius: 50%;");
        sp.setTranslateY(-1);

        String firstChar = (first == null || first.isEmpty()) ? "" : String.valueOf(first.charAt(0));
        String lastChar = (last == null || last.isEmpty()) ? "" : String.valueOf(last.charAt(0));
        String initials = firstChar + lastChar;

        Label lbl = new Label(initials.toUpperCase());
        lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("-fx-text-fill: -color-accent-fg; -fx-font-weight: bold; -fx-font-size: 11px;");

        sp.getChildren().add(lbl);
        return sp;
    }

    public static Node buildEmployeeNameCell(Employee e) {
        if (e == null)
            return null;
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = buildAvatar(e.getFirstName(), e.getLastName());
        Label name = new Label(e.getFullName());
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        box.getChildren().addAll(avatar, name);
        return box;
    }

    public static Label createBadge(String text, String badgeStyleClass) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add(badgeStyleClass);
        lbl.setStyle("-fx-min-width: 95px; -fx-max-width: 95px; -fx-alignment: center;");
        return lbl;
    }

    public static Label buildStatusBadge(EmployeeStatus status) {
        if (status == null)
            return null;

        String styleClass = switch (status) {
            case ACTIVE -> "badge-success";
            case VACATION -> "badge-info";
            case LICENCE -> "badge-warning";
            case INACTIVE, OFF -> "badge-danger";
        };
        return createBadge(status.getDescription(), styleClass);
    }

    public static void showError(Stage owner, String msg) {
        Stage alertStage = new Stage();

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-min-width: 320px; -fx-min-height: 120px;");

        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 13px; -fx-text-alignment: center;");

        Button btnOk = new Button("OK");
        btnOk.getStyleClass().add("accent");
        btnOk.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnOk.setOnAction(e -> alertStage.close());

        root.getChildren().addAll(msgLabel, btnOk);

        prepareCustomWindow(alertStage, "Erro", root, false, false, Modality.APPLICATION_MODAL, owner);
        alertStage.showAndWait();
    }

    public static void showConfirm(Stage owner, String title, String msg, Runnable onYes) {
        Stage alertStage = new Stage();

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-min-width: 320px; -fx-min-height: 120px;");

        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 13px; -fx-text-alignment: center;");

        Button btnYes = new Button("Sim");
        btnYes.getStyleClass().add("accent");
        btnYes.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnYes.setOnAction(e -> {
            alertStage.close();
            onYes.run();
        });

        Button btnNo = new Button("Não");
        btnNo.getStyleClass().add("flat");
        btnNo.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnNo.setOnAction(e -> alertStage.close());

        HBox buttons = new HBox(10, btnYes, btnNo);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(msgLabel, buttons);

        prepareCustomWindow(alertStage, title, root, false, false, Modality.APPLICATION_MODAL, owner);
        alertStage.showAndWait();
    }

    public static void showInfo(Stage owner, String title, String msg) {
        Stage alertStage = new Stage();

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-min-width: 320px; -fx-min-height: 120px;");

        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 13px; -fx-text-alignment: center;");

        Button btnOk = new Button("OK");
        btnOk.getStyleClass().add("accent");
        btnOk.setStyle("-fx-min-width: 80px; -fx-cursor: hand;");
        btnOk.setOnAction(e -> alertStage.close());

        root.getChildren().addAll(msgLabel, btnOk);

        prepareCustomWindow(alertStage, title, root, false, false, Modality.APPLICATION_MODAL, owner);
        alertStage.showAndWait();
    }
}
