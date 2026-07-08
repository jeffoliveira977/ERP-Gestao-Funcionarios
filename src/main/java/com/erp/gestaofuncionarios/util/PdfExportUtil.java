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

package com.erp.gestaofuncionarios.util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.TimeRecord;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportUtil {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    public static void exportTerminationToPdf(
            File file, Employee employee, String reason, LocalDate exitDate, int noticeDays,
            BigDecimal balanceSalary, BigDecimal proportional13, BigDecimal vacationVal,
            BigDecimal fgtsBalance, BigDecimal fgtsFine, BigDecimal total) throws Exception {

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Paragraph title = new Paragraph("TERMO DE RESCISÃO DO CONTRATO DE TRABALHO", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable tableEmp = new PdfPTable(2);
        tableEmp.setWidthPercentage(100);
        tableEmp.setSpacingAfter(15);

        addCell(tableEmp, "EMPRESA / EMPREGADOR:", "Gestão de Funcionários S.A.");
        addCell(tableEmp, "CNPJ:", "00.000.000/0001-00");
        addCell(tableEmp, "COLABORADOR:", employee.getFullName());
        addCell(tableEmp, "CPF:", employee.getCpf());
        addCell(tableEmp, "CARGO:", employee.getPosition() != null ? employee.getPosition().getName() : "—");
        addCell(tableEmp, "DEPARTAMENTO:", employee.getDepartment() != null ? employee.getDepartment().getName() : "—");
        addCell(tableEmp, "DATA ADMISSÃO:", employee.getHireDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addCell(tableEmp, "DATA AFASTAMENTO:", exitDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addCell(tableEmp, "MOTIVO AFASTAMENTO:", reason);
        addCell(tableEmp, "AVISO PRÉVIO:", noticeDays + " dias");

        document.add(tableEmp);

        Paragraph sub1 = new Paragraph("DEMONSTRATIVO DE VERBAS E DIREITOS", FONT_SUBTITLE);
        sub1.setSpacingAfter(8);
        document.add(sub1);

        PdfPTable tableValues = new PdfPTable(2);
        tableValues.setWidthPercentage(100);
        tableValues.setSpacingAfter(20);

        addValueRow(tableValues, "Saldo de Salário:", balanceSalary);
        addValueRow(tableValues, "13º Salário Proporcional:", proportional13);
        addValueRow(tableValues, "Férias Proporcionais + 1/3 Constitucional:", vacationVal);
        addValueRow(tableValues, "Saldo do FGTS para cálculo:", fgtsBalance);
        addValueRow(tableValues, "Multa Rescisória de 40% do FGTS (pago pela empresa):", fgtsFine);

        PdfPCell cellTotalLabel = new PdfPCell(new Paragraph("TOTAL LÍQUIDO A RECEBER:", FONT_HEADER));
        cellTotalLabel.setPadding(6);
        cellTotalLabel.setBackgroundColor(new Color(230, 230, 230));
        tableValues.addCell(cellTotalLabel);

        PdfPCell cellTotalVal = new PdfPCell(new Paragraph(fmt(total), FONT_HEADER));
        cellTotalVal.setPadding(6);
        cellTotalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellTotalVal.setBackgroundColor(new Color(230, 230, 230));
        tableValues.addCell(cellTotalVal);

        document.add(tableValues);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfPTable tableSign = new PdfPTable(2);
        tableSign.setWidthPercentage(100);
        tableSign.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cell1 = new PdfPCell(
                new Paragraph("_____________________________________\nAssinatura do Empregador", FONT_VALUE));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell2 = new PdfPCell(
                new Paragraph("_____________________________________\nAssinatura do Colaborador", FONT_VALUE));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

        tableSign.addCell(cell1);
        tableSign.addCell(cell2);
        document.add(tableSign);

        document.close();
    }

    public static void exportTimeSheetToPdf(
            File file, Employee employee, LocalDate start, LocalDate end, List<TimeRecord> records) throws Exception {

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Paragraph title = new Paragraph("FOLHA ESPELHO DE PONTO", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph period = new Paragraph("Período: " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " a "
                + end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FONT_SUBTITLE);
        period.setAlignment(Element.ALIGN_CENTER);
        document.add(period);
        document.add(new Paragraph(" "));

        PdfPTable tableEmp = new PdfPTable(2);
        tableEmp.setWidthPercentage(100);
        tableEmp.setSpacingAfter(15);

        addCell(tableEmp, "EMPRESA:", "Gestão de Funcionários S.A.");
        addCell(tableEmp, "COLABORADOR:", employee.getFullName());
        addCell(tableEmp, "MATRÍCULA / REGISTRO:",
                employee.getRegistrationNumber() != null ? employee.getRegistrationNumber() : "—");
        addCell(tableEmp, "CPF:", employee.getCpf());
        addCell(tableEmp, "DEPARTAMENTO:", employee.getDepartment() != null ? employee.getDepartment().getName() : "—");
        addCell(tableEmp, "CARGO:", employee.getPosition() != null ? employee.getPosition().getName() : "—");

        document.add(tableEmp);

        PdfPTable tableGrid = new PdfPTable(4);
        tableGrid.setWidthPercentage(100);
        tableGrid.setSpacingAfter(20);
        tableGrid.setWidths(new float[] { 15, 20, 20, 45 });

        addGridHeader(tableGrid, "Data");
        addGridHeader(tableGrid, "Entrada");
        addGridHeader(tableGrid, "Saída");
        addGridHeader(tableGrid, "Observações");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (TimeRecord r : records) {
            tableGrid.addCell(new PdfPCell(new Paragraph(r.getDate().format(dtf), FONT_VALUE)));
            tableGrid.addCell(
                    new PdfPCell(new Paragraph(r.getClockIn() != null ? r.getClockIn().toString() : "—", FONT_VALUE)));
            tableGrid.addCell(new PdfPCell(
                    new Paragraph(r.getClockOut() != null ? r.getClockOut().toString() : "—", FONT_VALUE)));
            tableGrid.addCell(new PdfPCell(new Paragraph(r.getNotes() != null ? r.getNotes() : "", FONT_VALUE)));
        }

        document.add(tableGrid);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        PdfPTable tableSign = new PdfPTable(2);
        tableSign.setWidthPercentage(100);
        tableSign.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cell1 = new PdfPCell(
                new Paragraph("_____________________________________\nAssinatura do Empregador", FONT_VALUE));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell2 = new PdfPCell(
                new Paragraph("_____________________________________\nAssinatura do Colaborador", FONT_VALUE));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

        tableSign.addCell(cell1);
        tableSign.addCell(cell2);
        document.add(tableSign);

        document.close();
    }

    private static void addCell(PdfPTable table, String label, String value) {
        PdfPCell cellLabel = new PdfPCell(new Paragraph(label, FONT_LABEL));
        cellLabel.setBorder(Rectangle.BOX);
        cellLabel.setPadding(4);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Paragraph(value, FONT_VALUE));
        cellValue.setBorder(Rectangle.BOX);
        cellValue.setPadding(4);
        table.addCell(cellValue);
    }

    private static void addValueRow(PdfPTable table, String label, BigDecimal value) {
        PdfPCell cellLabel = new PdfPCell(new Paragraph(label, FONT_VALUE));
        cellLabel.setPadding(5);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Paragraph(fmt(value), FONT_VALUE));
        cellValue.setPadding(5);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellValue);
    }

    private static void addGridHeader(PdfPTable table, String title) {
        PdfPCell cell = new PdfPCell(new Paragraph(title, FONT_HEADER));
        cell.setPadding(6);
        cell.setBackgroundColor(new Color(240, 240, 240));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static String fmt(BigDecimal v) {
        if (v == null)
            return "R$ 0,00";

        return "R$ " + String.format("%,.2f", v).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}
