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
public class DocumentChecklist {

    @Column(name = "doc_cpf")
    private Boolean cpf = false;
    @Column(name = "doc_rg")
    private Boolean rg = false;
    @Column(name = "doc_work_card")
    private Boolean workCard = false;
    @Column(name = "doc_address")
    private Boolean address = false;
    @Column(name = "doc_aso")
    private Boolean aso = false;
    @Column(name = "doc_photo")
    private Boolean photo = false;

    public DocumentChecklist() {
    }

    public Boolean getCpf() {
        return cpf;
    }

    public void setCpf(Boolean v) {
        this.cpf = v;
    }

    public Boolean getRg() {
        return rg;
    }

    public void setRg(Boolean v) {
        this.rg = v;
    }

    public Boolean getWorkCard() {
        return workCard;
    }

    public void setWorkCard(Boolean v) {
        this.workCard = v;
    }

    public Boolean getAddress() {
        return address;
    }

    public void setAddress(Boolean v) {
        this.address = v;
    }

    public Boolean getAso() {
        return aso;
    }

    public void setAso(Boolean v) {
        this.aso = v;
    }

    public Boolean getPhoto() {
        return photo;
    }

    public void setPhoto(Boolean v) {
        this.photo = v;
    }
}
