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

import java.math.BigDecimal;

public class ValidationUtil {

    public static BigDecimal parseSalary(String text) {
        if (text == null || text.isBlank()) {
            throw new NumberFormatException("Valor vazio");
        }

        String clean = text.replaceAll("[^\\d.,]", "").trim();

        int lastComma = clean.lastIndexOf(',');
        int lastDot = clean.lastIndexOf('.');
        int sepIndex = Math.max(lastComma, lastDot);

        String normalized;
        if (sepIndex == -1) {
            normalized = clean;
        } else {
            String integerPart = clean.substring(0, sepIndex).replaceAll("[.,]", "");
            String decimalPart = clean.substring(sepIndex + 1).replaceAll("[.,]", "");
            normalized = integerPart + "." + decimalPart;
        }

        return new BigDecimal(normalized);
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public static boolean isValidCpf(String cpf) {
        if (cpf == null) {
            return false;
        }
        String clean = cpf.replaceAll("\\D", "");
        if (clean.length() != 11) {
            return false;
        }

        if (clean.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (clean.charAt(i) - '0') * (10 - i);
            }
            int r1 = 11 - (sum % 11);
            int d1 = (r1 == 10 || r1 == 11) ? 0 : r1;

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += (clean.charAt(i) - '0') * (11 - i);
            }
            int r2 = 11 - (sum % 11);
            int d2 = (r2 == 10 || r2 == 11) ? 0 : r2;

            return (clean.charAt(9) - '0' == d1) && (clean.charAt(10) - '0' == d2);
        } catch (Exception e) {
            return false;
        }
    }
}
