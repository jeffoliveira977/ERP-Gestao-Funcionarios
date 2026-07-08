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

package com.erp.gestaofuncionarios.dao;

import com.erp.gestaofuncionarios.model.Absence;
import com.erp.gestaofuncionarios.util.JpaUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class AbsenceDao extends GenericDao<Absence> {

    public AbsenceDao() {
        super(Absence.class);
    }

    public List<Absence> findByEmployee(Long employeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em
                    .createQuery("from Absence a where a.employee.id = :employeeId order by a.startDate desc",
                            Absence.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Absence> findByEmployeeAndPeriod(Long employeeId, LocalDate start, LocalDate end) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "from Absence a where a.employee.id = :employeeId and " +
                            "((a.startDate <= :end and a.endDate >= :start))",
                    Absence.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
