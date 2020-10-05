package com.promotions.models;

import com.mobisale.data.ItemPricingData;

import java.util.ArrayList;

public class ItemPricing {
    public double PriceBruto;
    public double PriceNeto;
    public double PriceBrutoUnit;
    public double BasketDiscountPercent;
    public double PriceNetoUnit;
    public double DiscountPercent;
    public String UnitType;
    public double SubTotalsPromotionDiscountValue;
    public double TotalLine;
    public ItemPricingData PricingData;
    public String PricingProcedure;
    public String ItemCode;
    public double Quantity;
    public ArrayList<ItemPricingData.ItemPricingLine> itemPricingLines;
    public Boolean HasFixedPrice;
    public String PricingCacheDate;

    public ItemPricing(){}

    public ItemPricing(ItemPricing item)
    {
        PriceBruto = item.PriceBruto;
        PriceNeto = item.PriceNeto;
        PriceBrutoUnit = item.PriceBrutoUnit;
        PriceNetoUnit = item.PriceNetoUnit;
        DiscountPercent = item.DiscountPercent;
        UnitType = item.UnitType;
        SubTotalsPromotionDiscountValue = item.SubTotalsPromotionDiscountValue;
        TotalLine = item.TotalLine;
        PricingData = new ItemPricingData(item.PricingData);
        PricingProcedure = item.PricingProcedure;
        ItemCode = item.ItemCode;
        Quantity = item.Quantity;
        HasFixedPrice = item.HasFixedPrice;
    }
}
