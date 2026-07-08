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

package com.erp.gestaofuncionarios;

import atlantafx.base.theme.NordDark;
import com.erp.gestaofuncionarios.ui.LoginWindow;
import com.erp.gestaofuncionarios.util.DataSeeder;
import com.erp.gestaofuncionarios.util.JpaUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        try {
            DataSeeder.seed();
        } catch (Exception e) {
            System.err.println("[Database] Warning: failed to establish initial database communication. " +
                    "Make sure PostgreSQL is running. Error: " + e.getMessage());
        }

        primaryStage.initStyle(StageStyle.UNDECORATED);
        LoginWindow loginWindow = new LoginWindow(primaryStage);
        Scene scene = loginWindow.buildScene();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        JpaUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
