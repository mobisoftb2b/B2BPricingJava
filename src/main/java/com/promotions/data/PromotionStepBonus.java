package com.promotions.data;

import com.mobisale.utils.LogUtil;
import com.promotions.manager.PromotionsDataManager;
//import org.apache.tomcat.dbcp.dbcp.DelegatingResultSet;

import java.util.*;

/**
 * Created by israel on 4/22/14.
 */
public class PromotionStepBonus extends APromotionStep {
    private int totalQuantity = 0;
    private String stepsBasedUOM = ItemPromotionData.PC_UNIT;
    public HashMap<String, PromotionBonusItem> PromotionBonusItems = new HashMap<>();
    private PromotionPopulationMapData promotionPopulationMapData = new PromotionPopulationMapData();
    private boolean needToOpenBonusPopup;

    public PromotionStepBonus(String Cust_Key, String espNumber, int recordNumber, int step, int qtyBasedStep, int valBasedStep, int promotionType, double promotionDiscount, double bonusPrice, double bonusDiscount, double priceBasedQty,
                              String priceBQtyUOM, double promotionPrice, String promotionPriceCurrency, int bonusQuantity, String bonusQuantityUOM, int bonusMultipleQty, String bonusMultQtyUOM, StepDescription stepDescription, String stepDescriptionStr) {
        super(Cust_Key, espNumber, recordNumber, step, qtyBasedStep, valBasedStep, promotionType, promotionDiscount, bonusPrice, bonusDiscount, priceBasedQty, priceBQtyUOM, promotionPrice, promotionPriceCurrency, bonusQuantity, bonusQuantityUOM, bonusMultipleQty, bonusMultQtyUOM, stepDescription, stepDescriptionStr);
    }

    public synchronized void setItems(int selectedCharacteristics, String materialCharValue, Set<String> possibleItems, PromotionsDataManager promotionsDataManager) {
        //Marina - possibleItems are from buy items but materialCharItem is bonus item
        ArrayList<String> itemCodes = promotionPopulationMapData.getItems(selectedCharacteristics, materialCharValue, possibleItems, promotionsDataManager);
        for (String itemCode : itemCodes) {
            try {
                ItemPromotionData itemPromotionData = promotionsDataManager.getOrderUIItem(itemCode);
                //Marina here we check if bonus item is in buy items
                //I say let's return it anyway
                //uncomment following 2 lines if we want to check
                //if (itemPromotionData == null)
                //    continue;
                PromotionBonusItem promotionItem;
                if (itemPromotionData == null)
                    promotionItem = new PromotionBonusItem(itemCode, BonusPrice, BonusDiscount, -1, BonusQuantityUOM);
                else
                    promotionItem = new PromotionBonusItem(itemCode, BonusPrice, BonusDiscount, Math.round(itemPromotionData.getUnitInKar()), BonusQuantityUOM);
//                OrderDataManager.getInstance().getOrderUIItem(itemCode).setItemParticipateInDeal(true);
                PromotionBonusItems.put(itemCode, promotionItem);
            } catch (Exception e) {
                // TODO: 2019-07-31 add log
            }
        }
    }

    public synchronized void excludeIfNeeded(ArrayList<String> excludeItemCodes) {
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

    public synchronized void updateTotalQuantity(int totalQty, String stepsBasedUOM) {
        totalQuantity = totalQty;
        this.stepsBasedUOM = stepsBasedUOM;
    }


    public synchronized void updateBonusDiscount(HashMap<String, ItemPromotionData> itemsDataMap, PromotionsDataManager promotionsDataManager) {
        if (PromotionBonusItems.size() == 0) return;

        int total = getTotalCalculatedBonusQuantity();

     //   if (PromotionBonusItems.size() == 1) {
            for(Map.Entry<String, PromotionBonusItem> bonusItem : PromotionBonusItems.entrySet()){
                PromotionBonusItem promotionBonusItem = bonusItem.getValue();
                promotionBonusItem.updateQuantity(total);
                updateCurrentTotalBonusQuantityForUIItem(promotionBonusItem, promotionsDataManager);
            }

      //  } /*else {
            // check is need popup
         //  if (total != getCurrentTotalBonusQuantity()) {
         //       needToOpenBonusPopup = true;
         //   }
        //}


    }

    public synchronized void resetPromotionDiscount(PromotionsDataManager promotionsDataManager) {
        if (PromotionBonusItems.size() == 0) return;
        needToOpenBonusPopup = false;
        for (Map.Entry<String, PromotionBonusItem> stringPromotionBonusItemEntry : PromotionBonusItems.entrySet()) {
            PromotionBonusItem promotionBonusItem = stringPromotionBonusItemEntry.getValue();
            promotionBonusItem.resetQuantity();
            ItemPromotionData itemPromotionData = promotionsDataManager.getOrderUIItem(promotionBonusItem.ItemCode);
            if (itemPromotionData != null) {
                promotionsDataManager.resetBonusData(ESPNumber);
            }
        }
    }

    public synchronized void updateCurrentTotalBonusQuantityForUIItem(PromotionBonusItem promotionBonusItem, PromotionsDataManager promotionsDataManager) {
        PromotionHeader promotionHeader = PromotionsDataManager.getPromotionHeaderByESPNumber(ESPNumber);
        String description = "";
        if (promotionHeader != null) {
            description = promotionHeader.ESPDescription;
        }
        ItemPromotionData itemPromotionData = promotionsDataManager.getOrderUIItem(promotionBonusItem.ItemCode);

        long updateTime;
        if (PromotionBonusItems.size() == 1) {
            updateTime = System.currentTimeMillis();
        } else {
            // todo get last buy item time
            updateTime = PromotionsDataManager.getPromotionUpdateTime(ESPNumber, promotionBonusItem.ItemCode);
        }
        int total = promotionBonusItem.getQuantity();
        double discount = -1;
        if (itemPromotionData != null) {
            double price = promotionBonusItem.getPromotionNetoPrice();
            discount = promotionBonusItem.getPromotionNetoDiscount();

            if (price > 0) {
                int unitsQuantity = Math.round(itemPromotionData.getUnitInKar());
                if (!promotionBonusItem.BaseUnit.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                    price = price / unitsQuantity;
                }
                double basePrice = itemPromotionData.getItemPricingData().getTotalStartValue();
                discount = (1 - (price / basePrice)) * 100;
            }
            /*
            if (total == 0) {
                promotionsDataManagerInstance.resetBonusData(ESPNumber);
            } else {
                if (!BonusQuantityUOM.isEmpty() && BonusQuantityUOM.equalsIgnoreCase("KAR")) {
                    promotionsDataManagerInstance.updateBonusData(itemPromotionData.getItemCode(), ESPNumber, description, 0, total, BonusQuantityUOM, discount, updateTime);
                } else {
                    promotionsDataManagerInstance.updateBonusData(itemPromotionData.getItemCode(), ESPNumber, description, total, 0, BonusQuantityUOM, discount, updateTime);
                }
            }
             */
        }
        if (total == 0) {
            promotionsDataManager.resetBonusData(ESPNumber);
        } else {
            if (!BonusQuantityUOM.isEmpty() && BonusQuantityUOM.equalsIgnoreCase("KAR")) {
                promotionsDataManager.updateBonusData(promotionBonusItem.ItemCode, ESPNumber, description, 0, total, BonusQuantityUOM, discount, updateTime);
            } else {
                promotionsDataManager.updateBonusData(promotionBonusItem.ItemCode, ESPNumber, description, total, 0, BonusQuantityUOM, discount, updateTime);
            }
        }
    }

    private synchronized int getCurrentTotalBonusQuantity() {
        int total = 0;
        for (Map.Entry<String, PromotionBonusItem> stringPromotionBonusItemEntry : PromotionBonusItems.entrySet()) {
            PromotionBonusItem promotionBonusItem = stringPromotionBonusItemEntry.getValue();
            total += promotionBonusItem.getQuantity();
        }
        return total;
    }

    public synchronized int getTotalCalculatedBonusQuantity() {
        if (BonusMultipleQty == 0) {
            LogUtil.LOG.error("Trying to divide by BonusMultipleQuantity=0");
            return  0;
        }
        else {
                return (((totalQuantity - QtyBasedStep) / BonusMultipleQty) + 1) * BonusQuantity;
        }
    }

    public synchronized boolean isNeedToOpenBonusPopup() {
        return needToOpenBonusPopup;
    }

    public void setNeedToOpenBonusPopup(boolean needToOpenBonusPopup) {
        this.needToOpenBonusPopup = needToOpenBonusPopup;
    }

    public synchronized boolean isTotalBonusAmountChanged() {
        return getTotalCalculatedBonusQuantity() != getCurrentTotalBonusQuantity();
    }

    //public String getStepDescription(int definitionMethod, String stepsBasedUOM) {
    //String remoteDescription = super.getStepDescription(definitionMethod, stepsBasedUOM);
    //    if (remoteDescription != null) return remoteDescription;

    //    return "";
    //}
    @Override
    public  synchronized StepDescription getStepDescription(int definitionMethod, String stepsBasedUOM)
    {
        StepDescription stepDesc = new StepDescription();
        StepDescription remoteDescription = super.getStepDescription1(definitionMethod, stepsBasedUOM);
        if (remoteDescription != null) {
            return remoteDescription;
        }
        if (this.StepDescriptionStr == null || this.StepDescriptionStr == "")
            stepDesc.Description =  getStepDescriptionDesc(definitionMethod, stepsBasedUOM);
        else
            stepDesc.Description = StepDescriptionStr;
        stepDesc.IsBonus = true;
        if (stepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT))
            stepDesc.BuyBoxOrUnit = ItemPromotionData.PC_UNIT;
        else
            stepDesc.BuyBoxOrUnit = ItemPromotionData.KARTON_UNIT;
        if (PriceBQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT))
            stepDesc.GetBoxOrUnit = ItemPromotionData.PC_UNIT;
        else
            stepDesc.GetBoxOrUnit = ItemPromotionData.KARTON_UNIT;
        if (BonusQuantityUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT))
            stepDesc.BonusBoxOrUnit = ItemPromotionData.PC_UNIT;
        else
            stepDesc.BonusBoxOrUnit = ItemPromotionData.KARTON_UNIT;
        if (BonusMultQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT))
            stepDesc.BonusMultiBoxOrUnit = ItemPromotionData.PC_UNIT;
        else
            stepDesc.BonusMultiBoxOrUnit = ItemPromotionData.KARTON_UNIT;
        stepDesc.EspNumber = this.ESPNumber;
        stepDesc.MinQtyForBonus = this.Step;
        stepDesc.PromotionDiscount = this.PromotionDiscount;
        stepDesc.BonusDiscount = this.BonusDiscount;
        stepDesc.QtyForBonus = this.BonusMultipleQty;
        stepDesc.BonusQuantity = this.BonusQuantity;
        stepDesc.BonusItemCodes = new ArrayList<>();
        for (String key : PromotionBonusItems.keySet())
            stepDesc.BonusItemCodes.add(key);
        return stepDesc;
    }


    public synchronized String getStepDescriptionDesc(int definitionMethod, String stepsBasedUOM) {
        String description = "";
        String buyBoxOrUnit = "";
        String getBoxOrUnit = "";
        String bonusQuantityBoxOrUnit = "";
        String bonusMultiBoxOrUnit = "";
        if (stepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            buyBoxOrUnit = "יח";
        } else {
            buyBoxOrUnit = "קר";
        }
        if (PriceBQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            getBoxOrUnit = "יח";
        } else {
            getBoxOrUnit = "קר";
        }
        if (BonusQuantityUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            bonusQuantityBoxOrUnit = "יח";
        } else {
            bonusQuantityBoxOrUnit = "קר";
        }
        if (BonusMultQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
            bonusMultiBoxOrUnit = "יח";
        } else {
            bonusMultiBoxOrUnit = "קר";
        }

        if (PromotionType == 4) {
            description = //String.format("%s %d %s%s %s %d %s %s %d %s %s %s",
                    "קנה מ" +  Step +  " " + buyBoxOrUnit + " " + "עד" + " " +  BonusMultipleQty + " " + buyBoxOrUnit + " " + "קבל" + " " + BonusQuantity +  " " + bonusQuantityBoxOrUnit + " " + "של" + " " +
                    "בונוס";//);
            if (PromotionDiscount > 0) {
                description = String.format("%s %s %s%s %s", description,
                        "וגם", dfPercent.format(PromotionDiscount), "%", "הנחה");
            }
        } else if (PromotionType == 5) {
            description = String.format("%s %d %s%s %s %d %s %s %d %s %s %s %s %s %s %s%s %s %s",
                    "קנה לפחות", Step, buyBoxOrUnit, ",", "על כל",
                    BonusMultipleQty, bonusMultiBoxOrUnit, "קבל", BonusQuantity, bonusQuantityBoxOrUnit, "של",
                    ""/*BonusProductName*/, "בונוס", "וגם", "מחיר נטו של", "₪ ", dfPrice.format(PromotionPrice), "ל", getBoxOrUnit);
        }

        return description;
    }

    @Override
    public synchronized boolean isPromotionStepBonus() {
        return true;
    }

    public synchronized boolean inventoryValidation() {
       return true;
    }
}
