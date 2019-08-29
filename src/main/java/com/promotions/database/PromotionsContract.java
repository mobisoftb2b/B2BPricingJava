package com.promotions.database;


public class PromotionsContract {

    interface PromotionItemsBonusColumn {
        String PROMOTION_BONUS_ESP_NUMBER = "ESPNumber";
        String PROMOTION_BONUS_RECORD_NUMBER = "RecordNumber";
        String PROMOTION_BONUS_STEP = "Step";
        String PROMOTION_BONUS_INCLUDE_EXCLUDE_SIGN = "IncludeExcludeSign";
        String PROMOTION_BONUS_SELECTED_CHARACTERISTICS = "SelectedCharacteristics";
        String PROMOTION_BONUS_MATERIAL_CHAR_VALUE = "MaterialCharValue";
    }

    interface PromotionBonusColumn {
        String PROMOTION_BONUS_ESP_NUMBER = "ESPNumber";
        String PROMOTION_BONUS_RECORD_NUMBER = "RecordNumber";
        String PROMOTION_BONUS_STEP = "Step";
        String PROMOTION_BONUS_REC_NUMBER = "BonusRecNumber";
        String PROMOTION_BONUS_RECORD_SEQUENCE = "RecordSequence";
    }

    interface PromotionHeaderColumn {
        String PROMOTION_HEADER_ESP_NUMBER = "ESPNumber";
        String PROMOTION_HEADER_ESP_TYPE = "ESPType";
        String PROMOTION_HEADER_START_DATE = "StartDate";
        String PROMOTION_HEADER_END_DATE = "EndDate";
        String PROMOTION_HEADER_SALES_ORGANIZATION = "SalesOrganization";
        String PROMOTION_HEADER_DISTRIBUTION_CHANNEL = "DistributionChannel";
        String PROMOTION_HEADER_DEFINITION_METHOD = "DefinitionMethod";
        String PROMOTION_HEADER_EXCLUDE_DISCOUNT = "ExcludeDiscount";
        String PROMOTION_HEADER_STEPS_BASED_UOM = "StepsBasedUOM";
        String PROMOTION_HEADER_STEPS_BASED_CUR = "StepsBasedCur";
        String PROMOTION_HEADER_NO_BLOCK_ESP = "NoBlockESP";
        String PROMOTION_HEADER_PROMOTION_IN_FOCUS = "PromoInFocus";
        String PROMOTION_HEADER_ESP_PRIORITY = "ESPPriority";
        String PROMOTION_HEADER_ESP_STATUS = "ESPStatus";
        String PROMOTION_HEADER_BASKET_ESP = "BasketESP";
        String PROMOTION_HEADER_BONUS_ESP = "BonusESP";
        String PROMOTION_HEADER_ESP_DESCRIPTION = "ESPDescription";
        String PROMOTION_HEADER_ESP_RETURN_RELEVANT = "ESPReturnRelevant";
        String PROMOTION_HEADER_ESP_CLASSIFICATION = "Classification";
        String PROMOTION_HEADER_AGENT_ID = "Agentid";
    }

    interface PromotionItemsColumn {
        String PROMOTION_ITEMS_ESP_NUMBER = "ESPNumber";
        String PROMOTION_ITEMS_RECORD_NUMBER = "RecordNumber";
        String PROMOTION_ITEMS_RECORD_SEQUENCE = "RecordSequence";
        String PROMOTION_ITEMS_INCLUDE_EXCLUDE_SIGN = "IncludeExcludeSign";
        String PROMOTION_ITEMS_SELECTED_CHARACTERISTICS = "SelectedCharacteristics";
        String PROMOTION_ITEMS_MATERIAL_CHAR_VALUE = "MaterialCharValue";
        String PROMOTION_ITEMS_MIN_VAR_PROD = "MinVarProd";
        String PROMOTION_ITEMS_MIN_TOTAL_QTY = "MinTotQty";
        String PROMOTION_ITEMS_MANDATORY = "Mandatory";
        String PROMOTION_ITEMS_UOM_FOR_MIN_QTY = "UOMForMinQty";
        String PROMOTION_ITEMS_MIN_TOTAL_VAL = "MinTotVal";
        String PROMOTION_ITEMS_CUR_FOR_MIN_VAL = "CurForMinVal";
        String PROMOTION_ITEMS_AGENT_ID = "Agentid";
    }

    interface PromotionStepsColumn {
        String PROMOTION_STEPS_ESP_NUMBER = "ESPNumber";
        String PROMOTION_STEPS_RECORD_NUMBER = "RecordNumber";
        String PROMOTION_STEPS_STEP_ID = "Step";
        String PROMOTION_STEPS_QTY_BASED_STEP = "QtyBasedStep";
        String PROMOTION_STEPS_VAL_BASED_STEP = "ValBasedStep";
        String PROMOTION_STEPS_PROMOTION_TYPE = "PromotionType";
        String PROMOTION_STEPS_PROMOTION_DISCOUNT = "PromotionDiscount";
        String PROMOTION_STEPS_PRICE_BASED_QTY = "PPriceBasedQty";
        String PROMOTION_STEPS_PRICE_BASED_QTY_UOM = "PPriceBQtyUOM";
        String PROMOTION_STEPS_PROMOTION_PRICE = "PromotionPrice";
        String PROMOTION_STEPS_PROMOTION_PRICE_CURRENCY = "PromotionPriceCurrency";
        String PROMOTION_STEPS_BONUS_QUANTITY = "BonusQuantity";
        String PROMOTION_STEPS_BONUS_QUANTITY_UOM = "BonusQuantityUOM";
        String PROMOTION_STEPS_BONUS_MULTIPLE_QTY = "BonusMultipleValue";
        String PROMOTION_STEPS_BONUS_MULTIPLE_QTY_UOM = "BonusMultQtyUOM";
        String PROMOTION_STEPS_BONUS_DISCOUNT = "BounsDiscount";
        String PROMOTION_STEPS_BONUS_PRICE = "BonusPrice";
        String PROMOTION_STEPS_STEP_DESCRIPTION = "StepDescription";
    }

    interface PromotionCustomersColumn {
        String PROMOTION_CUSTOMERS_AGENT_ID = "Agentid";
        String PROMOTION_CUSTOMERS_CUST_KEY = "Cust_Key";
        String PROMOTION_CUSTOMERS_ESP_NUMBER = "ESPNumber";
    }

    interface PromotionItemMappingColumn {
        String PROMOTION_ITEM_MAPPING_POPULATION_CODE = "SelectedCharacteristics";
        String PROMOTION_ITEM_MAPPING_POPULATION_ITEMS_FILED_CODE = "ItemField";
    }

    public static class PromotionItemsBonus implements PromotionItemsBonusColumn {
    }

    public static class PromotionBonus implements PromotionBonusColumn {
    }

    public static class PromotionHeader implements PromotionHeaderColumn {
    }

    public static class PromotionItems implements PromotionItemsColumn {
    }

    public static class PromotionSteps implements PromotionStepsColumn {
    }

    public static class PromotionCustomers implements PromotionCustomersColumn {
    }

    public static class PromotionItemMapping implements PromotionItemMappingColumn {
    }

    private PromotionsContract() {
    }
}
