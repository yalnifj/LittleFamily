package com.yellowforktech.littlefamilytree.db;

import java.util.Date;

/**
 * Created by john on 1/14/2017.
 */

public class Sale {
    private Date startDate;
    private Date endDate;
    private Double price;
    private String salesText;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSalesText() {
        return salesText;
    }

    public void setSalesText(String salesText) {
        this.salesText = salesText;
    }
}
