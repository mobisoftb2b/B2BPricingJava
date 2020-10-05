package com.promotions.data;

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


    public boolean isPromotionHeaderActiveAndOpen(String itemCode, float newQuantity, int definitionMethod, String StepsBasedUOM) {
        StepRecordNumber ActiveStepRecordNumber = null;
        IsOpen = true;
        PCTotalQty = 0;
        KARTotalQty = 0;
        totalVal = 0;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.updateQuantity(itemCode, newQuantity, definitionMethod, StepsBasedUOM);
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

    public boolean isItemExist(String itemCode) {
        boolean isItemExist = false;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                break;
            }
        }
        return isItemExist;
    }

    public void updatePromotionDiscount(String itemCode, float newQuantity, int definitionMethod, String stepsBasedUOM, boolean excludeDiscount, boolean isBlocked) {
        if (isPromotionHeaderActiveAndOpen(itemCode, newQuantity, definitionMethod, stepsBasedUOM) && !isBlocked) {
            for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
                stepRecordNumber.updatePromotionDiscount(PCTotalQty, KARTotalQty, totalVal, definitionMethod, stepsBasedUOM, excludeDiscount);
            }
        } else {
            for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
                stepRecordNumber.resetPromotionDiscount();
            }
        }
    }

    public ArrayList<ItemPromotionData> updateItemsPriceAndDiscount(HashMap<String, ItemPromotionData> itemsDataMap) {
        ArrayList<ItemPromotionData> itemsData = new ArrayList<ItemPromotionData>();
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            itemsData.addAll(stepRecordNumber.updateItemsPriceAndDiscount(itemsDataMap));
        }
        return itemsData;
    }

    public ArrayList<StepDescription> getStepDescription(String itemCode) {
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

    public StepDescription getSelectedStepDetailDescription(String itemCode) {
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

    public StepRecordNumber getActiveStepRecordNumber() {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            APromotionStep promotionStep = stepRecordNumber.getActivePromotionStep();
            if (promotionStep != null) return stepRecordNumber;
        }
        return null;
    }

    public APromotionStep getActivePromotionStep() {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            APromotionStep promotionStep = stepRecordNumber.getActivePromotionStep();
            if (promotionStep != null) return promotionStep;
        }
        return null;
    }

    public void blockPromotion(String itemCode) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.blockPromotion();
        }
    }

    public void blockAllPromotion() {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.blockPromotion();
        }
    }

    public void unBlockPromotion(String itemCode, boolean excludeDiscount) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.unBlockPromotion(PCTotalQty, KARTotalQty, totalVal, excludeDiscount);
        }
    }

    public void unBlockAllPromotion(boolean excludeDiscount) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.unBlockPromotion(PCTotalQty, KARTotalQty, totalVal, excludeDiscount);
        }
    }

    public int getNextStepDiff(String itemCode) {
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

    public int getMinimumRemainForPopulationItems(String itemCode) {
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

    public int getPromotionStatus() {
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

    public void updateItemPricing(String itemCode, int uiIndex, double price, double discount, String unitType) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.updateItemPricing(itemCode, uiIndex, price, discount, unitType);
        }
    }

    public boolean isHaveItems() {
        boolean isHaveItems = false;
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            if (stepRecordNumber.isHaveItems()) {
                isHaveItems = true;
                break;
            }
        }
        return isHaveItems;
    }

    public boolean isNeedToOpenBonusPopup() {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            if (stepRecordNumber.isNeedToOpenBonusPopup()) {
                return true;
            }
        }
        return false;
    }

    public boolean isActivePromotionWasReset(String itemCode) {
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

    public void duplicateItem(String itemCode, int quantity) {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            boolean isItemExist = stepRecordNumber.isItemExist(itemCode);
            if (isItemExist) {
                stepRecordNumber.duplicateItem(itemCode, quantity);
            }
        }
    }

    public Set<String> getAllPromotionItemCodes() {
        StepRecordNumber stepRecordNumber = getActiveStepRecordNumber();
        if (stepRecordNumber != null) {
            return stepRecordNumber.getAllPromotionItemCodes();
        }
        return new HashSet<>();
    }

    public void resetPromotions() {
        for (StepRecordNumber stepRecordNumber : stepRecordNumbers) {
            stepRecordNumber.resetPromotions();
        }
    }

    public void ClearSteps() {
        stepRecordNumbers.clear();
    }
}
