package com.promotions.data;

/**
 * Created with IntelliJ IDEA.
 * User: israel
 * Date: 10/6/13
 * Time: 3:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class PromotionBonusItem {
    public final String ItemCode;
    public final int UnitsQuantity;
    public String BaseUnit;
    private double promotionNetoPrice;
    private double promotionNetoDiscount;
    private int quantity = 0;

    public PromotionBonusItem(String itemCode, double price, double discount, int unitsQuantity, String baseUnit) {
        ItemCode = itemCode;
        promotionNetoPrice = price;
        promotionNetoDiscount = discount;
        UnitsQuantity = unitsQuantity;
        BaseUnit = baseUnit;
    }

    public void updateQuantity(float newQuantity) {
        quantity = (int) newQuantity;
    }

    public void updatePromotionNetoDiscount(double promotionNetoDiscount) {
        this.promotionNetoDiscount = promotionNetoDiscount;
    }

    public void resetQuantity() {
        quantity = 0;
    }

    public double getPromotionNetoPrice() {
        return promotionNetoPrice;
    }

    public double getPromotionNetoDiscount() {
        return promotionNetoDiscount;
    }

    public int getQuantity() {
        return quantity;
    }
}

