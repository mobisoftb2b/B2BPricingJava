package com.promotions.data;


/**
 * Created by israel on 4/22/14.
 */
public class PromotionStep extends APromotionStep {

    public PromotionStep(String Cust_Key, String ESPNumber, int recordNumber, int step, int qtyBasedStep, int valBasedStep, int promotionType, double promotionDiscount, double priceBasedQty,
                         String priceBQtyUOM, double promotionPrice, String promotionPriceCurrency, String stepDescription) {
        super(Cust_Key, ESPNumber, recordNumber, step, qtyBasedStep, valBasedStep, promotionType, promotionDiscount, 0, 0, priceBasedQty, priceBQtyUOM, promotionPrice, promotionPriceCurrency, 0, "", 0, "", stepDescription);
    }

    //public String getStepDescription(int definitionMethod, String stepsBasedUOM) {
    //    return super.getStepDescription(definitionMethod, stepsBasedUOM);
    //}
    @Override
    public String getStepDescription(int definitionMethod, String stepsBasedUOM) {
        String remoteDescription = super.getStepDescription1(definitionMethod, stepsBasedUOM);
        if (remoteDescription != null && !remoteDescription.isEmpty() ) return remoteDescription;
        String description = "";
        String buyBoxOrUnit = "";
        String getBoxOrUnit = "";
        if (stepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            buyBoxOrUnit = "יח\'";
        } else {
            buyBoxOrUnit = "קר\'";
        }
        if (PriceBQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            getBoxOrUnit = "יח\'";
        } else {
            getBoxOrUnit = "קר\'";
        }
        if (definitionMethod == 1) {
            if (PromotionType == 2) {
                description = String.format("%s %d %s %s %s %s", "קנה", QtyBasedStep, buyBoxOrUnit, "קבל", dfPercent.format(PromotionDiscount) + "%", "הנחה");
            } else if (PromotionType == 3) {
                description = String.format("%s %d %s %s %s %s %s", "קנה", QtyBasedStep, buyBoxOrUnit, "במחיר נטו", dfPrice.format(PromotionPrice) + "₪ ", "ל", getBoxOrUnit);
            }
        } else {
            if (PromotionType == 2) {
                description = String.format("%s %s %s %s %s", "קנה בלפחות", dfPrice.format(ValBasedStep) + "₪ ", "קבל", dfPercent.format(PromotionDiscount) + "%", "הנחה");
            } else if (PromotionType == 3) {
                description = String.format("%s %s %s %s %s %s", "קנה ב", dfPrice.format(ValBasedStep) + "₪ ", "במחיר נטו", dfPrice.format(PromotionPrice) + "₪ ", "ל", getBoxOrUnit);
            }
        }
        return description;
    }

    @Override
    public boolean isPromotionStepBonus() {
        return false;
    }
}
