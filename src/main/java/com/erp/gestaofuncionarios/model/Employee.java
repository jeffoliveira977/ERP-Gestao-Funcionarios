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

package com.erp.gestaofuncionarios.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(unique = true)
    private String rg;

    @Column(name = "pis_pasep", unique = true)
    private String pisPasep;

    @Column
    private String phone;

    @Column(name = "mobile_phone")
    private String mobilePhone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    @Column
    private String nationality;

    @Embedded
    private Address address = new Address();

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "termination_reason")
    private String terminationReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @Column(name = "weekly_hours")
    private Integer weeklyHours;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(nullable = false)
    private BigDecimal salary;

    @Embedded
    private BankInfo bankInfo = new BankInfo();

    @Embedded
    private EmergencyContact emergencyContact = new EmergencyContact();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Embedded
    private DocumentChecklist documents = new DocumentChecklist();

    public Employee() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        this.id = v;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String v) {
        this.registrationNumber = v;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String v) {
        this.firstName = v;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String v) {
        this.lastName = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        this.email = v;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String v) {
        this.cpf = v;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String v) {
        this.rg = v;
    }

    public String getPisPasep() {
        return pisPasep;
    }

    public void setPisPasep(String v) {
        this.pisPasep = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        this.phone = v;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String v) {
        this.mobilePhone = v;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate v) {
        this.birthDate = v;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender v) {
        this.gender = v;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus v) {
        this.maritalStatus = v;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String v) {
        this.nationality = v;
    }

    public Address getAddress() {
        if (address == null)
            address = new Address();
        return address;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate v) {
        this.hireDate = v;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate v) {
        this.terminationDate = v;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String v) {
        this.terminationReason = v;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus v) {
        this.status = v;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType v) {
        this.contractType = v;
    }

    public Integer getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(Integer v) {
        this.weeklyHours = v;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department v) {
        this.department = v;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position v) {
        this.position = v;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal v) {
        this.salary = v;
    }

    public BankInfo getBankInfo() {
        if (bankInfo == null)
            bankInfo = new BankInfo();
        return bankInfo;
    }

    public EmergencyContact getEmergencyContact() {
        if (emergencyContact == null)
            emergencyContact = new EmergencyContact();
        return emergencyContact;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String v) {
        this.notes = v;
    }

    public DocumentChecklist getDocuments() {
        if (documents == null)
            documents = new DocumentChecklist();
        return documents;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
