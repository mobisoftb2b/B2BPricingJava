package com.promotions.models;

import java.util.List;

public class PricingRequest {

    private String customerCode;
    private List<Item> items;

    public PricingRequest() {

    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

}
