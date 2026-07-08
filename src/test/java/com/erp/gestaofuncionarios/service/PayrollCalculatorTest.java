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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class PayrollCalculatorTest {

    @Test
    public void testInssCalculation() {
        // Salário de R$ 1.412,00 (alíquota mínima de 7.5%)
        BigDecimal salMin = new BigDecimal("1412.00");
        BigDecimal inssMin = PayrollCalculator.calculateInss(salMin);
        assertEquals(new BigDecimal("105.90"), inssMin); // 1412 * 7.5% = 105.90
        
        // Salário de R$ 3.000,00 (faixa de 12%)
        // Cálculo detalhado:
        // 1ª faixa: 1412 * 0.075 = 105.90
        // 2ª faixa: (2666.68 - 1412.00) * 0.09 = 1254.68 * 0.09 = 112.92
        // 3ª faixa: (3000 - 2666.68) * 0.12 = 333.32 * 0.12 = 40.00
        // Total = 105.90 + 112.92 + 40.00 = 258.82
        BigDecimal salMedio = new BigDecimal("3000.00");
        BigDecimal inssMedio = PayrollCalculator.calculateInss(salMedio);
        assertEquals(new BigDecimal("258.82"), inssMedio);
    }

    @Test
    public void testIrrfCalculation() {
        // Base de cálculo de R$ 2.000,00 (abaixo do teto de isenção de R$ 2.259,20)
        BigDecimal baseIsenta = new BigDecimal("2000.00");
        BigDecimal irrfIsento = PayrollCalculator.calculateIrrf(baseIsenta);
        assertEquals(BigDecimal.ZERO.setScale(2), irrfIsento);
        
        // Base de cálculo de R$ 3.000,00
        // Cálculo: (3000 * 15%) - dedução de 381.44 = 450 - 381.44 = 68.56
        BigDecimal baseMedio = new BigDecimal("3000.00");
        BigDecimal irrfMedio = PayrollCalculator.calculateIrrf(baseMedio);
        assertEquals(new BigDecimal("68.56"), irrfMedio);
    }
}
