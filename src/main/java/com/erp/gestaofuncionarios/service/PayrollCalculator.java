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

package com.erp.gestaofuncionarios.service;

import com.erp.gestaofuncionarios.dao.AbsenceDao;
import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.Payroll;
import com.erp.gestaofuncionarios.model.PayrollStatus;
import com.erp.gestaofuncionarios.model.Absence;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class PayrollCalculator {

    private static final AbsenceDao absenceDao = new AbsenceDao();

    public static Payroll generatePayroll(Employee employee, String period, BigDecimal allowances) {
        BigDecimal baseSalary = employee.getSalary() != null ? employee.getSalary() : BigDecimal.ZERO;

        BigDecimal absenceDeductions = calculateAbsenceDeductions(employee.getId(), period, baseSalary);

        BigDecimal inss = calculateInss(baseSalary);
        BigDecimal baseIrrf = baseSalary.subtract(inss);
        BigDecimal irrf = calculateIrrf(baseIrrf);
        BigDecimal totalDeductions = inss.add(irrf).add(absenceDeductions);

        BigDecimal netSalary = baseSalary.add(allowances).subtract(totalDeductions);
        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            netSalary = BigDecimal.ZERO;
        }

        return new Payroll(
                employee,
                period,
                baseSalary,
                allowances,
                totalDeductions,
                netSalary,
                null,
                PayrollStatus.DRAFT);
    }

    public static BigDecimal calculateAbsenceDeductions(Long employeeId, String period, BigDecimal baseSalary) {
        if (employeeId == null || period == null || !period.contains("/")
                || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        try {
            String[] parts = period.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            YearMonth ym = YearMonth.of(year, month);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            List<Absence> absences = absenceDao.findByEmployeeAndPeriod(employeeId, start, end);
            int unexcusedDays = 0;

            for (Absence a : absences) {
                if (!a.getExcused()) {
                    LocalDate overlapStart = a.getStartDate().isBefore(start) ? start : a.getStartDate();
                    LocalDate overlapEnd = a.getEndDate().isAfter(end) ? end : a.getEndDate();
                    int days = (int) (overlapEnd.toEpochDay() - overlapStart.toEpochDay()) + 1;
                    unexcusedDays += days;
                }
            }

            if (unexcusedDays > 0) {
                BigDecimal dailyRate = baseSalary.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
                return dailyRate.multiply(BigDecimal.valueOf(unexcusedDays)).setScale(2, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            System.err.println("[PayrollCalculator] Error calculating absence deductions: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    public static BigDecimal calculateInss(BigDecimal salary) {
        double val = salary.doubleValue();
        double inss = 0;

        double limit1 = 1412.00;
        double limit2 = 2666.68;
        double limit3 = 4000.03;
        double limit4 = 7786.02;

        if (val <= limit1) {
            inss = val * 0.075;
        } else if (val <= limit2) {
            inss = (limit1 * 0.075) + ((val - limit1) * 0.09);
        } else if (val <= limit3) {
            inss = (limit1 * 0.075) + ((limit2 - limit1) * 0.09) + ((val - limit2) * 0.12);
        } else if (val <= limit4) {
            inss = (limit1 * 0.075) + ((limit2 - limit1) * 0.09) + ((limit3 - limit2) * 0.12) + ((val - limit3) * 0.14);
        } else {
            // Teto do INSS
            inss = (limit1 * 0.075) + ((limit2 - limit1) * 0.09) + ((limit3 - limit2) * 0.12)
                    + ((limit4 - limit3) * 0.14);
        }

        return BigDecimal.valueOf(inss).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateIrrf(BigDecimal baseSalary) {
        double val = baseSalary.doubleValue();
        double irrf = 0;

        if (val <= 2259.20) {
            irrf = 0;
        } else if (val <= 2826.65) {
            irrf = (val * 0.075) - 169.44;
        } else if (val <= 3751.05) {
            irrf = (val * 0.15) - 381.44;
        } else if (val <= 4664.68) {
            irrf = (val * 0.225) - 662.77;
        } else {
            irrf = (val * 0.275) - 896.00;
        }

        if (irrf < 0)
            irrf = 0;

        return BigDecimal.valueOf(irrf).setScale(2, RoundingMode.HALF_UP);
    }
}
