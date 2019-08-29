package com.promotions.database;


public class PromotionsDatabase {
    private static final String TAG = "PromotionsDatabase";

    public interface Tables {
        String TABLE_PROMOTION_ITEMS_BONUSES = "PromotionItemsBonuses";
        String TABLE_PROMOTION_BONUSES = "PromotionBonuses_zsdt_esprpb";
        String TABLE_PROMOTION_HEADER = "PromotionHeader_zsdt_esp";
        String TABLE_PROMOTION_ITEMS = "PromotionItems_zsdt_espmtr";
        String TABLE_PROMOTION_STEPS = "PromotionSteps_zsdt_esprps";
        String TABLE_PROMOTION_CUSTOMERS = "v_CustPromotions";
        String TABLE_PROMOTION_ITEMS_MAPPING = "PromotionItemMapping";
    }

    public PromotionsDatabase() {

    }

}
