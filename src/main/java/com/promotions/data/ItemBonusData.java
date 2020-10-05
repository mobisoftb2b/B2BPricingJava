package com.promotions.data;


import java.util.ArrayList;

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
    public int ItemBonusPCQuantity;
    public int ItemBonusKARQuantity;
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
