package com.promotions.data;


import com.promotions.manager.PromotionsDataManager;

/**
 * Created with IntelliJ IDEA.
 * User: israel
 * Date: 10/6/13
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class PromotionItem {
    public final String ItemCode;
    public double RealPrice;
    public double RealDiscount;
    public final int UnitsQuantity;
    public String BaseUnit;
    private double netoPrice;
    private double netoDiscount;
    private double netoTotalPrice;
    private double promotionNetoDiscount;
    private int PCQuantity = 0;
    private int KARQuantity = 0;
    private double valQuantity = 0;

    private boolean isDuplicateItem = false;
    private int uiIndex = 0;

    public PromotionItem(String itemCode, double realPrice, double realDiscount, int unitsQuantity, String baseUnit) {
        ItemCode = itemCode;
        RealPrice = realPrice;
        RealDiscount = realDiscount;
        UnitsQuantity = unitsQuantity;
        BaseUnit = baseUnit;
//        netoPrice = RealPrice * (1F - (RealDiscount / 100F));
    }

    public void updateQuantity(float newQuantity) {
        PCQuantity = (int) newQuantity;
        KARQuantity = (int) (newQuantity / UnitsQuantity);
        double neto = RealPrice * (1F - (RealDiscount / 100F));
        if (BaseUnit.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            valQuantity = PCQuantity * neto;
        } else {
            valQuantity = KARQuantity * neto;
        }
    }

    public void updatePrice(double newPrice) {
        netoPrice = newPrice;
    }

    public void updateDiscount(double newDiscount) {
        netoDiscount = newDiscount;
    }

    public void updateNetoTotalPrice(double newNetoTotalPrice) {
        netoTotalPrice = newNetoTotalPrice;
    }

    public void updatePromotionNetoDiscount(double promotionNetoDiscount) {
        this.promotionNetoDiscount = promotionNetoDiscount;
    }

    public void resetPriceAndDiscount(String espNumber) {
        PromotionHeader activePromotionHeader = PromotionsDataManager.getInstance().getPromotionHeaderByESPNumber(espNumber);
        ItemPromotionData itemPromotionData = PromotionsDataManager.getInstance().getOrderUIItem(ItemCode, getUiIndex());
        if (itemPromotionData != null && itemPromotionData.isItemPricingInitialized()) {

            promotionNetoDiscount = 0;
            itemPromotionData.getItemPricingData().updatePromotionCondition(promotionNetoDiscount, promotionNetoDiscount, false, activePromotionHeader.IsClassification, true);

            //old version
            netoPrice = itemPromotionData.getItemPricingData().getTotalValue();
            netoDiscount = itemPromotionData.getItemPricingData().getTotalDiscountValue();
            netoTotalPrice = netoPrice * itemPromotionData.getTotalQuantityByUnitType();
        } else {
            netoPrice = RealPrice * (1F - (RealDiscount / 100F));
            netoDiscount = RealDiscount;
            try {
                netoTotalPrice = netoPrice * itemPromotionData.getTotalQuantityByUnitType();
            } catch (Exception e) {
                if (BaseUnit.equalsIgnoreCase("PC")) {
                    netoTotalPrice = PCQuantity * netoPrice;
                } else {
                    netoTotalPrice = KARQuantity * netoPrice;
                }
            }
        }
    }

    public void resetQuantity() {
        PCQuantity = 0;
        KARQuantity = 0;
        valQuantity = 0;
    }

    public double getNetoPrice() {
        return netoPrice;
    }

    public double getNetoTotalPrice() {
        return netoTotalPrice;
    }

    public double getNetoDiscount() {
        return netoDiscount;
    }

    public double getPromotionNetoDiscount() {
        return promotionNetoDiscount;
    }

    public int getPCQuantity() {
        return PCQuantity;
    }

    public int getKARQuantity() {
        return KARQuantity;
    }

    public double getValQuantity() {
        return valQuantity;
    }

    public boolean isDuplicateItem() {
        return isDuplicateItem;
    }

    public void setDuplicateItem(boolean duplicateItem) {
        isDuplicateItem = duplicateItem;
    }

    public int getUiIndex() {
        return uiIndex;
    }

    public void setUiIndex(int uiIndex) {
        this.uiIndex = uiIndex;
    }

    public void updateItemPricing(double price, double discount, String unitType) {
        RealPrice = price;
        RealDiscount = discount;
        BaseUnit = unitType;
//        netoPrice = RealPrice * (1F - (RealDiscount / 100F));
    }
}
