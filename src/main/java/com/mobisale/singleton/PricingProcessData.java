package com.mobisale.singleton;

import com.mobisale.utils.LogUtil;
import com.promotions.models.ItemPricing;
import com.promotions.models.ItemPricingShow;

import javax.print.attribute.HashAttributeSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PricingProcessData {
    private static PricingProcessData m_instance = null;
    private ConcurrentHashMap<String, ItemPricing> pricingCacheMap = new ConcurrentHashMap<>();
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

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

    public synchronized void AddItemWithPrice(String custID, com.promotions.models.Item item){
           String key = custID + "_" + item.ItemCode;
        item.Pricing.ItemCode = item.ItemCode;
        com.promotions.models.ItemPricing copyItem = new ItemPricing(item.Pricing);
        copyItem.PricingCacheDate = new SimpleDateFormat(DATE_FORMAT_NOW).format(new Date());

        pricingCacheMap.put(key, copyItem);
    }

    public synchronized ItemPricing GetItemWithPrice(String custID, com.promotions.models.Item item)
    {
        String key = custID + "_" + item.ItemCode;
        com.promotions.models.ItemPricing result = pricingCacheMap.get(key);
        if (result != null) {
            com.promotions.models.ItemPricing copyItem = new ItemPricing(result);
            return copyItem;
        }
        else
            return  null;
    }

    public synchronized List<ItemPricingShow> GetAllItemsForCustomer(String custID){
        Set<String> cacheKeySet = pricingCacheMap.keySet();

        List<ItemPricingShow> result = new ArrayList<>();

        for (String key: cacheKeySet) {
            if (custID == null || (custID != null && key.contains(custID + "_"))){
                com.promotions.models.ItemPricing itemprice = pricingCacheMap.get(key);
                com.promotions.models.ItemPricingShow itempriceShow = new ItemPricingShow();
                itempriceShow.Cache_Key = key;
                if (custID != null) {
                    itempriceShow.CustID = custID;
                    itempriceShow.ItemCode = key.substring((custID + "_").length());
                }
                else{
                    Integer delimIndex = key.indexOf('_');
                    itempriceShow.CustID = key.substring(0, delimIndex);
                    itempriceShow.ItemCode = key.substring(delimIndex + 1);
                }
                itempriceShow.PriceBruto = itemprice.PriceBruto;
                itempriceShow.PriceNeto = itemprice.PriceNeto;
                itempriceShow.DiscountPercent = itemprice.DiscountPercent;
                itempriceShow.UnitType = itemprice.UnitType;
                itempriceShow.PricingCacheDate = itemprice.PricingCacheDate;
                if (itemprice != null)
                    result.add(itempriceShow);
            }
        }

        return  result;
    }



}
