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
import com.erp.gestaofuncionarios.ui.util.UiIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardView {

    public Node build() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox container = new VBox(24);

        Label title = new Label("Painel de Controle");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Resumo operacional, indicadores de custos e distribuição de equipe");
        subtitle.getStyleClass().add("section-subtitle");
        VBox header = new VBox(2, title, subtitle);

        EmployeeDao empDao = new EmployeeDao();
        List<Employee> allEmployees;
        try {
            allEmployees = empDao.findAll();
        } catch (Exception e) {
            allEmployees = List.of();
        }

        long total = allEmployees.size();
        long ativos = allEmployees.stream().filter(emp -> emp.getStatus() == EmployeeStatus.ACTIVE).count();
        long inativos = total - ativos;
        BigDecimal folha = allEmployees.stream()
                .filter(emp -> emp.getStatus() == EmployeeStatus.ACTIVE && emp.getSalary() != null)
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // KPIs
        HBox kpis = new HBox(14);
        kpis.getChildren().addAll(
                kpiCard("Total de Funcionários", String.valueOf(total), "Cadastros gerais", UiIcon.USERS, "#8fbcbb"),
                kpiCard("Funcionários Ativos", String.valueOf(ativos), "Em atividade comercial", UiIcon.USER,
                        "#a3be8c"),
                kpiCard("Funcionários Inativos", String.valueOf(inativos), "Desligados ou suspensos", UiIcon.USER,
                        "#bf616a"),
                kpiCard("Custo Total da Folha",
                        "R$ " + String.format("%,.2f", folha).replace(",", "X").replace(".", ",").replace("X", "."),
                        "Total de salários ativos", UiIcon.DOLLAR, "#ebcb8b"));

        Map<String, Long> byDept = allEmployees.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(e -> e.getDepartment().getName(), Collectors.counting()));

        Map<String, Double> avgSalaryByDept = allEmployees.stream()
                .filter(e -> e.getDepartment() != null && e.getSalary() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getDepartment().getName(),
                        Collectors.averagingDouble(e -> e.getSalary().doubleValue())));

        // Charts Section
        HBox chartsRow = new HBox(16);
        HBox.setHgrow(chartsRow, Priority.ALWAYS);

        // Pie Chart
        VBox panePie = new VBox(8);
        panePie.getStyleClass().add("form-section");
        HBox.setHgrow(panePie, Priority.ALWAYS);
        Label lblPieTitle = new Label("Distribuição por Departamento");
        lblPieTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        PieChart pieChart = new PieChart();
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setMinHeight(280);
        pieChart.setPrefHeight(280);
        byDept.forEach((dept, count) -> pieChart.getData().add(new PieChart.Data(dept, count)));

        if (byDept.isEmpty()) {
            Label noData = new Label("Nenhum dado departamental cadastrado.");
            noData.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-style: italic;");
            panePie.getChildren().addAll(lblPieTitle, noData);
        } else {
            panePie.getChildren().addAll(lblPieTitle, pieChart);
        }

        // Bar Chart
        VBox paneBar = new VBox(8);
        paneBar.getStyleClass().add("form-section");
        HBox.setHgrow(paneBar, Priority.ALWAYS);
        Label lblBarTitle = new Label("Média Salarial por Departamento (R$)");
        lblBarTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setMinHeight(280);
        barChart.setPrefHeight(280);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        avgSalaryByDept.forEach((dept, avg) -> series.getData().add(new XYChart.Data<>(dept, avg)));
        barChart.getData().add(series);

        if (avgSalaryByDept.isEmpty()) {
            Label noData = new Label("Nenhum dado salarial disponível.");
            noData.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-style: italic;");
            paneBar.getChildren().addAll(lblBarTitle, noData);
        } else {
            paneBar.getChildren().addAll(lblBarTitle, barChart);
        }

        chartsRow.getChildren().addAll(panePie, paneBar);

        // Recent Hires
        VBox recentSection = new VBox(8);
        Label recentTitle = new Label("Contratações Recentes");
        recentTitle.getStyleClass().add("section-title");
        recentTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox recentList = new VBox(6);
        recentList.getStyleClass().add("form-section");

        List<Employee> recent = allEmployees.stream()
                .sorted((a, b) -> b.getHireDate().compareTo(a.getHireDate()))
                .limit(5).toList();

        if (recent.isEmpty()) {
            Label noRecent = new Label("Nenhuma contratação registrada.");
            noRecent.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-style: italic;");
            recentList.getChildren().add(noRecent);
        } else {
            for (Employee emp : recent) {
                HBox row = new HBox(14);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(4, 0, 4, 0));

                StackPane avatar = buildAvatar(emp.getFirstName(), emp.getLastName());

                VBox info = new VBox(1);
                Label nameL = new Label(emp.getFullName());
                nameL.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 12px; -fx-font-weight: bold;");
                Label posL = new Label(emp.getPosition() != null ? emp.getPosition().getName() : "—");
                posL.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
                info.getChildren().addAll(nameL, posL);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label dateL = new Label("Admissão: " + emp.getHireDate().toString());
                dateL.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");

                Label badge = new Label(emp.getStatus().toString());
                badge.getStyleClass().add(emp.getStatus() == EmployeeStatus.ACTIVE ? "badge-success" : "badge-danger");

                row.getChildren().addAll(avatar, info, spacer, dateL, badge);
                recentList.getChildren().add(row);
            }
        }
        recentSection.getChildren().addAll(recentTitle, recentList);

        container.getChildren().addAll(header, kpis, chartsRow, recentSection);
        scroll.setContent(container);
        return scroll;
    }

    private VBox kpiCard(String label, String value, String subtitle, String iconData, String color) {
        VBox card = new VBox(6);
        card.getStyleClass().add("kpi-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        StackPane icon = UiIcon.get(iconData, 20, color);

        Label lbl = new Label(label);
        lbl.getStyleClass().add("kpi-label");

        Label val = new Label(value);
        val.getStyleClass().addAll("kpi-value");
        val.setStyle("-fx-font-size: 22px;");

        Label sub = new Label(subtitle);
        sub.getStyleClass().add("kpi-subtitle");

        card.getChildren().addAll(icon, lbl, val, sub);
        return card;
    }

    private StackPane buildAvatar(String firstName, String lastName) {
        StackPane sp = new StackPane();
        sp.setStyle(
                "-fx-background-color: -color-accent-subtle; -fx-background-radius: 50%; -fx-min-width: 30px; -fx-min-height: 30px; -fx-pref-width: 30px; -fx-pref-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px;");

        String initials = (firstName.isEmpty() ? "" : String.valueOf(firstName.charAt(0)))
                + (lastName.isEmpty() ? "" : String.valueOf(lastName.charAt(0)));

        Label lbl = new Label(initials.toUpperCase());
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("-fx-text-fill: -color-accent-fg; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 0;");
        sp.getChildren().add(lbl);
        StackPane.setAlignment(lbl, Pos.CENTER);
        return sp;
    }
}
