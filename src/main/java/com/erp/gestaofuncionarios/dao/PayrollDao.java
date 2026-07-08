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

import com.erp.gestaofuncionarios.model.Payroll;
import com.erp.gestaofuncionarios.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class PayrollDao extends GenericDao<Payroll> {
    public PayrollDao() {
        super(Payroll.class);
    }

    public Payroll findByEmployeeAndPeriod(Long employeeId, String period) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("from Payroll p where p.employee.id = :employeeId and p.period = :period", Payroll.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("period", period)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Payroll> findByPeriod(String period) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("from Payroll p where p.period = :period order by p.employee.firstName", Payroll.class)
                    .setParameter("period", period)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Payroll> findByEmployee(Long employeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("from Payroll p where p.employee.id = :employeeId order by p.period desc", Payroll.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
