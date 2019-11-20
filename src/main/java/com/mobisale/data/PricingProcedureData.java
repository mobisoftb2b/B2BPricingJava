package com.mobisale.data;

/**
 * Created by israel on 3/17/14.
 */
public class PricingProcedureData {
    public final int StepNumber;
    public final String ConditionType;
    public final int FromStep;
    public final int ToStep;
    public final boolean ManualOnly;
    public final String Requirement;
    public final String Subtotal;
    public final boolean Statistical;
    public final String AltCondBaseValue;
    public final String SkipToCondition;
    public final boolean IsResetOnPromotion;

    public PricingProcedureData(int stepNumber, String conditionType, int fromStep, int toStep, boolean manualOnly, String requirement, String subtotal, boolean statistical, String altCondBaseValue, String skipToCondition, boolean isResetOnPromotion) {
        StepNumber = stepNumber;
        ConditionType = conditionType;
        FromStep = fromStep;
        ToStep = toStep;
        ManualOnly = manualOnly;
        Requirement = requirement;
        Subtotal = subtotal;
        Statistical = statistical;
        AltCondBaseValue = altCondBaseValue;
        SkipToCondition = skipToCondition;
        IsResetOnPromotion = isResetOnPromotion;
    }
}
