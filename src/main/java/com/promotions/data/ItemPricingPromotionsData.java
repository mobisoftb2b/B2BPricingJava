package com.promotions.data;

import static com.promotions.data.ItemPromotionData.PC_UNIT;

public class ItemPricingPromotionsData {
    private double totalStartValue = 0;
    private double totalValue = 0;
    private double totalDiscountValue = 0;
    private String priceUnitType = PC_UNIT;
    private double subTotalsPromotionDiscountValue = 0;
    //
    private double itemDiscountValueWithoutExcludeDiscount = 0;
    private double promotionValue = 0;
    private boolean excludeDiscount;
    private boolean isPromotionClassification;
    private boolean needToRound;


    public ItemPricingPromotionsData(double totalStartValue, double totalValue, double totalDiscountValue, String priceUnitType, double subTotalsPromotionDiscountValue) {
        this.totalStartValue = totalStartValue;
        this.totalValue = totalValue;
        this.totalDiscountValue = totalDiscountValue;
        this.priceUnitType = priceUnitType;
        this.subTotalsPromotionDiscountValue = subTotalsPromotionDiscountValue;
    }

    public ItemPricingPromotionsData() {
    }

    public double getTotalStartValue() {
        return totalStartValue;
    }

    public void setTotalStartValue(double totalStartValue) {
        this.totalStartValue = totalStartValue;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public double getTotalDiscountValue() {
        return totalDiscountValue;
    }

    public void setTotalDiscountValue(double totalDiscountValue) {
        this.totalDiscountValue = totalDiscountValue;
    }

    public double getPromotionValue() {
        return promotionValue;
    }

    public void setPromotionValue(double promotionValue) {
        this.promotionValue = promotionValue;
    }

    public String getPriceUnitType() {
        return priceUnitType;
    }

    public void setPriceUnitType(String priceUnitType) {
        this.priceUnitType = priceUnitType;
    }

    public double getItemDiscountValueWithoutExcludeDiscount() {
        return itemDiscountValueWithoutExcludeDiscount;
    }

    public boolean isExcludeDiscount() {
        return excludeDiscount;
    }

    public boolean isPromotionClassification() {
        return isPromotionClassification;
    }

    public boolean isNeedToRound() {
        return needToRound;
    }

    public double getSubTotalsPromotionDiscountValue() {
        return subTotalsPromotionDiscountValue;
    }

    public void setSubTotalsPromotionDiscountValue(double subTotalsPromotionDiscountValue) {
        this.subTotalsPromotionDiscountValue = subTotalsPromotionDiscountValue;
    }

    public void updatePromotionCondition(double itemDiscountValueWithoutExcludeDiscount, double itemDiscountValue, boolean excludeDiscount, boolean isPromotionClassification, boolean needToRound) {
        this.itemDiscountValueWithoutExcludeDiscount = itemDiscountValueWithoutExcludeDiscount;
        promotionValue = itemDiscountValue;
        this.excludeDiscount = excludeDiscount;
        this.isPromotionClassification = isPromotionClassification;
        this.needToRound = needToRound;
    }
}
