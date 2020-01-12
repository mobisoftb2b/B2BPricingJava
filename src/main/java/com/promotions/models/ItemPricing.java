package com.promotions.models;

import com.mobisale.data.ItemPricingData;

public class ItemPricing {
    public double PriceBruto;
    public double PriceNeto;
    public double DiscountPercent;
    public String UnitType;
    public double SubTotalsPromotionDiscountValue;
    public double TotalLine;
    public ItemPricingData PricingData;
    public String PricingProcedure;
    public String ItemCode;

    public ItemPricing(){}

    public ItemPricing(ItemPricing item)
    {
        PriceBruto = item.PriceBruto;
        PriceNeto = item.PriceNeto;
        DiscountPercent = item.DiscountPercent;
        UnitType = item.UnitType;
        SubTotalsPromotionDiscountValue = item.SubTotalsPromotionDiscountValue;
        TotalLine = item.TotalLine;
        PricingData = new ItemPricingData(item.PricingData);
        PricingProcedure = item.PricingProcedure;
        ItemCode = item.ItemCode;
    }
}
