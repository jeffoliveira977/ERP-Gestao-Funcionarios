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

import com.erp.gestaofuncionarios.model.Employee;
import com.erp.gestaofuncionarios.model.EmployeeStatus;
import com.erp.gestaofuncionarios.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class EmployeeDao extends GenericDao<Employee> {
    public EmployeeDao() {
        super(Employee.class);
    }

    public Employee findByCpf(String cpf) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("from Employee e where e.cpf = :cpf", Employee.class)
                    .setParameter("cpf", cpf)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Employee findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("from Employee e where e.email = :email", Employee.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Employee> search(String query, Long departmentId, Long positionId, String status) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder sb = new StringBuilder("from Employee e where 1=1");
            if (query != null && !query.trim().isEmpty()) {
                sb.append(
                        " and (lower(e.firstName) like :query or lower(e.lastName) like :query or e.cpf like :query)");
            }
            if (departmentId != null) {
                sb.append(" and e.department.id = :departmentId");
            }
            if (positionId != null) {
                sb.append(" and e.position.id = :positionId");
            }
            if (status != null && !status.trim().isEmpty()) {
                sb.append(" and e.status = :status");
            }

            TypedQuery<Employee> tq = em.createQuery(sb.toString(), Employee.class);

            if (query != null && !query.trim().isEmpty()) {
                tq.setParameter("query", "%" + query.toLowerCase().trim() + "%");
            }
            if (departmentId != null) {
                tq.setParameter("departmentId", departmentId);
            }
            if (positionId != null) {
                tq.setParameter("positionId", positionId);
            }
            if (status != null && !status.trim().isEmpty()) {
                tq.setParameter("status", EmployeeStatus.valueOf(status));
            }

            return tq.getResultList();
        } finally {
            em.close();
        }
    }
}
