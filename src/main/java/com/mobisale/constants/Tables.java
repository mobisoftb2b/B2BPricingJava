package com.mobisale.constants;

public class Tables {
    public static String GetFullTableName(String tableName)
    {
        return "[dbo].[" + tableName + "]";
    }
    public static String TABLE_PRICING_SEQUENCE = "Prc_AccSequence_T682I";
    public static String TABLE_PRICING_CONDITION = "Prc_ConditionFields_T682Z";
    public static String TABLE_PRICING_PROCEDURES = "PricingProcedures_T683S";
    public static String TABLE_PRICING_CONDITION_TYPES = "MTN_Prc_ConditionTypes";
    public static String TABLE_PRICING_CONDITIONS_ACCESS = "Prc_Condtions_Access_T685";
    //public static String TABLE_PRICING_DOC_PROCEDURE = "[dbo].[PricingDocProcedure_T683V]";
    public static String TABLE_MTN_MAPPING = "[dbo].[MTN_Mapping]";
    public static String TABLE_ITEMS = "B2B_Items";
    public static String TABLE_ITEMS_PRICING = "B2B_Pricing_Items";
    public static String TABLE_ITEMS_SQLITE = "B2B_Pricing_Items";
    public static String TABLE_CUSTOMERS = "B2B_Customers";
    public static String TABLE_CUSTOMERS_PRICING = "B2B_Pricing_Customers";
    public static String TABLE_CUSTCATEGORY = "B2B_CustCategory";
}
