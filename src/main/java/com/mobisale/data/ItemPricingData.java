package com.mobisale.data;

import com.mobisale.singleton.*;
import com.mobisale.utils.Holder;
import com.mobisale.utils.LogUtil;
import com.mobisale.utils.NumberUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by israel on 5/7/14.
 */
public class ItemPricingData {
    public static final int CONDITION_TYPE_MAAM = 10;
    public static final int CONDITION_TYPE_CREDIT_TERMS = 20;
    public static final int CONDITION_TYPE_DEAL = 30;
    public static final int CONDITION_FREEZE_PRICE = 40;
    public static final int CONDITION_BASE_PRICE = 41;
    public static final int CONDITION_DIFF_FREEZE_BASE_PRICE = 42;
    public static final int CONDITION_CASH_DISCOUNT = 50;
    public static final int CONDITION_RECOMMENDED_PRICE = 60;
    public static final int CONDITION_TYPE_DEPOSIT = 70;
    public static final int CONDITION_ITEM_DISCOUNT = 80;
    public static final int CONDITION_CUSTOMER_DISCOUNT = 81;
    public static final int CONDITION_OPENING_BRANCH_DISCOUNT = 90;
    public static final int CONDITION_INSERTION_PRODUCT_DISCOUNT = 100;
    public static final int CONDITION_DEAL_DISCOUNT = 110;
    public static final int CONDITION_DEAL_NETO_DISCOUNT = 120;
    public static final int CONDITION_AGENT_DISCOUNT = 130;
    public static final int CONDITION_DEPRECIATION_BROKEN = 140;
    public static final int CONDITION_CASH_PROACTIVE_DISCOUNT = 150;
    public static final int CONDITION_FIXED_PRICE_STAT = 200;
    public static final int CONDITION_FIXED_PRICE = 201;
    public static final int CONDITION_BASKET_DISCOUNT = 210;
    public static final String PC_UNIT = "PC";
    public static final String KARTON_UNIT = "KAR";
    public static final String KG_UNIT = "KG";
    public final String PricingProcedureName;
    public final String ItemID;
    public  String PricingProcedure = "";
    private ArrayList<ItemPricingLine> itemPricingLines = new ArrayList<ItemPricingLine>();
    private float Quantity = 0;
    private double TotalValue = 0;
    private double TotalValueUrounded = 0;
    private double TotalQuantityAndValue = 0;
    private double TotalStartValue = 0;
    private double TotalDiscountValue = 0;
    private double FreezeValue = 0;
    private double MaamDiscountValue = 0;
    private double DepositValue = 0;
    private double DepositValueUnRounded = 0;
    private double PromotionValue = 0;
    private double OriginalPromotionValue = 0;
    private double SubTotalsPromotionDiscountValue = 0;
    private boolean isSubTotalsPromotion = false;
    private String CreditValue = "";
    private int lineCounter = 0;
    private String UnitType = "";
    private String PriceUnit = "";
    private boolean isUpdateAgentDiscountManually = false;
    private HashMap<String, Double> previousManualPriceValueMap = new HashMap<String, Double>();
    private boolean isResetPromotion = false;
    private boolean isRestoreManualPercentValueToOriginal = false;

    private void copyItemPricingLines(ArrayList<ItemPricingLine> lines)
    {
        lineCounter = 0;
        for(ItemPricingLine line : lines) {
            ItemPricingLine itemPricingLine = new ItemPricingLine(lineCounter++, line.StepNumber, line.ConditionValue, line.FromStep, line.ToStep,
                    line.ManualOnly, line.Requirement, line.Subtotal, line.Statistical, line.IsToResetPromotion,
                    line.AltCondBaseValue, line.ConditionData);
            itemPricingLines.add(itemPricingLine);
        }

    }
    public ItemPricingData(ItemPricingData item)
    {
        PricingProcedureName = item.PricingProcedureName;
        ItemID = item.ItemID;
        PricingProcedure = item.PricingProcedure;
        Quantity = item.Quantity;
        //taameiAsia do not have "make it zero" pricing line and TotalPrice is doubling on recompute pricing
        if (!System.getenv("PROVIDER").equalsIgnoreCase("taameiAsia") && !System.getenv("PROVIDER").equalsIgnoreCase("noa") && !System.getenv("PROVIDER").equalsIgnoreCase("laben")) {
            TotalValue = item.TotalValue;
            TotalValueUrounded = item.TotalValueUrounded;
            TotalQuantityAndValue = item.TotalQuantityAndValue;
            TotalStartValue = item.TotalStartValue;
            TotalDiscountValue = item.TotalDiscountValue;
            FreezeValue = item.FreezeValue;
            MaamDiscountValue = item.MaamDiscountValue;
            DepositValue = item.DepositValue;
            DepositValueUnRounded = item.DepositValueUnRounded;
            PromotionValue = item.PromotionValue;
            OriginalPromotionValue = item.OriginalPromotionValue;
            SubTotalsPromotionDiscountValue = item.SubTotalsPromotionDiscountValue;
            isSubTotalsPromotion = item.isSubTotalsPromotion;
            CreditValue = item.CreditValue;
        }
        UnitType = item.UnitType;
        PriceUnit = item.PriceUnit;
        isUpdateAgentDiscountManually = item.isUpdateAgentDiscountManually;
        isResetPromotion = item.isResetPromotion;
        isRestoreManualPercentValueToOriginal = item.isRestoreManualPercentValueToOriginal;

        copyItemPricingLines(item.itemPricingLines);
    }

    public ItemPricingData(String pricingProcedureName, String itemID, float quantity) {
        PricingProcedureName = pricingProcedureName;
        ItemID = itemID;
        Quantity = quantity;
    }

    public synchronized void initPricing(boolean forceUpdate, String DocNum, String RequestId, ActiveSelectionData activeSelectionData) {

        if (!forceUpdate && itemPricingLines.size() > 0) {
            return;
        }
        clearPricing();
        PricingSequenceData pricingSequence  = new PricingSequenceData();
        //LogUtil.LOG.info("initPricing 1");
        PricingProcedureListData pricingProcedureListData = PricingProceduresData.getInstance().getPricingProcedureDataByProcedureName(PricingProcedureName);
        //LogUtil.LOG.info("initPricing 2");
        PricingProcedure = pricingProcedureListData.Procedure;
        if (pricingProcedureListData == null) {
            return;
        }
       // LogUtil.LOG.info("initPricing 3");
        String conditionTypeToSkip = null;
        for (PricingProcedureData pricingProcedureData : pricingProcedureListData.pricingProcedureDatas) {
            if (pricingProcedureData.ConditionType == null)
            {
                //continue;
                LogUtil.LOG.info("condition null");
            }
            String accessSequence = ConditionsAccessData.getInstance().getAccessSequence(pricingProcedureData.ConditionType);
            LogUtil.LOG.info(RequestId + " " + "DocNum=" + DocNum  + " itemCode=" + ItemID  + " stepNumber=" + pricingProcedureData.StepNumber + " ConditionType=" + pricingProcedureData.ConditionType + " access Sequence=" + accessSequence);
            ConditionReturnListData conditionReturnListData = pricingSequence.getAccessSequenceData(pricingProcedureData.ConditionType, accessSequence, pricingProcedureData.ManualOnly, ItemID, DocNum, RequestId, activeSelectionData);
//            if ((pricingProcedureData.ConditionType == null || pricingProcedureData.ConditionType.isEmpty()) && (pricingProcedureData.Subtotal == null || pricingProcedureData.Subtotal.isEmpty()) && !pricingProcedureData.Statistical) {
//                continue;
//            }
            if (conditionTypeToSkip != null && !pricingProcedureData.ConditionType.equalsIgnoreCase(conditionTypeToSkip)) {
                continue;
            } else {
                conditionTypeToSkip = null;
            }
            if (pricingProcedureData.Requirement != null) {
                if (pricingProcedureData.Requirement.equalsIgnoreCase("602")) {
                    continue;
                } else if (pricingProcedureData.Requirement.equalsIgnoreCase("603")) {
                    continue;
                }
            }
            ConditionReturnData conditionReturnData = null;
            if (!conditionReturnListData.conditionReturnDatas.isEmpty()) {
                conditionReturnData = conditionReturnListData.conditionReturnDatas.get(0);
            }
            ItemPricingLine itemPricingLine = new ItemPricingLine(lineCounter++, pricingProcedureData.StepNumber, pricingProcedureData.ConditionType, pricingProcedureData.FromStep, pricingProcedureData.ToStep,
                    pricingProcedureData.ManualOnly, pricingProcedureData.Requirement, pricingProcedureData.Subtotal, pricingProcedureData.Statistical, pricingProcedureData.IsResetOnPromotion,
                    pricingProcedureData.AltCondBaseValue, conditionReturnData);
            itemPricingLines.add(itemPricingLine);
//            System.out.println("ItemID = " + ItemID + " , " + "lineCounter" + lineCounter + " , " + "StepNumber" + pricingProcedureData.StepNumber + " , " + "ConditionType" + pricingProcedureData.ConditionType);
            ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
            if (((conditionTypeData != null && conditionTypeData.ConditionType == CONDITION_TYPE_DEAL) || conditionReturnData != null) && (pricingProcedureData.SkipToCondition != null && !pricingProcedureData.SkipToCondition.isEmpty())) {
                conditionTypeToSkip = pricingProcedureData.SkipToCondition;
            }
        }

        LogUtil.LOG.info(RequestId + " " + "DocNum=" + DocNum  + " itemCode=" + ItemID  + " PricingProcedure " + PricingProcedure);
        for(ItemPricingLine line : itemPricingLines)
        {
            if (line.ConditionReturnValue != 0) {
                LogUtil.LOG.info(RequestId + " " + "DocNum=" + DocNum  + " itemCode=" + ItemID  +  " ConditionValue " + line.ConditionValue + " Result " + line.ConditionReturnValue);
                if (line.UnitType != null)
                    UnitType = line.UnitType;
            }
         }
        //calculate discount
        if (!isUpdateAgentDiscountPrice()) {
            TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue + getConditionReturnCalculateValueForPromotionLine(false), false);
            TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + getConditionReturnCalculateValueForPromotionLine(true), false);
        }
        TotalDiscountValue = NumberUtil.roundDoublePrecisionByParameter((1 - ((TotalValueUrounded - getDepositValueUnRounded()) / TotalStartValue)) * 100, false);
        SubTotalsPromotionDiscountValue = NumberUtil.roundDoublePrecisionByParameter((1 - (getSubTotal999() / TotalStartValue)) * 100, false);
    }

    public synchronized void updatePricing(boolean isNeedToUpdateAgentDiscountManually, float quantity) {
        TotalValue = 0;
        TotalValueUrounded = 0;
        TotalStartValue = 0;
        TotalQuantityAndValue = 0;
        TotalDiscountValue = 0;
        SubTotalsPromotionDiscountValue = 0;
        FreezeValue = 0;
        MaamDiscountValue = 0;
        DepositValue = 0;
        DepositValueUnRounded = 0;
        CreditValue = "";
        //UnitType = "";
        PriceUnit = "";
        isSubTotalsPromotion = false;
        Quantity = quantity;
        if (isNeedToUpdateAgentDiscountManually)
            isUpdateAgentDiscountManually = false;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            itemPricingLine.updatePricing(quantity);
        }
        //calculate discount
        if (!isUpdateAgentDiscountPrice()) {
            TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue + getConditionReturnCalculateValueForPromotionLine(false), false);
            TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + getConditionReturnCalculateValueForPromotionLine(true), false);
        }
        TotalDiscountValue = NumberUtil.roundDoublePrecisionByParameter((1 - ((TotalValueUrounded - getDepositValueUnRounded()) / TotalStartValue)) * 100, false);
        SubTotalsPromotionDiscountValue = NumberUtil.roundDoublePrecisionByParameter((1 - (getSubTotal999() / TotalStartValue)) * 100, false);
    }

    public synchronized  void clearPricing() {
        lineCounter = 0;
        TotalValue = 0;
        TotalValueUrounded = 0;
        TotalQuantityAndValue = 0;
        TotalStartValue = 0;
        TotalDiscountValue = 0;
        SubTotalsPromotionDiscountValue = 0;
        FreezeValue = 0;
        MaamDiscountValue = 0;
        DepositValue = 0;
        DepositValueUnRounded = 0;
        CreditValue = "";
        UnitType = "";
        PriceUnit = "";
        itemPricingLines.clear();
        isUpdateAgentDiscountManually = false;
        isSubTotalsPromotion = false;
        //Quantity = 0;
    }

    public double getItemBasePrice() {
        return TotalStartValue;
    }

    public double getItemTotalBasePrice(double quantity) {
        return TotalStartValue * quantity;
    }

    public double getItemNetoPrice() {
        return TotalValueUrounded;
    }

    public double getTotalItemPriceNeto() {
        return TotalQuantityAndValue;
    }

    public double RecalculateTotalLine(double quantity){
        return TotalValueUrounded * quantity;
    }

    public double getItemDiscountValue() {
        return TotalDiscountValue;
    }

    public double getSubTotalsPromotionDiscountValue() {
        return SubTotalsPromotionDiscountValue;
    }

    public double getMaamDiscountValue() {
        return MaamDiscountValue;
    }

    public double getDepositValue() {
        return DepositValueUnRounded;
    }

    public double getDepositValueUnRounded() {
        return DepositValueUnRounded;
    }

    public String getCreditValue() {
        return CreditValue;
    }

    public String getPriceUnitType() {
        return UnitType;
    }

    public String getPriceUnit() {
        return PriceUnit;
    }

    public double getPromotionValue() {
        return PromotionValue;
    }

    public double getOriginalPromotionValue() {
        return OriginalPromotionValue;
    }

    public boolean isRestoreManualPercentValueToOriginal() {
        return isRestoreManualPercentValueToOriginal;
    }

    public double getPercentValueFromConditionType(int type) {
        double percentValue = 0;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            if (itemPricingLine.ConditionData == null) continue;
            int conditionType = -1;
            ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
            if (conditionTypeData != null) {
                conditionType = conditionTypeData.ConditionType;
            }
            if (conditionType == type) {
                if (itemPricingLine.ManualOnly) {
                    percentValue = itemPricingLine.ManualPercentValue;
                } else {
                    percentValue = itemPricingLine.ConditionReturnValue;
                }
                break;
            }
        }
        return percentValue;
    }

    public double getPercentValueFromConditionValue(String conditionValue) {
        double percentValue = 0;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            if (conditionValue.equalsIgnoreCase(itemPricingLine.ConditionValue)) {
                if (itemPricingLine.ManualOnly) {
                    percentValue = itemPricingLine.ManualPercentValue;
                } else {
                    percentValue = itemPricingLine.ConditionReturnValue;
                }
                break;
            }
        }
        return percentValue;
    }

    public boolean isBasePriceHaveManualOnly() {
        boolean retValue = false;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            int type = -1;
            ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
            if (conditionTypeData != null) {
                type = conditionTypeData.ConditionType;
            }
            if (type == CONDITION_BASE_PRICE) {
                if (itemPricingLine.ConditionData != null && itemPricingLine.ManualOnly) {
                    retValue = true;
                    break;
                }
            }
        }
        return retValue;
    }

    public boolean isHaveManualOnly() {
        boolean retValue = false;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            int type = -1;
            ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
            if (conditionTypeData != null) {
                type = conditionTypeData.ConditionType;
            }
            if (type == CONDITION_BASE_PRICE) {
                continue;
            }
            if (type == CONDITION_TYPE_DEAL) {
                continue;
            }
            if (itemPricingLine.ManualOnly) {
                if (type == CONDITION_AGENT_DISCOUNT && true) {
                    continue;
                }
                retValue = true;
                break;
            }
        }
        return retValue;
    }

    public double getOriginalManualPercent(String conditionValue) {
        double retValue = 0;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            if (conditionValue.equalsIgnoreCase(itemPricingLine.ConditionValue)) {
                retValue = itemPricingLine.ConditionReturnValue;
                break;
            }
        }
        return retValue;
    }


    public void updatePromotionCondition(double itemDiscountValueWithoutExcludeDiscount, double itemDiscountValue, boolean excludeDiscount, boolean isPromotionClassification, boolean needToRound) {
        synchronized (itemPricingLines) {
            double promotionValueWithoutExcludeDiscount = NumberUtil.roundDoublePrecisionByParameter(itemDiscountValueWithoutExcludeDiscount == -1 ? 0 : -itemDiscountValueWithoutExcludeDiscount, needToRound);
            PromotionValue = NumberUtil.roundDoublePrecisionByParameter(itemDiscountValue == -1 ? 0 : -itemDiscountValue, needToRound);
            OriginalPromotionValue = promotionValueWithoutExcludeDiscount;
            if (itemPricingLines.size() > 0) {
                for (ItemPricingLine itemPricingLine : itemPricingLines) {

                    if (itemPricingLine.IsToResetPromotion) {
                        itemPricingLine.Statistical = itemDiscountValue > 0;
                    }

                    int type = -1;
                    ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
                    if (conditionTypeData != null) {
                        type = conditionTypeData.ConditionType;
                    }
                    if (type == CONDITION_TYPE_DEAL) {
                        itemPricingLine.ConditionReturnValue = NumberUtil.roundDoublePrecisionByParameter(PromotionValue, needToRound);
                        itemPricingLine.ExcludeDiscount = excludeDiscount;
                        itemPricingLine.IsPromotionClassification = isPromotionClassification;
                        itemPricingLine.NeedToRound = needToRound;
                        if (isResetPromotion) {
                            updatePromotionConditionAfterManualDiscount(true);
                        } else {
                            updatePricing(false, Quantity);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void updatePromotionConditionAfterManualDiscount(boolean isStatistical) {
        synchronized (itemPricingLines) {
            if (itemPricingLines.size() > 0) {
                for (ItemPricingLine itemPricingLine : itemPricingLines) {
                    int type = -1;
                    ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
                    if (conditionTypeData != null) {
                        type = conditionTypeData.ConditionType;
                    }
                    if (type == CONDITION_TYPE_DEAL) {
                        PromotionValue = 0;
                        itemPricingLine.ConditionReturnValue = NumberUtil.roundDoublePrecisionByParameter(PromotionValue, true);
                        itemPricingLine.Statistical = isStatistical;
                        updatePricing(false, Quantity);
                        break;
                    }
                }
            }
        }
    }

    public ArrayList<ItemPricingLine> getItemPricingLines() {
        ArrayList<ItemPricingLine> pricingLines = new ArrayList<ItemPricingLine>();
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            if (!isEmpty(itemPricingLine.ConditionValue) || !isEmpty(itemPricingLine.TableName) || itemPricingLine.ConditionReturnCalculateValue > 0) {
                pricingLines.add(itemPricingLine);
            }
        }
        return pricingLines;
    }

    public ArrayList<ItemPricingLine> getItemPricingLinesForShow(Holder<Boolean> hasFixedPrice) {
        ArrayList<ItemPricingLine> pricingLines = new ArrayList<ItemPricingLine>();
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            if (itemPricingLine.ConditionReturnValue != 0) {
                if (itemPricingLine.IsFixedPrice)
                    hasFixedPrice.value = true;
                pricingLines.add(itemPricingLine);
            }
        }
        return pricingLines;
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private synchronized double getSubTotal999() {
        double retValue = 0;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            if (itemPricingLine.Subtotal.equalsIgnoreCase("1") || itemPricingLine.Subtotal.equalsIgnoreCase("5") || itemPricingLine.Subtotal.equalsIgnoreCase("6")) {
                if (itemPricingLine.Subtotal.equalsIgnoreCase("1")) {
                    retValue += NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.TotalValueForLineUnrounded, false);
                    continue;
                }
                retValue += NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValueUnrounded, false);
            }
        }
        return retValue;
    }

    private synchronized double getSubTotal992() {
        double retValue = 0;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            if (itemPricingLine.Subtotal.equalsIgnoreCase("F") || itemPricingLine.Subtotal.equalsIgnoreCase("1") || itemPricingLine.Subtotal.equalsIgnoreCase("5") || itemPricingLine.Subtotal.equalsIgnoreCase("6")) {
                if (itemPricingLine.Subtotal.equalsIgnoreCase("1")) {
                    retValue += NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.TotalValueForLineUnrounded, false);
                    continue;
                }
                retValue += NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.IsPromotionClassification ? 0 : itemPricingLine.ConditionReturnCalculateValueUnrounded, false);
            }
        }
        return retValue;
    }

    private synchronized double getSubTotal997() {
        double retValue = 0;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            if (itemPricingLine.Subtotal.equalsIgnoreCase("F") || itemPricingLine.Subtotal.equalsIgnoreCase("1") || itemPricingLine.Subtotal.equalsIgnoreCase("5") || itemPricingLine.Subtotal.equalsIgnoreCase("6")) {
                if (itemPricingLine.Subtotal.equalsIgnoreCase("1")) {
                    retValue += NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.TotalValueForLineUnrounded, false);
                    continue;
                }
                retValue += NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.IsPromotionClassification ? 0 : itemPricingLine.ConditionReturnCalculateValueUnrounded, false);
            }
        }
        // ADD AGENT RETURN VALUE
        return retValue;
    }

    private synchronized double getConditionReturnCalculateValueForPromotionLine(boolean isUnRound) {
        double conditionReturnCalculateValueForPromotionLine = 0;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            int type = -1;
            ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
            if (conditionTypeData != null) {
                type = conditionTypeData.ConditionType;
            }
            if (type == CONDITION_TYPE_DEAL) {
                if (itemPricingLine.Statistical) {
                    if (isUnRound) {
                        conditionReturnCalculateValueForPromotionLine = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValueUnrounded, false);
                    } else {
                        conditionReturnCalculateValueForPromotionLine = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValue, false);
                    }
                }
                break;
            }
        }
        return conditionReturnCalculateValueForPromotionLine;
    }

    private synchronized boolean isUpdateAgentDiscountPrice() {
        boolean isUpdateAgentDiscountPrice = false;
        for (ItemPricingLine itemPricingLine : itemPricingLines) {
            int type = -1;
            ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
            if (conditionTypeData != null) {
                type = conditionTypeData.ConditionType;
            }
            if (type == CONDITION_AGENT_DISCOUNT) {
                if (itemPricingLine.ConditionReturnCalculateValue < 0) {
                    isUpdateAgentDiscountPrice = true;
                }
                break;
            }
        }
        return isUpdateAgentDiscountPrice;
    }

    public class ItemPricingLine {
        public final int PricingLineNum;
        public final int StepNumber;
        public final String ConditionValue;
        public String ConditionDescription = "";
        public final int FromStep;
        public final int ToStep;
        public final boolean ManualOnly;
        public boolean IsFixedPrice;
        public final String Requirement;
        public final String Subtotal;
        public boolean Statistical;
        public final boolean IsToResetPromotion;
        public final String AltCondBaseValue;
        public final String TableName;
        //for calculation
        public double ConditionReturnValue;
        public int ConditionReturnValueType;
        public final ConditionReturnData ConditionData;
        public double ConditionReturnCalculateValue;
        public double ConditionReturnCalculateValueBasket;
        public double ConditionReturnCalculateValueUnrounded;
        public double TotalQuantityConditionReturnCalculateValue;
        public double ManualPercentValue = 0;
        public double ManualPercentFrom;
        public double ManualPercentTo;
        public double ManualPriceFrom;
        public double ManualPriceTo;
        public double StatisticalValue;
        public double TotalValueForLine;
        public double TotalValueForLineUnrounded;
        public double TotalQuantityAndValueForLine;
        public boolean ExcludeDiscount = false;
        public boolean IsPromotionClassification = false;
        public boolean NeedToRound = false;
        public String UnitType;

        ItemPricingLine(int pricingLineNum, int stepNumber, String conditionValue, int fromStep, int toStep, boolean manualOnly, String requirement, String subtotal, boolean statistical, boolean isToResetPromotion, String altCondBaseValue, ConditionReturnData conditionReturnData) {
            PricingLineNum = pricingLineNum;
            StepNumber = stepNumber;
            ConditionValue = conditionValue == null ? "" : conditionValue;
            FromStep = fromStep;
            ToStep = toStep;
            ManualOnly = manualOnly;
            Requirement = requirement;
            Subtotal = subtotal;
            Statistical = statistical;
            IsToResetPromotion = isToResetPromotion;
            AltCondBaseValue = altCondBaseValue;
            ConditionData = conditionReturnData;
            TableName = ConditionData == null ? "" : ConditionData.TableName == null ? "" : ConditionData.TableName;
            ConditionReturnValue = ConditionData == null ? 0 : NumberUtil.roundDoublePrecision(ConditionData.ReturnValue);
            ManualPercentValue = ConditionReturnValue;
            ConditionReturnValueType = ConditionData == null ? 0 : ConditionData.ReturnValueType;
            updatePricing(Quantity);
            //if (ConditionData != null && ConditionData.UnitType > 0)
            //    UnitType = ConditionData.UnitType;
        }

        private synchronized void updatePricing(float quantity) {
            Quantity = quantity;
            if (ConditionValue.equalsIgnoreCase("ZCP1")) {
                return;
            }
            int type = -1;
            ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(ConditionValue);
            if (conditionTypeData != null) {
                ConditionDescription = conditionTypeData.Comment;
                type = conditionTypeData.ConditionType;
            }
            switch (type) {
                case CONDITION_FREEZE_PRICE:
                    updateFreezePrice();
                    break;
                case CONDITION_BASE_PRICE:
                    updateBasePrice();
                    break;
                case CONDITION_DIFF_FREEZE_BASE_PRICE:
                    updateBasePriceAfterFreeze();
                    break;
                case CONDITION_TYPE_DEAL:
                    updatePromotionPrice();
                    break;
                case CONDITION_CASH_DISCOUNT:
                    updateStepPrice();
                    break;
                case CONDITION_CUSTOMER_DISCOUNT:
                    //Todo request from sql table.
                    //Marina - let's do upon ConditionReturnValue
                    //double percentValue = 10;
                    if (ManualOnly) {
                        ManualPercentValue = -Math.abs(ManualPercentValue);
                    } else {
                        ConditionReturnValue = -Math.abs(ConditionReturnValue);
                    }
                    updateStepPrice();
                    break;
                case CONDITION_AGENT_DISCOUNT:
                    updateAgentDiscountPrice();
                    break;
                case CONDITION_TYPE_MAAM:
                    updateMaamPrice();
                    break;
                case CONDITION_TYPE_CREDIT_TERMS:
                    updateCreditPrice();
                    break;
                case CONDITION_TYPE_DEPOSIT:
                    updateDepositDiscountPrice();
                    break;
                case CONDITION_FIXED_PRICE_STAT:
                    updateFixedPriceStat();
                    break;
                case CONDITION_FIXED_PRICE:
                    updateFixedPrice();
                    break;
                case CONDITION_BASKET_DISCOUNT:
                    updateBasketDiscount();
                    break;
                default:
                    updateStepPrice();
                    break;
            }
        }

        private synchronized void updateFixedPriceStat() {
            if (ConditionData != null) {
                IsFixedPrice = true;
                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
                TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                if (Statistical) {
                    TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                    TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                    TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
                } else {
                    TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                    TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                    TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
                }
            } else {
                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * TotalValue, true);
                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * TotalValue, false);
                TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
            }
            if (Statistical) {
                StatisticalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLine, false);
            } else if (ConditionData != null) {
                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
            } else {
                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
            }
        }

        private synchronized void updateFixedPrice() {
            if (FromStep > 0) {
                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(0, true);
                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(0, false);
                TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                for (ItemPricingLine itemPricingLine : itemPricingLines) {
                    if (itemPricingLine.StepNumber == FromStep) {
                        IsFixedPrice = true;
                        ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValue, true);
                        ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValueUnrounded, false);
                        TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.TotalQuantityConditionReturnCalculateValue, true);
                        break;
                    }
                }
            }
            ItemPricingLine fixedPriceStatLine = null;
            for (ItemPricingLine itemPricingLine : itemPricingLines) {
                ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
                if (conditionTypeData != null) {
                    if (conditionTypeData.ConditionType == CONDITION_FIXED_PRICE_STAT) {
                        if (itemPricingLine.ConditionData != null) {
                            fixedPriceStatLine = itemPricingLine;
                        }
                        break;
                    }
                }
            }
            if (fixedPriceStatLine != null) {
                int altCondBaseValueAsInt = 0;
                if (AltCondBaseValue != null && !AltCondBaseValue.isEmpty()) {
                    try {
                        altCondBaseValueAsInt = Integer.valueOf(AltCondBaseValue);
                    } catch (Exception e) {
                        LogUtil.LOG.error("This Will Be Printed On Error12");

                    }
                }
                if (altCondBaseValueAsInt == 998) {
                    for (ItemPricingLine itemPricingLine : itemPricingLines) {
                        ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(itemPricingLine.ConditionValue);
                        if (conditionTypeData != null) {
                            if (conditionTypeData.ConditionType == CONDITION_OPENING_BRANCH_DISCOUNT) {
                                IsFixedPrice = true;
                                ConditionReturnValue = -(itemPricingLine.ConditionReturnCalculateValueUnrounded / ConditionReturnCalculateValueUnrounded) * 100;
                                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValue, true);
                                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValueUnrounded, false);
                                TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.TotalQuantityConditionReturnCalculateValue, true);

                                TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(fixedPriceStatLine.ConditionReturnCalculateValue + ConditionReturnCalculateValue, false);
                                TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(fixedPriceStatLine.ConditionReturnCalculateValueUnrounded + ConditionReturnCalculateValueUnrounded, false);
                                TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(fixedPriceStatLine.TotalQuantityConditionReturnCalculateValue + TotalQuantityConditionReturnCalculateValue, true);

                                TotalValue = NumberUtil.roundDoublePrecisionByParameter(fixedPriceStatLine.ConditionReturnCalculateValue + ConditionReturnCalculateValue, false);
                                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(fixedPriceStatLine.ConditionReturnCalculateValueUnrounded + ConditionReturnCalculateValueUnrounded, false);
                                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(fixedPriceStatLine.TotalQuantityConditionReturnCalculateValue + TotalQuantityConditionReturnCalculateValue, true);

                                break;
                            }
                        }
                    }
                }
            } else {
                TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);

                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
            }

        }

        private synchronized void updateFreezePrice() {
            ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
            ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
            TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
            TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValue, false);
            TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded, false);
            TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityConditionReturnCalculateValue, true);
            FreezeValue = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLine, false);
            if (Statistical) {
                StatisticalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLine, false);
            } else if (ConditionData != null) {
                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
            }
        }

        private synchronized void updateBasePrice() {
            if (ConditionData != null) {
                if (UnitType == null || UnitType.isEmpty()) {
                    if (ConditionData.UnitType == ConditionReturnData.UNIT_TYPE_PC) {
                        UnitType = PC_UNIT;
                    } else if (ConditionData.UnitType == ConditionReturnData.UNIT_TYPE_KAR) {
                        UnitType = KARTON_UNIT;
                    } else if (ConditionData.UnitType == ConditionReturnData.UNIT_TYPE_KG) {
                        UnitType = KG_UNIT;
                    }
                }
                if (PriceUnit == null || PriceUnit.isEmpty()) {
                    PriceUnit = ConditionData.PriceUnit;
                }
            } else {
                UnitType = PC_UNIT;
            }
            if (ManualOnly && ConditionData != null) {
                ManualPercentFrom = NumberUtil.roundDoublePrecisionByParameter(((ConditionData.DiscountFrom - ConditionReturnValue) / ConditionReturnValue) * 100, true);
                ManualPercentTo = NumberUtil.roundDoublePrecisionByParameter((ConditionData.DiscountTo / ConditionReturnValue) * 100, true);
                ManualPriceFrom = NumberUtil.roundDoublePrecisionByParameter(ConditionData.DiscountFrom, true);
                ManualPriceTo = NumberUtil.roundDoublePrecisionByParameter(ConditionData.DiscountTo, true);
                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ManualPercentValue, true);
                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ManualPercentValue, false);
            } else {
                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
            }
            TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
            TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
            TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
            TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
            if (Statistical) {
                StatisticalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLine, false);
            } else if (ConditionData != null) {
                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
                TotalStartValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
            }
        }

        private synchronized void updateBasePriceAfterFreeze() {
            if (FreezeValue == 0) {
                ConditionReturnValue = NumberUtil.roundDoublePrecisionByParameter(FreezeValue, false);
                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
                TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValue, false);
                TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded, false);
                TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityConditionReturnCalculateValue, true);
                TotalStartValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
            } else {
                ConditionReturnValue = NumberUtil.roundDoublePrecisionByParameter(FreezeValue - TotalValue, false);
                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
                TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValue, false);
                TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded, false);
                TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityConditionReturnCalculateValue, true);
                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue + TotalValueForLine, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + TotalValueForLineUnrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityAndValueForLine, true);
                TotalStartValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
            }
        }

        private synchronized void updateBasketDiscount(){
            updateStepPrice();
            ConditionReturnCalculateValueBasket = Math.abs(ConditionReturnValue);
        }

        private synchronized void updateStepPrice() {
            if (ConditionData != null) {
                if (ManualOnly) {
                    ManualPercentFrom = NumberUtil.roundDoublePrecisionByParameter(ConditionData.DiscountFrom, true);
                    ManualPercentTo = NumberUtil.roundDoublePrecisionByParameter(ConditionData.DiscountTo, true);
                    ManualPriceFrom = NumberUtil.roundDoublePrecisionByParameter((ManualPercentFrom / 100) * TotalValue, true);
                    ManualPriceTo = NumberUtil.roundDoublePrecisionByParameter((ManualPercentTo / 100) * TotalValue, true);
                    ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ManualPercentValue / 100) * TotalValue, true);
                    ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ManualPercentValue / 100) * TotalValue, false);
                    TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                    TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                    TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
                } else {
                    if (ConditionReturnValueType == ConditionReturnData.RETURN_VALUE_TYPE_DISCOUNT) {
                        if (FromStep > 0) {
                            for (ItemPricingLine itemPricingLine : itemPricingLines) {
                                if (itemPricingLine.StepNumber == FromStep) {
                                    ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * itemPricingLine.TotalValueForLine, true);
                                    ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * itemPricingLine.TotalValueForLine, false);
                                    TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                                    TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.TotalValueForLine + ConditionReturnCalculateValue, false);
                                    TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.TotalValueForLineUnrounded + ConditionReturnCalculateValueUnrounded, false);
                                    TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.TotalQuantityAndValueForLine + TotalQuantityConditionReturnCalculateValue, true);
                                    break;
                                }
                            }
                        } else {
                            int altCondBaseValueAsInt = 0;
                            double value = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                            if (AltCondBaseValue != null && !AltCondBaseValue.isEmpty()) {
                                try {
                                    altCondBaseValueAsInt = Integer.valueOf(AltCondBaseValue);
                                } catch (Exception e) {
                                    LogUtil.LOG.error("This Will Be Printed On Error12");

                                }
                            }
                            if (altCondBaseValueAsInt == 992) {
                                value = getSubTotal992();
                            } else if (altCondBaseValueAsInt == 997) {
                                value = getSubTotal997();
                            } else if (altCondBaseValueAsInt == 991) {
                                value = getSubTotal999();
                            }
                            ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * value, true);
                            ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * value, false);
                            TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                            if (Statistical) {
                                TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                                TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                                TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
                            } else {
                                TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                                TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                                TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
                            }
                        }
                    } else {
                        ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                        ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
                        TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                        if (Statistical) {
                            TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                            TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                            TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
                        } else {
                            TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                            TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                            TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
                        }
                    }
                }
            } else {
                if (FromStep > 0 && ToStep > 0) {
                    ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(0, true);
                    ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(0, false);
                    TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    for (ItemPricingLine itemPricingLine : itemPricingLines) {
                        if (itemPricingLine.StepNumber >= FromStep && itemPricingLine.StepNumber <= ToStep) {
                            ConditionReturnCalculateValue += NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValue, true);
                            ConditionReturnCalculateValueUnrounded += NumberUtil.roundDoublePrecisionByParameter(itemPricingLine.ConditionReturnCalculateValueUnrounded, false);
                        }
                    }
                    TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                    TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                    TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
                } else {
                    ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * TotalValue, true);
                    ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * TotalValue, false);
                    TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                    TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                    TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
                }
            }
            if (Statistical) {
                StatisticalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLine, false);
            } else if (ConditionData != null) {
                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
            } else {
                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue, true);
            }
        }

        private synchronized void updateAgentDiscountPrice() {
            int altCondBaseValueAsInt = 0;
            double value = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
            if (AltCondBaseValue != null) {
                try {
                    altCondBaseValueAsInt = Integer.valueOf(AltCondBaseValue);
                } catch (Exception e) {
                    LogUtil.LOG.error("This Will Be Printed On Error107");
                }
            }
            if (altCondBaseValueAsInt == 992) {
                value = getSubTotal992();
            } else if (altCondBaseValueAsInt == 997) {
                value = getSubTotal997();
            }
            if (ConditionData != null) {
                if (ManualOnly) {
                    ManualPercentFrom = NumberUtil.roundDoublePrecisionByParameter(ConditionData.DiscountFrom, true);
                    ManualPercentTo = NumberUtil.roundDoublePrecisionByParameter(ConditionData.DiscountTo, true);
                    ManualPriceFrom = NumberUtil.roundDoublePrecisionByParameter((ManualPercentFrom / 100) * value, true);
                    ManualPriceTo = NumberUtil.roundDoublePrecisionByParameter((ManualPercentTo / 100) * value, true);
                    if (false) {
                        if (!isUpdateAgentDiscountManually)
                            ManualPercentValue = NumberUtil.roundDoublePrecisionByParameter(ManualPercentFrom, true);
                    } else {
                        ManualPercentValue = NumberUtil.roundDoublePrecisionByParameter(0, true);
                    }
                    ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ManualPercentValue / 100) * value, true);
                    ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ManualPercentValue / 100) * value, false);
                    TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                } else {
                    if (ConditionReturnValueType == ConditionReturnData.RETURN_VALUE_TYPE_DISCOUNT) {
                        ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * value, true);
                        ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * value, false);
                        TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    } else {
                        ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                        ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
                        TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    }
                }
                if (ConditionReturnCalculateValue < 0) {
                    TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter((TotalValue + getConditionReturnCalculateValueForPromotionLine(false)) + ConditionReturnCalculateValue, false);
                    TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter((TotalValueUrounded + getConditionReturnCalculateValueForPromotionLine(true)) + ConditionReturnCalculateValueUnrounded, false);
                    TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + getConditionReturnCalculateValueForPromotionLine(true) * Quantity + TotalQuantityConditionReturnCalculateValue, true);
                } else {
                    TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
                    TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
                    TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
                }
                if (false) {
                    TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLine, false);
                    TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLineUnrounded, false);
                    TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValueForLine, true);
                }
            }
        }

        private void updateDepositDiscountPrice() {
            int unitInKar = 1; // todo add from item data json.
            if (ConditionData != null) {
                if (UnitType.equalsIgnoreCase(PC_UNIT)) {
                    if (ConditionData.UnitType == ConditionReturnData.UNIT_TYPE_PC) {
                        ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                        ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
                        TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    } else {
                        ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue / unitInKar, true);
                        ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue / unitInKar, false);
                        TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    }
                } else {
                    if (ConditionData.UnitType == ConditionReturnData.UNIT_TYPE_PC) {
                        ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue * unitInKar, true);
                        ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue * unitInKar, false);
                        TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    } else {
                        ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                        ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
                        TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
                    }
                }
            } else {
                ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
                ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, false);
                TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
            }
            TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
            TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
            TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
            DepositValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValue, false);
            DepositValueUnRounded = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded, false);
            TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLine, false);
            TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLineUnrounded, false);
            TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValueForLine, true);
        }

        private synchronized void updatePromotionPrice() {
            int altCondBaseValueAsInt = 0;
            double value = NumberUtil.roundDoublePrecisionByParameter(TotalValue, false);
            if (AltCondBaseValue != null && !AltCondBaseValue.isEmpty()) {
                try {
                    altCondBaseValueAsInt = Integer.valueOf(AltCondBaseValue);
                } catch (Exception e) {
                    LogUtil.LOG.error("This Will Be Printed On Error13 - AltCondBaseValue=" + AltCondBaseValue);
                }
            }
            if (altCondBaseValueAsInt == 992) {
                value = getSubTotal992();
            } else if (altCondBaseValueAsInt == 997) {
                value = getSubTotal997();
            } else if (altCondBaseValueAsInt == 999) {
                value = getSubTotal999();
            }
            if (altCondBaseValueAsInt == 999) {
                isSubTotalsPromotion = true;
            }
            ConditionReturnValue = NumberUtil.roundDoublePrecisionByParameter(PromotionValue, NeedToRound);
            ConditionReturnValueType = ConditionReturnData.RETURN_VALUE_TYPE_DISCOUNT;
//            double totalValue = excludeDiscount ? TotalStartValue : TotalValue - DepositValue;
            double totalValue = NumberUtil.roundDoublePrecisionByParameter(ExcludeDiscount ? TotalStartValue : value, false);
            ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * totalValue, true);
            ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * totalValue, false);
            TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
            TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false)/* - DepositValue*/;
            TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false)/* - DepositValue*/;
            TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
            if (!Statistical) {
                TotalValue = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLine, false);
                TotalValueUrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueForLineUnrounded, false);
                TotalQuantityAndValue = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValueForLine, true);
            }
        }

        private synchronized void updateMaamPrice() {
            ConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * TotalValue, true);
            ConditionReturnCalculateValueUnrounded = NumberUtil.roundDoublePrecisionByParameter((ConditionReturnValue / 100) * TotalValueUrounded, false);
            TotalQuantityConditionReturnCalculateValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnCalculateValueUnrounded * Quantity, true);
            TotalValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalValue + ConditionReturnCalculateValue, false);
            TotalValueForLineUnrounded = NumberUtil.roundDoublePrecisionByParameter(TotalValueUrounded + ConditionReturnCalculateValueUnrounded, false);
            TotalQuantityAndValueForLine = NumberUtil.roundDoublePrecisionByParameter(TotalQuantityAndValue + TotalQuantityConditionReturnCalculateValue, true);
            MaamDiscountValue = NumberUtil.roundDoublePrecisionByParameter(ConditionReturnValue, true);
        }

        private synchronized void updateCreditPrice() {
            if (ConditionData != null) {
                CreditValue = ConditionData.CreditTerms;
            }
        }

        public String getCreditValueForLine() {
            return CreditValue;
        }

        public double getPreviousLineValue() {
            if (ManualPercentValue == 0.0f) {
                return TotalValueForLine;
            }
            if (ManualPercentValue == -100f) {
                return -ConditionReturnCalculateValue;
            }
            if ((100.0f + ManualPercentValue) == 0.0f) {
                return 0.0f;
            }
            double previousLineValue = (100.0f * TotalValueForLine) / (100.0f + ManualPercentValue);
            return previousLineValue;
        }
    }
}
