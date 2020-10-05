package com.promotions.data;

import java.text.DecimalFormat;

/**
 * Created by israel on 5/1/14.
 */
public abstract class APromotionStep {

    protected final String ESPNumber;
    protected final int RecordNumber;
    protected final int Step;
    protected final int QtyBasedStep;
    protected final int ValBasedStep;
    protected final int PromotionType;
    protected final double PromotionDiscount;
    protected final double BonusDiscount;
    protected final double BonusPrice;
    protected final double PriceBasedQty;
    protected final String PriceBQtyUOM;
    protected final double PromotionPrice;
    protected final String PromotionPriceCurrency;
    protected final int BonusQuantity;
    protected final String BonusQuantityUOM;
    protected final int BonusMultipleQty;
    protected final String BonusMultQtyUOM;
    protected final StepDescription StepDescriptionObj;
    protected final String m_CustKey;
    protected DecimalFormat dfPercent = new DecimalFormat("#,##0.00");
    protected DecimalFormat dfPrice = new DecimalFormat("#,##0.00");

    protected APromotionStep(String Cust_Key,  String espNumber, int recordNumber, int step, int qtyBasedStep, int valBasedStep, int promotionType, double promotionDiscount, double bonusPrice, double bonusDiscount, double priceBasedQty,
                             String priceBQtyUOM, double promotionPrice, String promotionPriceCurrency, int bonusQuantity, String bonusQuantityUOM, int bonusMultipleQty, String bonusMultQtyUOM, StepDescription stepDescription) {
        ESPNumber = espNumber;
        RecordNumber = recordNumber;
        Step = step;
        QtyBasedStep = qtyBasedStep;
        ValBasedStep = valBasedStep;
        PromotionType = promotionType;
        PromotionDiscount = promotionDiscount;
        BonusPrice = bonusPrice;
        BonusDiscount = bonusDiscount;
        PriceBasedQty = priceBasedQty;
        PriceBQtyUOM = priceBQtyUOM;
        PromotionPrice = promotionPrice;
        PromotionPriceCurrency = promotionPriceCurrency;
        BonusQuantity = bonusQuantity;
        BonusQuantityUOM = (bonusQuantityUOM == null || bonusQuantityUOM.isEmpty()) ? ItemPromotionData.PC_UNIT : bonusQuantityUOM;
        BonusMultipleQty = bonusMultipleQty;
        BonusMultQtyUOM = (bonusMultQtyUOM == null || bonusMultQtyUOM.isEmpty()) ? ItemPromotionData.PC_UNIT : bonusMultQtyUOM;
        StepDescriptionObj = stepDescription;
        m_CustKey = Cust_Key;
    }


    public abstract StepDescription getStepDescription(int definitionMethod, String stepsBasedUOM);
    public StepDescription getStepDescription1(int definitionMethod, String stepsBasedUOM){
           return StepDescriptionObj;
    }

    public abstract boolean isPromotionStepBonus();


}
