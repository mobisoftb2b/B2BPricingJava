package com.promotions.data;

import com.promotions.manager.PromotionsDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by israel on 4/22/14.
 */
public class PromotionStepBonus extends APromotionStep {
    private int totalQuantity = 0;
    private String stepsBasedUOM = ItemPromotionData.PC_UNIT;
    public HashMap<String, PromotionBonusItem> PromotionBonusItems = new HashMap<>();
    private boolean needToOpenBonusPopup;

    public PromotionStepBonus(String Cust_Key, String espNumber, int recordNumber, int step, int qtyBasedStep, int valBasedStep, int promotionType, double promotionDiscount, double bonusPrice, double bonusDiscount, double priceBasedQty,
                              String priceBQtyUOM, double promotionPrice, String promotionPriceCurrency, int bonusQuantity, String bonusQuantityUOM, int bonusMultipleQty, String bonusMultQtyUOM, String stepDescription) {
        super(Cust_Key, espNumber, recordNumber, step, qtyBasedStep, valBasedStep, promotionType, promotionDiscount, bonusPrice, bonusDiscount, priceBasedQty, priceBQtyUOM, promotionPrice, promotionPriceCurrency, bonusQuantity, bonusQuantityUOM, bonusMultipleQty, bonusMultQtyUOM, stepDescription);
    }

    public void setItems(int selectedCharacteristics, String materialCharValue, Set<String> possibleItems) {
        ArrayList<String> itemCodes = PromotionPopulationMapData.getInstance().getItems(selectedCharacteristics, materialCharValue, possibleItems);
        for (String itemCode : itemCodes) {
            try {
                ItemPromotionData itemPromotionData = PromotionsDataManager.getInstance(m_CustKey).getOrderUIItem(itemCode);
                if (itemPromotionData == null) continue;
                PromotionBonusItem promotionItem = new PromotionBonusItem(itemCode, BonusPrice, BonusDiscount, Math.round(itemPromotionData.getUnitInKar()), BonusQuantityUOM);
//                OrderDataManager.getInstance().getOrderUIItem(itemCode).setItemParticipateInDeal(true);
                PromotionBonusItems.put(itemCode, promotionItem);
            } catch (Exception e) {
                // TODO: 2019-07-31 add log
            }
        }
    }

    public void excludeIfNeeded(ArrayList<String> excludeItemCodes) {
        for (String excludeItemCode : excludeItemCodes) {
            boolean isRemoved = PromotionBonusItems.remove(excludeItemCode) != null;
            if (isRemoved) {
                try {
//                    OrderDataManager.getInstance().getOrderUIItem(excludeItemCode).setItemParticipateInDeal(false);
                } catch (Exception e) {
                    // TODO: 2019-07-31 add log
                }
            }
        }
    }

    public void updateTotalQuantity(int totalQty, String stepsBasedUOM) {
        totalQuantity = totalQty;
        this.stepsBasedUOM = stepsBasedUOM;
    }


    public void updateBonusDiscount(HashMap<String, ItemPromotionData> itemsDataMap) {
        if (PromotionBonusItems.size() == 0) return;

        int total = getTotalCalculatedBonusQuantity();

        if (PromotionBonusItems.size() == 1) {
            PromotionBonusItem promotionBonusItem = PromotionBonusItems.entrySet().iterator().next().getValue();
            promotionBonusItem.updateQuantity(total);
            updateCurrentTotalBonusQuantityForUIItem(promotionBonusItem);
        } else {
            // check is need popup
            if (total != getCurrentTotalBonusQuantity()) {
                needToOpenBonusPopup = true;
            }
        }
    }

    public void resetPromotionDiscount() {
        PromotionsDataManager promotionsDataManagerInstance = PromotionsDataManager.getInstance(m_CustKey);
        if (PromotionBonusItems.size() == 0) return;
        needToOpenBonusPopup = false;
        for (Map.Entry<String, PromotionBonusItem> stringPromotionBonusItemEntry : PromotionBonusItems.entrySet()) {
            PromotionBonusItem promotionBonusItem = stringPromotionBonusItemEntry.getValue();
            promotionBonusItem.resetQuantity();
            ItemPromotionData itemPromotionData = promotionsDataManagerInstance.getOrderUIItem(promotionBonusItem.ItemCode);
            if (itemPromotionData != null) {
                promotionsDataManagerInstance.resetBonusData(ESPNumber);
            }
        }
    }

    public void updateCurrentTotalBonusQuantityForUIItem(PromotionBonusItem promotionBonusItem) {
        PromotionHeader promotionHeader = PromotionsDataManager.getPromotionHeaderByESPNumber(ESPNumber);
        String description = "";
        if (promotionHeader != null) {
            description = promotionHeader.ESPDescription;
        }
        PromotionsDataManager promotionsDataManagerInstance = PromotionsDataManager.getInstance(m_CustKey);
        ItemPromotionData itemPromotionData = promotionsDataManagerInstance.getOrderUIItem(promotionBonusItem.ItemCode);
        if (itemPromotionData != null) {
            double price = promotionBonusItem.getPromotionNetoPrice();
            double discount = promotionBonusItem.getPromotionNetoDiscount();
            int total = promotionBonusItem.getQuantity();

            if (price > 0) {
                int unitsQuantity = Math.round(itemPromotionData.getUnitInKar());
                if (!promotionBonusItem.BaseUnit.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                    price = price / unitsQuantity;
                }
                double basePrice = itemPromotionData.getItemPricingData().getTotalStartValue();
                discount = (1 - (price / basePrice)) * 100;
            }

            long updateTime;
            if (PromotionBonusItems.size() == 1) {
                updateTime = System.currentTimeMillis();
            } else {
                // todo get last buy item time
                updateTime = PromotionsDataManager.getPromotionUpdateTime(ESPNumber, promotionBonusItem.ItemCode);
            }
            if (total == 0) {
                promotionsDataManagerInstance.resetBonusData(ESPNumber);
            } else {
                if (!BonusQuantityUOM.isEmpty() && BonusQuantityUOM.equalsIgnoreCase(ItemPromotionData.KARTON_UNIT)) {
                    promotionsDataManagerInstance.updateBonusData(itemPromotionData.getItemCode(), ESPNumber, description, 0, total, BonusQuantityUOM, discount, updateTime);
                } else {
                    promotionsDataManagerInstance.updateBonusData(itemPromotionData.getItemCode(), ESPNumber, description, total, 0, BonusQuantityUOM, discount, updateTime);
                }
            }
        }
    }

    private int getCurrentTotalBonusQuantity() {
        int total = 0;
        for (Map.Entry<String, PromotionBonusItem> stringPromotionBonusItemEntry : PromotionBonusItems.entrySet()) {
            PromotionBonusItem promotionBonusItem = stringPromotionBonusItemEntry.getValue();
            total += promotionBonusItem.getQuantity();
        }
        return total;
    }

    public int getTotalCalculatedBonusQuantity() {
        return (((totalQuantity - QtyBasedStep) / BonusMultipleQty) + 1) * BonusQuantity;
    }

    public boolean isNeedToOpenBonusPopup() {
        return needToOpenBonusPopup;
    }

    public void setNeedToOpenBonusPopup(boolean needToOpenBonusPopup) {
        this.needToOpenBonusPopup = needToOpenBonusPopup;
    }

    public boolean isTotalBonusAmountChanged() {
        return getTotalCalculatedBonusQuantity() != getCurrentTotalBonusQuantity();
    }

    //public String getStepDescription(int definitionMethod, String stepsBasedUOM) {
    //String remoteDescription = super.getStepDescription(definitionMethod, stepsBasedUOM);
    //    if (remoteDescription != null) return remoteDescription;

    //    return "";
    //}
    @Override
    public String getStepDescription(int definitionMethod, String stepsBasedUOM) {
        String remoteDescription = super.getStepDescription1(definitionMethod, stepsBasedUOM);
        if (remoteDescription != null && !remoteDescription.isEmpty()) return remoteDescription;
        String description = "";
        String buyBoxOrUnit = "";
        String getBoxOrUnit = "";
        String bonusQuantityBoxOrUnit = "";
        String bonusMultiBoxOrUnit = "";
        if (stepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            buyBoxOrUnit = "יח\\'";
        } else {
            buyBoxOrUnit = "קר\\'";
        }
        if (PriceBQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            getBoxOrUnit = "יח\\'";
        } else {
            getBoxOrUnit = "קר\\'";
        }
        if (BonusQuantityUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            bonusQuantityBoxOrUnit = "יח\\'";
        } else {
            bonusQuantityBoxOrUnit = "קר\\'";
        }
        if (BonusMultQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            bonusMultiBoxOrUnit = "יח\\'";
        } else {
            bonusMultiBoxOrUnit = "קר\\'";
        }

        if (PromotionType == 4) {
            description = String.format("%s %d %s%s %s %d %s %s %d %s %s %s %s %s %s%s %s",
                    "קנה לפחות", Step, buyBoxOrUnit, ",", "על כל",
                    BonusMultipleQty, bonusMultiBoxOrUnit, "קבל", BonusQuantity, bonusQuantityBoxOrUnit, "של",
                    ""/*BonusProductName*/, "בונוס", "וגם", dfPercent.format(PromotionDiscount), "%", "הנחה");
        } else if (PromotionType == 5) {
            description = String.format("%s %d %s%s %s %d %s %s %d %s %s %s %s %s %s %s%s %s %s",
                    "קנה לפחות", Step, buyBoxOrUnit, ",", "על כל",
                    BonusMultipleQty, bonusMultiBoxOrUnit, "קבל", BonusQuantity, bonusQuantityBoxOrUnit, "של",
                    ""/*BonusProductName*/, "בונוס", "וגם", "מחיר נטו של", "₪ ", dfPrice.format(PromotionPrice), "ל", getBoxOrUnit);
        }

        return description;
    }

    @Override
    public boolean isPromotionStepBonus() {
        return true;
    }

    public boolean inventoryValidation() {
       return true;
    }
}
