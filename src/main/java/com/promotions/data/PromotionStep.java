package com.promotions.data;


/**
 * Created by israel on 4/22/14.
 */
public class PromotionStep extends APromotionStep {

    public PromotionStep(String ESPNumber, int recordNumber, int step, int qtyBasedStep, int valBasedStep, int promotionType, double promotionDiscount, double priceBasedQty,
                         String priceBQtyUOM, double promotionPrice, String promotionPriceCurrency, String stepDescription) {
        super(ESPNumber, recordNumber, step, qtyBasedStep, valBasedStep, promotionType, promotionDiscount, 0, 0, priceBasedQty, priceBQtyUOM, promotionPrice, promotionPriceCurrency, 0, "", 0, "", stepDescription);
    }

    public String getStepDescription(int definitionMethod, String stepsBasedUOM) {
        return super.getStepDescription(definitionMethod, stepsBasedUOM);
    }

    @Override
    public boolean isPromotionStepBonus() {
        return false;
    }
}
