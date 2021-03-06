package com.promotions.data;


import com.promotions.manager.PromotionsDataManager;

import java.util.*;

/**
 * Created by israel on 4/22/14.
 */
public class StepRecordNumber {
    public final String ESPNumber;
    public final int RecordNumber;
    public final int DefinitionMethod;
    public final String StepsBasedUOM;
    public boolean isOpen;
    public int PCTotalQty;
    public int KARTotalQty;
    public double totalVal;
    public TreeMap<Integer, APromotionStep> promotionStepTreeMap = new TreeMap<>();
    private ArrayList<PromotionPopulationItem> promotionPopulationItems = new ArrayList<PromotionPopulationItem>();
    private HashMap<String, ArrayList<PromotionItem>> allPromotionItems = new HashMap<>();
    private APromotionStep activePromotionStep;
    private boolean isActivePromotionWasReset;
    private String m_CustKey;

    public StepRecordNumber(String cust_Key, String espNumber, int recordNumber, int definitionMethod, String stepsBasedUOM) {
        ESPNumber = espNumber;
        RecordNumber = recordNumber;
        DefinitionMethod = definitionMethod;
        StepsBasedUOM = stepsBasedUOM;
        m_CustKey = cust_Key;
    }

    public synchronized boolean updateQuantity(String itemCode, float newQuantity, int definitionMethod, String StepsBasedUOM, PromotionsDataManager promotionsDataManager) {
        PCTotalQty = 0;
        KARTotalQty = 0;
        totalVal = 0;
        isOpen = true;
        boolean isItemExist = false;
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            isItemExist = isItemExist || promotionPopulationItem.updateQuantity(itemCode, newQuantity, promotionsDataManager);
            PCTotalQty += promotionPopulationItem.totalPCQty;
            KARTotalQty += promotionPopulationItem.totalKarQty;
            totalVal += promotionPopulationItem.totalVal;
            isOpen = isOpen && promotionPopulationItem.isValid(definitionMethod, StepsBasedUOM);
        }
        return isItemExist;
    }

    public synchronized void updatePromotionDiscount(int PCTotalQty, int KARTotalQty, double totalVal, int definitionMethod, String stepsBasedUOM, boolean excludeDiscount, PromotionsDataManager promotionsDataManager) {
        APromotionStep currentActivePromotionStep = null;
        NavigableMap<Integer, APromotionStep> descendingMap = promotionStepTreeMap.descendingMap();
        Set<Map.Entry<Integer, APromotionStep>> entrySet = descendingMap.entrySet();
        for (Map.Entry<Integer, APromotionStep> next : entrySet) {
            if (definitionMethod == 1) {
                if (stepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                    if (PCTotalQty >= next.getKey()) {
                        currentActivePromotionStep = next.getValue();
                        break;
                    }
                } else {
                    if (KARTotalQty >= next.getKey()) {
                        currentActivePromotionStep = next.getValue();
                        break;
                    }
                }
            } else {
                if (totalVal >= next.getKey()) {
                    currentActivePromotionStep = next.getValue();
                    break;
                }
            }
        }

        // TODO: 14/02/2018  be aware this change may cause errors to discounts promotions look at git history to rollback
        if (currentActivePromotionStep != null) {
            if (currentActivePromotionStep.isPromotionStepBonus()) {

                boolean isAlreadyReset = false;
                if (!currentActivePromotionStep.equals(activePromotionStep)) {
                    resetPromotionDiscount(promotionsDataManager);
                    isAlreadyReset = true;
                }

                if (stepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                    ((PromotionStepBonus) currentActivePromotionStep).updateTotalQuantity(PCTotalQty, stepsBasedUOM);
                } else {
                    ((PromotionStepBonus) currentActivePromotionStep).updateTotalQuantity(KARTotalQty, stepsBasedUOM);
                }

                if (!isAlreadyReset && ((PromotionStepBonus) currentActivePromotionStep).isTotalBonusAmountChanged()) {
                    resetPromotionDiscount(promotionsDataManager);
                }
            }
            activePromotionStep = currentActivePromotionStep;
            for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
                promotionPopulationItem.updatePriceAndDiscount(activePromotionStep, excludeDiscount, promotionsDataManager);
            }
        }
        else {
            resetPromotionDiscount(promotionsDataManager);
        }
    }

    public synchronized void resetPromotionDiscount(PromotionsDataManager promotionsDataManager) {
        isActivePromotionWasReset = false;
        if (activePromotionStep != null && activePromotionStep.isPromotionStepBonus()) {
            ((PromotionStepBonus) activePromotionStep).resetPromotionDiscount(promotionsDataManager);
            isActivePromotionWasReset = true/*((PromotionStepBonus) activePromotionStep).PromotionBonusItems.size() == 1*/;
        }
        activePromotionStep = null;
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            promotionPopulationItem.resetPromotionDiscount(promotionsDataManager);
        }
    }

    public synchronized void resetPromotions(PromotionsDataManager promotionsDataManager) {
        isActivePromotionWasReset = false;
        if (activePromotionStep != null && activePromotionStep.isPromotionStepBonus()) {
            ((PromotionStepBonus) activePromotionStep).resetPromotionDiscount(promotionsDataManager);
            isActivePromotionWasReset = true/*((PromotionStepBonus) activePromotionStep).PromotionBonusItems.size() == 1*/;
        }
        activePromotionStep = null;
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            promotionPopulationItem.resetPromotions(promotionsDataManager);
        }
    }

    public synchronized boolean isItemExist(String itemCode) {
//        boolean isItemExist = false;
        return allPromotionItems.get(itemCode) != null;
//        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
//            isItemExist = promotionPopulationItem.isItemExist(itemCode);
//            if (isItemExist) {
//                break;
//            }
//        }
//        return isItemExist;
    }

    public synchronized void addPromotionPopulationItem(PromotionPopulationItem promotionPopulationItem) {
        promotionPopulationItems.add(promotionPopulationItem);
        for (String itemCode : promotionPopulationItem.PromotionItemHeaderMap.keySet()) {
            if (allPromotionItems.get(itemCode) == null) {
                allPromotionItems.put(itemCode, promotionPopulationItem.PromotionItemHeaderMap.getAllItem(itemCode));
            }
        }

    }

    public synchronized void addAllPromotionPopulationItem(ArrayList<PromotionPopulationItem> promotionPopulationItems) {
        this.promotionPopulationItems.addAll(promotionPopulationItems);
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            for (String itemCode : promotionPopulationItem.PromotionItemHeaderMap.keySet()) {
                if (allPromotionItems.get(itemCode) == null) {
                    allPromotionItems.put(itemCode, promotionPopulationItem.PromotionItemHeaderMap.getAllItem(itemCode));
                }
            }
        }
    }

    public synchronized ArrayList<ItemPromotionData> updateItemsPriceAndDiscount(HashMap<String, ItemPromotionData> itemsDataMap, PromotionsDataManager promotionsDataManager) {
        ArrayList<ItemPromotionData> itemPromotionData = new ArrayList<ItemPromotionData>();
        boolean isActivePromotionStep = activePromotionStep != null;
        boolean IsBonus = isActivePromotionStep && activePromotionStep.isPromotionStepBonus();
        if (IsBonus) {
            ((PromotionStepBonus) activePromotionStep).updateBonusDiscount(itemsDataMap, promotionsDataManager);
            if (!((PromotionStepBonus) activePromotionStep).inventoryValidation()) {
                resetPromotionDiscount(promotionsDataManager);
                return itemPromotionData;
            }
        }
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            itemPromotionData.addAll(promotionPopulationItem.updateItemsPriceAndDiscount(m_CustKey, itemsDataMap, isActivePromotionStep, IsBonus, promotionsDataManager));
        }
        return itemPromotionData;
    }

    public synchronized ArrayList<StepDescription> getStepDescription() {
        ArrayList<StepDescription> stepsDescription = new ArrayList<StepDescription>();
        for (APromotionStep promotionStep : promotionStepTreeMap.values()) {
            stepsDescription.add(promotionStep.getStepDescription(DefinitionMethod, StepsBasedUOM));
        }
        return stepsDescription;
    }

    public synchronized ArrayList<PromotionItem> getAllPromotionItems() {
        ArrayList<PromotionItem> allPromotionItems = new ArrayList<PromotionItem>();
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            allPromotionItems.addAll(promotionPopulationItem.PromotionItemHeaderMap.getAllItem());
        }
        return allPromotionItems;
    }

    public synchronized Set<String> getAllPromotionItemCodes() {
        return allPromotionItems.keySet();
    }

    public synchronized StepDescription getSelectedStepDetailDescription() {
        StepDescription description = null;
        if (activePromotionStep != null) {
            description = activePromotionStep.getStepDescription(DefinitionMethod, StepsBasedUOM);
        }
        return description;
    }

    public synchronized APromotionStep getActivePromotionStep() {
        return activePromotionStep;
    }

    public synchronized void blockPromotion(PromotionsDataManager promotionsDataManager) {
        resetPromotionDiscount(promotionsDataManager);
    }

    public synchronized void unBlockPromotion(int PCTotalQty, int KARTotalQty, double totalVal, boolean excludeDiscount, PromotionsDataManager promotionsDataManager) {
        updatePromotionDiscount(PCTotalQty, KARTotalQty, totalVal, DefinitionMethod, StepsBasedUOM, excludeDiscount, promotionsDataManager);
    }

    public synchronized int getNextStepDiff(int PCTotalQty, int KARTotalQty, double totalVal) {
        int nextStepDiff = -1;
        if (PCTotalQty == 0 && KARTotalQty == 0) {
            return nextStepDiff;
        }
        for (APromotionStep promotionStep : promotionStepTreeMap.values()) {
            if (DefinitionMethod == 1) {
                if (StepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                    if (promotionStep.QtyBasedStep > PCTotalQty) {
                        nextStepDiff = promotionStep.QtyBasedStep - PCTotalQty;
                        break;
                    }
                } else {
                    if (promotionStep.QtyBasedStep > KARTotalQty) {
                        nextStepDiff = promotionStep.QtyBasedStep - KARTotalQty;
                        break;
                    }
                }
            } else {
                if (promotionStep.ValBasedStep > totalVal) {
                    nextStepDiff = (int) (promotionStep.ValBasedStep - totalVal);
                    break;
                }
            }
        }
        if (nextStepDiff == -1) {
            NavigableMap<Integer, APromotionStep> descendingMap = promotionStepTreeMap.descendingMap();
            Map.Entry<Integer, APromotionStep> firstEntry = descendingMap.firstEntry();
            APromotionStep promotionStep = firstEntry.getValue();
            if (promotionStep.BonusMultipleQty > 0) {
                if (DefinitionMethod == 1) {
                    if (StepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                        PCTotalQty = PCTotalQty % promotionStep.BonusMultipleQty;
                        if (PCTotalQty == 0) return nextStepDiff;
                        nextStepDiff = promotionStep.BonusMultipleQty - PCTotalQty;
                    } else {
                        KARTotalQty = KARTotalQty % promotionStep.BonusMultipleQty;
                        if (KARTotalQty == 0) return nextStepDiff;
                        nextStepDiff = promotionStep.BonusMultipleQty - KARTotalQty;
                    }
                }
            } else {
                if (DefinitionMethod == 1) {
                    if (StepsBasedUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                        PCTotalQty = PCTotalQty % promotionStep.QtyBasedStep;
                        if (promotionStep.QtyBasedStep > PCTotalQty) {
                            if (PCTotalQty == 0) return nextStepDiff;
                            nextStepDiff = promotionStep.QtyBasedStep - PCTotalQty;
                        }
                    } else {
                        if (promotionStep.QtyBasedStep > KARTotalQty) {
                            KARTotalQty = KARTotalQty % promotionStep.QtyBasedStep;
                            if (KARTotalQty == 0) return nextStepDiff;
                            nextStepDiff = promotionStep.QtyBasedStep - KARTotalQty;
                        }
                    }
                } else {
                    if (promotionStep.ValBasedStep > totalVal) {
                        totalVal = totalVal % promotionStep.ValBasedStep;
                        if (totalVal == 0) return nextStepDiff;
                        nextStepDiff = (int) (promotionStep.ValBasedStep - totalVal);
                    }
                }
            }
        }
        return nextStepDiff;
    }

    public synchronized int getMinimumRemainForPopulationItems(String itemCode) {
        int minimumRemainForPopulationItems = -1;
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            boolean isItemExist = promotionPopulationItem.isItemExist(itemCode);
            if (isItemExist) {
                if (DefinitionMethod == 1) {
                    if (promotionPopulationItem.UOMForMinQty.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                        if (promotionPopulationItem.totalPCQty < promotionPopulationItem.MinTotQty)
                            minimumRemainForPopulationItems = promotionPopulationItem.MinTotQty - promotionPopulationItem.totalPCQty;
                    } else {
                        if (promotionPopulationItem.totalKarQty < promotionPopulationItem.MinTotQty)
                            minimumRemainForPopulationItems = promotionPopulationItem.MinTotQty - promotionPopulationItem.totalKarQty;
                    }
                } else {
                    if (promotionPopulationItem.totalVal < promotionPopulationItem.MinTotVal)
                        minimumRemainForPopulationItems = (int) (promotionPopulationItem.MinTotVal - promotionPopulationItem.totalVal);
                }
                break;
            }
        }
        return minimumRemainForPopulationItems;
    }

    public synchronized int getPromotionStatus() {
        if (activePromotionStep == null) {
            return 3;
        }
        return 1;
    }

    public synchronized void updateItemPricing(String itemCode, int uiIndex, double price, double discount, String unitType) {
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            promotionPopulationItem.updateItemPricing(itemCode, uiIndex, price, discount, unitType);
        }
    }

    public synchronized boolean isHaveItems() {
        return getAllPromotionItems().size() > 0;
    }

    public synchronized boolean isNeedToOpenBonusPopup() {
        boolean isActivePromotionStep = activePromotionStep != null;
        boolean IsBonus = isActivePromotionStep && activePromotionStep.isPromotionStepBonus();
        return IsBonus && ((PromotionStepBonus) activePromotionStep).isNeedToOpenBonusPopup();
    }

    public synchronized boolean isActivePromotionWasReset() {
        return isActivePromotionWasReset;
    }

    public synchronized void duplicateItem(String itemCode, int quantity) {
        for (PromotionPopulationItem promotionPopulationItem : promotionPopulationItems) {
            promotionPopulationItem.duplicateItem(itemCode, quantity);
        }
    }

    public APromotionStep getPromotionStep(int position) {
        try {
            return (APromotionStep) promotionStepTreeMap.values().toArray()[position];
        } catch (Exception e) {
            // TODO: 2019-07-31 add log
        }
        return null;
    }
}
