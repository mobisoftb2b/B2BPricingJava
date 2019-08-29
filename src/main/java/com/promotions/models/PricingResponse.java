package com.promotions.models;

import com.promotions.data.ItemBonusData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PricingResponse {

    private String customerCode;
    private List<Item> items;
    private HashMap<String, ArrayList<ItemBonusData>> bonusDataMap;

    public PricingResponse() {

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

    public HashMap<String, ArrayList<ItemBonusData>> getBonusDataMap() {
        return bonusDataMap;
    }

    public void setBonusDataMap(HashMap<String, ArrayList<ItemBonusData>> bonusDataMap) {
        this.bonusDataMap = bonusDataMap;
    }
}
