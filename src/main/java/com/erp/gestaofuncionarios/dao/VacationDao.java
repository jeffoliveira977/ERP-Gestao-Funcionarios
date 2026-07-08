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

import com.erp.gestaofuncionarios.model.Vacation;
import com.erp.gestaofuncionarios.model.VacationStatus;
import com.erp.gestaofuncionarios.util.JpaUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class VacationDao extends GenericDao<Vacation> {
    public VacationDao() {
        super(Vacation.class);
    }

    public List<Vacation> findByEmployee(Long employeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "from Vacation v where v.employee.id = :eid order by v.startDate desc", Vacation.class)
                    .setParameter("eid", employeeId).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Vacation> findByStatus(VacationStatus status) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "from Vacation v where v.status = :status order by v.startDate", Vacation.class)
                    .setParameter("status", status).getResultList();
        } finally {
            em.close();
        }
    }

    public int countDaysUsedByEmployee(Long employeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Vacation> vacations = em.createQuery(
                    "from Vacation v where v.employee.id = :eid and v.status in (:s1, :s2)", Vacation.class)
                    .setParameter("eid", employeeId)
                    .setParameter("s1", VacationStatus.IN_PROGRESS)
                    .setParameter("s2", VacationStatus.COMPLETED)
                    .getResultList();
            return vacations.stream().mapToInt(v -> v.getDaysCount() != null ? v.getDaysCount() : 0).sum();
        } finally {
            em.close();
        }
    }
}
