package com.promotions.models;

import java.util.List;

public class PricingRequest {

    private String customerCode;
    private List<Item> items;
    private String Cust_Key;
    private Boolean showPriceLines;

    private String promotions_Cust_Key = "";

    public PricingRequest() {

    }

    public Boolean getShowPriceLines(){
        return showPriceLines;
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

    public String getCust_Key() {
        return Cust_Key;
    }

    public void setCust_Key(String Cust_Key) {
        this.Cust_Key = Cust_Key;
    }

    public String getpromotions_Cust_Key() {
        return promotions_Cust_Key;
    }

    public void setpromotions_Cust_Key(String promotions_Cust_Key) {
        this.promotions_Cust_Key = promotions_Cust_Key;
    }

}
