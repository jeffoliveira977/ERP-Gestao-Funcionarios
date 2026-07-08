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

@Embeddable
public class BankInfo {

    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "bank_agency")
    private String agency;
    @Column(name = "bank_account")
    private String account;
    @Column(name = "bank_account_type")
    private String accountType;
    @Column(name = "bank_pix_key")
    private String pixKey;

    public BankInfo() {
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String v) {
        this.bankName = v;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String v) {
        this.agency = v;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String v) {
        this.account = v;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String v) {
        this.accountType = v;
    }

    public String getPixKey() {
        return pixKey;
    }

    public void setPixKey(String v) {
        this.pixKey = v;
    }
}
