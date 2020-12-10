package com.promotions.data;

import com.promotions.manager.PromotionsDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: israel
 * Date: 5/8/13
 * Time: 8:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class PromotionHeader {

    public final String ESPNumber;
    public final String Type;
    public final String SupplyFrom;
    public final String SupplyTo;
    public final int DefinitionMethod;
    public final boolean ExcludeDiscount;
    public final String StepsBasedUOM;
    public final String StepsBasedCur;
    public final boolean NoBlockESP;
    public final boolean PromoInFocus;
    public final int ESPPriority;
    public final int ESPStatus;
    public final boolean BasketESP;
    public final boolean BonusESP;
    public final String ESPDescription;
    public final boolean ESPReturnRelevant;
    public PromotionStepManager promotionStepManager = new PromotionStepManager(new ArrayList<StepRecordNumber>());
    public boolean isBlocked;
    public int newPriority;
    public final boolean IsClassification;


    public PromotionHeader(String ESPNumber, String type, String supplyFrom, String supplyTo, int definitionMethod, boolean excludeDiscount, String stepsBasedUOM, String stepsBasedCur, boolean noBlockESP, boolean promoInFocus, int ESPPriority, int ESPStatus, boolean basketESP, boolean bonusESP, String ESPDescription, boolean ESPReturnRelevant, int newPriority, boolean isClassification) {
        this.ESPNumber = ESPNumber;
        Type = type;
        SupplyFrom = supplyFrom;
        SupplyTo = supplyTo;
        DefinitionMethod = definitionMethod;
        ExcludeDiscount = excludeDiscount;
        StepsBasedUOM = stepsBasedUOM;
        StepsBasedCur = stepsBasedCur;
        NoBlockESP = noBlockESP;
        PromoInFocus = promoInFocus;
        this.ESPPriority = ESPPriority;
        this.ESPStatus = ESPStatus;
        BasketESP = basketESP;
        BonusESP = bonusESP;
        this.ESPDescription = ESPDescription;
        this.ESPReturnRelevant = ESPReturnRelevant;
        this.newPriority = newPriority;
        isBlocked = Type != null && Type.equalsIgnoreCase("R130");
        IsClassification = isClassification;
    }

    public boolean isPromotionHeaderActiveAndOpen(String itemCode, int newQuantity, PromotionsDataManager promotionsDataManager) {
        return promotionStepManager.isPromotionHeaderActiveAndOpen(itemCode, newQuantity, DefinitionMethod, StepsBasedUOM, promotionsDataManager);
    }

    public void updatePromotionDiscount(String itemCode, float newQuantity, PromotionsDataManager promotionsDataManager) {
        promotionStepManager.updatePromotionDiscount(itemCode, newQuantity, DefinitionMethod, StepsBasedUOM, ExcludeDiscount, isBlocked, promotionsDataManager);
    }

    public boolean isItemExist(String itemCode) {
        return promotionStepManager.isItemExist(itemCode);
    }

    public ArrayList<ItemPromotionData> updateItemsPriceAndDiscount(HashMap<String, ItemPromotionData> itemsDataMap, PromotionsDataManager promotionsDataManager) {
        ArrayList<ItemPromotionData> itemsData = promotionStepManager.updateItemsPriceAndDiscount(itemsDataMap, promotionsDataManager);
        for (ItemPromotionData itemPromotionData : itemsData) {
//            PromotionHeader activePromotionHeader = PromotionsDataManager.getInstance().getPromotionHeader(itemsListData.getItemCode());
//            if(activePromotionHeader.ESPNumber.equalsIgnoreCase(ESPNumber)){
            itemPromotionData.setPromotionHeader(this);
//            }
        }
        return itemsData;
    }

    public ArrayList<StepDescription> getStepDescriptions(String itemCode) {
        return promotionStepManager.getStepDescription(itemCode);
    }

    public StepDescription getSelectedStepDetailDescription(String itemCode) {
        return promotionStepManager.getSelectedStepDetailDescription(itemCode);
    }

    public StepRecordNumber getActiveStepRecordNumber() {
        return promotionStepManager.getActiveStepRecordNumber();
    }

    public APromotionStep getActivePromotionStep() {
        return promotionStepManager.getActivePromotionStep();
    }

    public boolean blockPromotion(String itemCode, PromotionsDataManager promotionsDataManager) {
        if (NoBlockESP) {
            return false;
        }
        isBlocked = true;
        if (itemCode == null) {
            promotionStepManager.blockAllPromotion(promotionsDataManager);
        } else {
            promotionStepManager.blockPromotion(itemCode, promotionsDataManager);
        }
        return isBlocked;
    }

    public void unBlockPromotion(String itemCode, PromotionsDataManager promotionsDataManager) {
        isBlocked = false;
        if (itemCode == null) {
            promotionStepManager.unBlockAllPromotion(ExcludeDiscount, promotionsDataManager);
        } else {
            promotionStepManager.unBlockPromotion(itemCode, ExcludeDiscount, promotionsDataManager);
        }
    }

    public boolean blockAllPromotion(PromotionsDataManager promotionsDataManager) {
        if (NoBlockESP) {
            return false;
        }
        isBlocked = true;
        promotionStepManager.blockAllPromotion(promotionsDataManager);
        return isBlocked;
    }

    public void unBlockAllPromotion(PromotionsDataManager promotionsDataManager) {
        isBlocked = false;
        promotionStepManager.unBlockAllPromotion(ExcludeDiscount, promotionsDataManager);
    }

    public boolean isBlockedBySystem() {
        return Type != null && Type.equalsIgnoreCase("R130");
    }

    public int getNextStepDiff(String itemCode) {
        if (isBlocked) return -1;
        return promotionStepManager.getNextStepDiff(itemCode);
    }

    public int getMinimumRemainForPopulationItems(String itemCode) {
        if (isBlocked) return -1;
        return promotionStepManager.getMinimumRemainForPopulationItems(itemCode);
    }

    public int getPromotionStatus() {
        int status;
        if (isBlocked) {
            status = 2;
        } else {
            status = promotionStepManager.getPromotionStatus();
        }
        return status;
    }

    public void updateItemPricing(String itemCode, int uiIndex, double price, double discount, String unitType) {
        promotionStepManager.updateItemPricing(itemCode, uiIndex, price, discount, unitType);
    }

    public boolean isHaveItems() {
        return promotionStepManager.isHaveItems();
    }

    public boolean isNeedToOpenBonusPopup() {
        return promotionStepManager.isNeedToOpenBonusPopup();
    }

    public boolean isActivePromotionWasReset(String itemCode) {
        return promotionStepManager.isActivePromotionWasReset(itemCode);
    }

    public void duplicateItem(String itemCode, int quantity) {
        promotionStepManager.duplicateItem(itemCode, quantity);
    }

    public Set<String> getAllPromotionItemCodes() {
        return promotionStepManager.getAllPromotionItemCodes();
    }

    public void resetPromotions(PromotionsDataManager promotionsDataManager) {
        promotionStepManager.resetPromotions(promotionsDataManager);
    }

}
