package com.promotions.models;

import com.promotions.data.ItemBonusData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PricingResponse {

    private String customerCode;
    private List<Item> items;
    private List<ItemBonusData> bonusDataMap;

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
        for (Item item : items)
        {
            item.Pricing.PricingData = null;
        }
        this.items = items;
    }

    public List<ItemBonusData> getBonusDataMap() {
        return bonusDataMap;
    }

    public void setBonusDataMap(HashMap<String, ArrayList<ItemBonusData>> bonusDataMap) {
        if (this.bonusDataMap == null)
            this.bonusDataMap = new ArrayList<>();
        else
            this.bonusDataMap.clear();
        for(String espNumber: bonusDataMap.keySet()) {
            ArrayList<ItemBonusData> bonusesForEspNumber = bonusDataMap.get(espNumber);
            for(ItemBonusData bonusData: bonusesForEspNumber)
                this.bonusDataMap.add(bonusData);
        }
    }
}
