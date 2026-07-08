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

import com.erp.gestaofuncionarios.model.TimeRecord;
import com.erp.gestaofuncionarios.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.time.LocalDate;
import java.util.List;

public class TimeRecordDao extends GenericDao<TimeRecord> {
    public TimeRecordDao() {
        super(TimeRecord.class);
    }

    public TimeRecord findByEmployeeAndDate(Long employeeId, LocalDate date) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em
                    .createQuery("from TimeRecord tr where tr.employee.id = :employeeId and tr.date = :date",
                            TimeRecord.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("date", date)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<TimeRecord> findByEmployee(Long employeeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em
                    .createQuery("from TimeRecord tr where tr.employee.id = :employeeId order by tr.date desc",
                            TimeRecord.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<TimeRecord> findAllOrderByDateDesc() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("from TimeRecord tr order by tr.date desc, tr.clockIn desc", TimeRecord.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
