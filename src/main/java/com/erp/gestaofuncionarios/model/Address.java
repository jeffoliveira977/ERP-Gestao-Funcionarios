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
public class Address {

    @Column(name = "address_street")
    private String street;
    @Column(name = "address_number")
    private String number;
    @Column(name = "address_complement")
    private String complement;
    @Column(name = "address_neighborhood")
    private String neighborhood;
    @Column(name = "address_city")
    private String city;
    @Column(name = "address_state")
    private String state;
    @Column(name = "address_zip_code")
    private String zipCode;

    public Address() {
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String v) {
        this.street = v;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String v) {
        this.number = v;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String v) {
        this.complement = v;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String v) {
        this.neighborhood = v;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String v) {
        this.city = v;
    }

    public String getState() {
        return state;
    }

    public void setState(String v) {
        this.state = v;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String v) {
        this.zipCode = v;
    }
}
