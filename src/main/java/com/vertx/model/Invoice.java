package com.vertx.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "invoice")
public class Invoice {

    private  Integer invoiceno;
    private  String name;
    private  String date;
    private  String filename;

    public Invoice() {
    }

    public Invoice(Integer invoiceno) {
        this.invoiceno = invoiceno;
    }

    public Integer getInvoiceno() {
        return invoiceno;
    }

    public void setInvoiceno(Integer invoiceno) {
        this.invoiceno = invoiceno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "{ invoiceno ::"+invoiceno+", Name ::"+name +", Date::"+date+" }";
    }
}
