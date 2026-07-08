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

import com.erp.gestaofuncionarios.ui.util.UiIcon;
import com.erp.gestaofuncionarios.ui.view.*;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;

public class MainWindow {

    private final javafx.stage.Stage stage;
    private BorderPane root;
    private VBox sidebar;
    private HBox topbar;
    private StackPane contentArea;

    private Button activeButton;

    private static boolean isDark = true;

    public static boolean isDarkTheme() {
        return isDark;
    }

    public MainWindow(javafx.stage.Stage stage) {
        this.stage = stage;
    }

    private double xOffset = 0;
    private double yOffset = 0;

    public Scene buildScene() {
        root = new BorderPane();
        root.getStyleClass().add("theme-dark");
        root.setStyle(
                "-fx-border-color: -color-border-default; -fx-border-width: 1px; -fx-background-color: -color-bg-default;");

        sidebar = buildSidebar();
        topbar = buildTopbar();

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

        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        Node dash = buildView("Dashboard");
        contentArea.getChildren().setAll(dash);

        root.setLeft(sidebar);
        root.setTop(topbar);
        root.setCenter(contentArea);

        if (sidebar.getChildren().size() > 2) {
            for (Node node : sidebar.getChildren()) {
                if (node instanceof Button btn && "Dashboard".equals(btn.getText())) {
                    activeButton = btn;
                    btn.getStyleClass().add("nav-button-active");

                    if (btn.getGraphic() instanceof StackPane sp) {
                        if (sp.getChildren().get(0) instanceof SVGPath svg) {
                            svg.setStyle("-fx-stroke: #ffffff;");
                        }
                    }
                    break;
                }
            }
        }

        Scene scene = new Scene(root, 1280, 800);
        javafx.application.Platform.runLater(() -> {
            makeResizable(stage, scene);
        });
        return scene;
    }

    private void makeResizable(javafx.stage.Stage stage, Scene scene) {
        scene.setOnMouseMoved(e -> {
            Boolean isMaximized = (Boolean) stage.getProperties().getOrDefault("custom-maximized", Boolean.FALSE);
            if (isMaximized) {
                scene.setCursor(javafx.scene.Cursor.DEFAULT);
                return;
            }
            double x = e.getSceneX();
            double y = e.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();
            javafx.scene.Cursor cursor = javafx.scene.Cursor.DEFAULT;

            boolean nearRight = x > width - 5;
            boolean nearBottom = y > height - 5;

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
            Boolean isMaximized = (Boolean) stage.getProperties().getOrDefault("custom-maximized", Boolean.FALSE);
            if (isMaximized)
                return;

            double x = e.getSceneX();
            double y = e.getSceneY();
            double screenX = e.getScreenX();
            double screenY = e.getScreenY();
            double width = scene.getWidth();
            double height = scene.getHeight();

            boolean nearRight = x > width - 15;
            boolean nearBottom = y > height - 15;

            if (nearRight && nearBottom) {
                stage.setWidth(Math.max(1000, screenX - stage.getX()));
                stage.setHeight(Math.max(640, screenY - stage.getY()));
            } else if (nearRight) {
                stage.setWidth(Math.max(1000, screenX - stage.getX()));
            } else if (nearBottom) {
                stage.setHeight(Math.max(640, screenY - stage.getY()));
            }
        });
    }

    private VBox buildSidebar() {
        VBox sb = new VBox();
        sb.getStyleClass().add("sidebar");

        sb.getChildren().add(sectionLabel("Principal"));

        Button btnDash = navButton("Dashboard", UiIcon.GRID, "Dashboard");
        Button btnEmp = navButton("Funcionários", UiIcon.USERS, "Funcionários");

        activeButton = btnDash;

        sb.getChildren().addAll(btnDash, btnEmp);

        // HR Section
        sb.getChildren().add(sectionLabel("Recursos Humanos"));
        Button btnDep = navButton("Departamentos", UiIcon.LAYERS, "Departamentos");
        Button btnPos = navButton("Cargos", UiIcon.BRIEFCASE, "Cargos");
        Button btnVac = navButton("Férias", UiIcon.UMBRELLA, "Férias");
        Button btnBen = navButton("Benefícios", UiIcon.HEART, "Benefícios");
        Button btnTrain = navButton("Treinamentos", UiIcon.BOOK_OPEN, "Treinamentos");
        Button btnAbs = navButton("Ausências", UiIcon.USERS, "Ausências");
        sb.getChildren().addAll(btnDep, btnPos, btnVac, btnBen, btnTrain, btnAbs);

        // DP Section
        sb.getChildren().add(sectionLabel("Departamento Pessoal"));
        Button btnTime = navButton("Ponto", UiIcon.CLOCK, "Ponto");
        Button btnPay = navButton("Folha de Pag.", UiIcon.DOLLAR, "Folha");
        Button btnTerm = navButton("Rescisão", UiIcon.TRASH, "Rescisão");
        sb.getChildren().addAll(btnTime, btnPay, btnTerm);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sb.getChildren().add(spacer);

        return sb;
    }

    private HBox buildTopbar() {
        HBox tb = new HBox();
        tb.getStyleClass().add("topbar");
        tb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        tb.setSpacing(15);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnTheme = new Button();
        btnTheme.getStyleClass().addAll("button", "flat");
        btnTheme.setTooltip(new Tooltip("Alternar Tema (Light/Dark)"));
        updateThemeButtonGraphic(btnTheme);
        btnTheme.setOnAction(e -> {
            toggleTheme();
            updateThemeButtonGraphic(btnTheme);
        });

        StackPane userIcon = UiIcon.get(UiIcon.USER, 18, "-color-fg-muted");
        Label userLabel = new Label("Administrador");
        userLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox brand = new HBox(10);
        brand.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        StackPane brandIcon = UiIcon.get(UiIcon.SHIELD, 18, "-color-accent-emphasis");
        Label brandLabel = new Label("Gestão de Funcionários");
        brand.getChildren().addAll(brandIcon, brandLabel);

        Button btnMinimize = new Button("—");
        btnMinimize.getStyleClass().addAll("button", "flat");
        btnMinimize
                .setStyle("-fx-font-size: 10px; -fx-padding: 6 10; -fx-cursor: hand; -fx-text-fill: -color-fg-muted;");
        btnMinimize.setOnAction(e -> {
            if (stage != null)
                stage.setIconified(true);
        });

        Button btnMaximize = new Button("⬜");
        btnMaximize.getStyleClass().addAll("button", "flat");
        btnMaximize
                .setStyle("-fx-font-size: 9px; -fx-padding: 6 10; -fx-cursor: hand; -fx-text-fill: -color-fg-muted;");
        btnMaximize.setOnAction(e -> {
            if (stage != null) {
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
            }
        });

        Button btnClose = new Button("✕");
        btnClose.getStyleClass().addAll("button", "flat");
        btnClose.setStyle("-fx-font-size: 11px; -fx-padding: 6 10; -fx-cursor: hand; -fx-text-fill: -color-fg-muted;");
        btnClose.setOnMouseEntered(evt -> btnClose.setStyle(
                "-fx-font-size: 11px; -fx-padding: 6 10; -fx-cursor: hand; -fx-background-color: #bf616a; -fx-text-fill: white;"));
        btnClose.setOnMouseExited(evt -> btnClose
                .setStyle("-fx-font-size: 11px; -fx-padding: 6 10; -fx-cursor: hand; -fx-text-fill: -color-fg-muted;"));
        btnClose.setOnAction(e -> javafx.application.Platform.exit());

        HBox winControls = new HBox(2, btnMinimize, btnMaximize, btnClose);
        winControls.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        tb.getChildren().addAll(brand, spacer, btnTheme, userIcon, userLabel, winControls);
        return tb;
    }

    private void toggleTheme() {
        isDark = !isDark;
        if (isDark) {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.NordDark().getUserAgentStylesheet());
            root.getStyleClass().remove("theme-light");
            root.getStyleClass().add("theme-dark");
        } else {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.NordLight().getUserAgentStylesheet());
            root.getStyleClass().remove("theme-dark");
            root.getStyleClass().add("theme-light");
        }
    }

    private void updateThemeButtonGraphic(Button btn) {
        String iconPath = isDark ? UiIcon.SUN : UiIcon.MOON;
        btn.setGraphic(UiIcon.get(iconPath, 16, "-color-fg-default"));
    }

    private Button navButton(String label, String iconData, String viewName) {
        StackPane icon = UiIcon.get(iconData, 16, "-color-fg-muted");

        Button btn = new Button(label, icon);
        btn.getStyleClass().add("nav-button");

        btn.setOnAction(e -> {
            Node view = buildView(viewName);
            navigateTo(viewName, view, btn);
        });

        return btn;
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("nav-section-label");
        return lbl;
    }

    private void navigateTo(String title, Node view, Button sourceBtn) {
        contentArea.getChildren().setAll(view);

        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");

            if (activeButton.getGraphic() instanceof StackPane sp) {
                if (sp.getChildren().get(0) instanceof SVGPath svg) {
                    svg.setStyle("-fx-stroke: -color-fg-muted;");
                }
            }
        }
        if (sourceBtn != null) {
            sourceBtn.getStyleClass().add("nav-button-active");

            if (sourceBtn.getGraphic() instanceof StackPane sp) {
                if (sp.getChildren().get(0) instanceof SVGPath svg) {
                    svg.setStyle("-fx-stroke: #ffffff;");
                }
            }
            activeButton = sourceBtn;
        }
    }

    private Node buildView(String name) {
        return switch (name) {
            case "Dashboard" -> new DashboardView().build();
            case "Funcionários" -> new EmployeeListView().build();
            case "Departamentos" -> new DepartmentView().build();
            case "Cargos" -> new PositionView().build();
            case "Férias" -> new VacationView().build();
            case "Benefícios" -> new BenefitView().build();
            case "Treinamentos" -> new TrainingView().build();
            case "Ponto" -> new TimeRecordView().build();
            case "Folha" -> new PayrollView().build();
            case "Ausências" -> new AbsenceView().build();
            case "Rescisão" -> new TerminationView().build();
            default -> new Label("View não encontrada: " + name);
        };
    }
}
