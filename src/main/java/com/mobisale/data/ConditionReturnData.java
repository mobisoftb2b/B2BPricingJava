package com.mtn.mobisale.data;

/**
 * Created by israel on 3/16/14.
 */
public class ConditionReturnData {
    public static final int RETURN_VALUE_TYPE_PRICE = 1;
    public static final int RETURN_VALUE_TYPE_DISCOUNT = 2;
    //
    public static final int UNIT_TYPE_PC = 1;
    public static final int UNIT_TYPE_KAR = 2;
    public static final int UNIT_TYPE_KG = 3;

    public final String AccessSequence;
    public final int Access;
    public final String TableName;
    public final String SapConstant;
    public final int ReturnValueType;
    public float ReturnValue;
    public final String CreditTerms;
    public float DiscountTo;
    public float DiscountFrom;
    public final int UnitType;
    public final String PriceUnit;

    public ConditionReturnData(String accessSequence, int access, String tableName, String sapConstant, int returnValueType, float returnValue, String creditTerms, float discountFrom, float discountTo, int unitType, int divideValue, String priceUnit) {
        AccessSequence = accessSequence;
        Access = access;
        TableName = tableName;
        SapConstant = sapConstant;
        ReturnValueType = returnValueType;
        switch (ReturnValueType){
            case RETURN_VALUE_TYPE_DISCOUNT:
                ReturnValue = returnValue / 10;
                DiscountFrom = discountFrom / 10;
                DiscountTo = discountTo / 10;
                break;
            case RETURN_VALUE_TYPE_PRICE:
                ReturnValue = returnValue / divideValue;
                DiscountFrom = discountFrom / divideValue;
                DiscountTo = discountTo / divideValue;
                break;
        }
        CreditTerms = creditTerms;
        UnitType = unitType;
        PriceUnit = priceUnit;
    }
}
