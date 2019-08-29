package com.promotions.data;


/**
 *  this object is for an item in each line in the Basket
 * @author yoav
 *
 */
public class ItemBonusData {
    public static final String CONSTANT_ESP_NUMBER_BONUS = "BONUS";

    public final String ItemCode;
    public final String ESPNumber;
    public final String ESPDescription;
    public final int ItemBonusPCQuantity;
    public final int ItemBonusKARQuantity;
    public final String BonusQuantityUOM;
    public final double ItemBonusPercent;
    public long UpdateTime;

    public ItemBonusData(String itemCode, String ESPNumber, String ESPDescription, int itemBonusPCQuantity, int itemBonusKARQuantity, String bonusQuantityUOM, double itemBonusPercent, long updateTime) {
        ItemCode = itemCode;
        this.ESPNumber = ESPNumber;
        this.ESPDescription = ESPDescription;
        ItemBonusPCQuantity = itemBonusPCQuantity;
        ItemBonusKARQuantity = itemBonusKARQuantity;
        BonusQuantityUOM = bonusQuantityUOM;
        ItemBonusPercent = itemBonusPercent;
        UpdateTime = updateTime;
    }
}
