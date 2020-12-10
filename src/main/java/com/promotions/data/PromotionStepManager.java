package com.promotions.data;

import com.promotions.manager.PromotionsDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by israel on 4/22/14.
 */
public class PromotionStepManager {

    public ArrayList<StepRecordNumber> stepRecordNumbers = new ArrayList<>();
    public boolean IsOpen;
    public int PCTotalQty;
    public int KARTotalQty;
    public double totalVal;

    public PromotionStepManager(ArrayList<StepRecordNumber> stepRecordNumbers) {
        this.stepRecordNumbers = stepRecordNumbers;
    }


    public synchronized boolean isPromotionHeaderActiveAndOpen(String itemCode, float newQuantity, int definitionMethod, String StepsBasedUOM, PromotionsDataManager promotionsDataManager) {
        StepRecordNumber ActiveStepRecordNumber = null;
        IsOpen = true;
        PCTotalQty = 0;
        KARTotalQty = 0;
        totalVal = 0;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.updateQuantity(itemCode, newQuantity, definitionMethod, StepsBasedUOM, promotionsDataManager);
            PCTotalQty += stepRecordNumber.PCTotalQty;
            KARTotalQty += stepRecordNumber.KARTotalQty;
            totalVal += stepRecordNumber.totalVal;
            if (isItemExist && ActiveStepRecordNumber == null) {
                ActiveStepRecordNumber = stepRecordNumber;
            }
            IsOpen = IsOpen && stepRecordNumber.isOpen;
        }
        return IsOpen && ActiveStepRecordNumber != null;
    }

    public synchronized boolean isItemExist(String itemCode) {
        boolean isItemExist = false;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                break;
            }
        }
        return isItemExist;
    }

    public synchronized void updatePromotionDiscount(String itemCode, float newQuantity, int definitionMethod, String stepsBasedUOM, boolean excludeDiscount, boolean isBlocked, PromotionsDataManager promotionsDataManager) {
        if (isPromotionHeaderActiveAndOpen(itemCode, newQuantity, definitionMethod, stepsBasedUOM, promotionsDataManager) && !isBlocked) {
            for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
                stepRecordNumber.updatePromotionDiscount(PCTotalQty, KARTotalQty, totalVal, definitionMethod, stepsBasedUOM, excludeDiscount, promotionsDataManager);
            }
        } else {
            for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
                stepRecordNumber.resetPromotionDiscount(promotionsDataManager);
            }
        }
    }

    public synchronized ArrayList<ItemPromotionData> updateItemsPriceAndDiscount(HashMap<String, ItemPromotionData> itemsDataMap, PromotionsDataManager promotionsDataManager) {
        ArrayList<ItemPromotionData> itemsData = new ArrayList<ItemPromotionData>();
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            itemsData.addAll(stepRecordNumber.updateItemsPriceAndDiscount(itemsDataMap, promotionsDataManager));
        }
        return itemsData;
    }

    public synchronized ArrayList<StepDescription> getStepDescription(String itemCode) {
        ArrayList<StepDescription> stepDescription = new ArrayList<StepDescription>();
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                stepDescription = stepRecordNumber.getStepDescription();
                break;
            }
        }
        return stepDescription;
    }

    public synchronized StepDescription getSelectedStepDetailDescription(String itemCode) {
        StepDescription description = null;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                description = stepRecordNumber.getSelectedStepDetailDescription();
                break;
            }
        }
        return description;
    }

    public synchronized StepRecordNumber getActiveStepRecordNumber() {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            APromotionStep promotionStep = stepRecordNumber.getActivePromotionStep();
            if (promotionStep != null) return stepRecordNumber;
        }
        return null;
    }

    public synchronized APromotionStep getActivePromotionStep() {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            APromotionStep promotionStep = stepRecordNumber.getActivePromotionStep();
            if (promotionStep != null) return promotionStep;
        }
        return null;
    }

    public synchronized void blockPromotion(String itemCode, PromotionsDataManager promotionsDataManager) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.blockPromotion(promotionsDataManager);
        }
    }

    public synchronized void blockAllPromotion(PromotionsDataManager promotionsDataManager) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.blockPromotion(promotionsDataManager);
        }
    }

    public synchronized void unBlockPromotion(String itemCode, boolean excludeDiscount, PromotionsDataManager promotionsDataManager) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.unBlockPromotion(PCTotalQty, KARTotalQty, totalVal, excludeDiscount, promotionsDataManager);
        }
    }

    public synchronized void unBlockAllPromotion(boolean excludeDiscount, PromotionsDataManager promotionsDataManager) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.unBlockPromotion(PCTotalQty, KARTotalQty, totalVal, excludeDiscount, promotionsDataManager);
        }
    }

    public synchronized int getNextStepDiff(String itemCode) {
        int nextStepDiff = -1;
        StepRecordNumber selectedStepRecordNumber = null;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                selectedStepRecordNumber = stepRecordNumber;
                break;
            }
        }
        if (selectedStepRecordNumber != null) {
            nextStepDiff = selectedStepRecordNumber.getNextStepDiff(PCTotalQty, KARTotalQty, totalVal);
        }
        return nextStepDiff;
    }

    public synchronized int getMinimumRemainForPopulationItems(String itemCode) {
        int minimumRemainForPopulationItems = -1;
        StepRecordNumber selectedStepRecordNumber = null;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                selectedStepRecordNumber = stepRecordNumber;
                break;
            }
        }
        if (selectedStepRecordNumber != null) {
            minimumRemainForPopulationItems = selectedStepRecordNumber.getMinimumRemainForPopulationItems(itemCode);
        }
        return minimumRemainForPopulationItems;
    }

    public synchronized int getPromotionStatus() {
        int status = -1;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            if (stepRecordNumber.getPromotionStatus() == 1) {
                status = 1;
                break;
            } else if (stepRecordNumber.getPromotionStatus() == 3) {
                status = 3;
            }
        }
        return status;
    }

    public synchronized void updateItemPricing(String itemCode, int uiIndex, double price, double discount, String unitType) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.updateItemPricing(itemCode, uiIndex, price, discount, unitType);
        }
    }

    public synchronized boolean isHaveItems() {
        boolean isHaveItems = false;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            if (stepRecordNumber.isHaveItems()) {
                isHaveItems = true;
                break;
            }
        }
        return isHaveItems;
    }

    public synchronized boolean isNeedToOpenBonusPopup() {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            if (stepRecordNumber.isNeedToOpenBonusPopup()) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isActivePromotionWasReset(String itemCode) {
        boolean isActivePromotionWasReset = false;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                isActivePromotionWasReset = stepRecordNumber.isActivePromotionWasReset();
                break;
            }
        }
        return isActivePromotionWasReset;
    }

    public synchronized void duplicateItem(String itemCode, int quantity) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                stepRecordNumber.duplicateItem(itemCode, quantity);
            }
        }
    }

    public synchronized Set<String> getAllPromotionItemCodes() {
        StepRecordNumber stepRecordNumber = getActiveStepRecordNumber();
        if (stepRecordNumber != null) {
            return stepRecordNumber.getAllPromotionItemCodes();
        }
        return new HashSet<>();
    }

    public synchronized void resetPromotions(PromotionsDataManager promotionsDataManager) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.resetPromotions(promotionsDataManager);
        }
    }

    public synchronized void ClearSteps() {
        stepRecordNumbers.clear();
    }
}
