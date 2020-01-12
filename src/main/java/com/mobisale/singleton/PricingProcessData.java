package com.mobisale.singleton;

import com.promotions.models.ItemPricing;

import javax.print.attribute.HashAttributeSet;
import java.util.HashMap;

public class PricingProcessData {
    private static PricingProcessData m_instance = null;
    private HashMap<String, com.promotions.models.ItemPricing> pricingCacheMap = new HashMap<>();

    public static PricingProcessData getInstance() {
        if (m_instance == null) {
            m_instance = new PricingProcessData();
        }
        // Return the instance
        return m_instance;
    }

    public void clearResources(){
        pricingCacheMap.clear();
    }

    public void AddItemWithPrice(com.promotions.models.Item item){
           String key = item.Pricing.PricingProcedure + "_" + item.ItemCode;
        item.Pricing.ItemCode = item.ItemCode;
        com.promotions.models.ItemPricing copyItem = new ItemPricing(item.Pricing);
        pricingCacheMap.put(key, copyItem);
    }

    public ItemPricing GetItemWithPrice(String pricingProcedure, com.promotions.models.Item item)
    {
        String key = pricingProcedure + "_" + item.ItemCode;
        com.promotions.models.ItemPricing result = pricingCacheMap.get(key);
        if (result != null) {
            com.promotions.models.ItemPricing copyItem = new ItemPricing(result);
            return copyItem;
        }
        else
            return  null;
    }
}
