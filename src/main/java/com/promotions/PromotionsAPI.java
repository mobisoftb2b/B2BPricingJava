package com.promotions;

import com.promotions.data.ItemPromotionData;
import com.promotions.data.PromotionHeader;
import com.promotions.interfaecs.IOrderObserver;
import com.promotions.manager.PromotionsDataManager;

import java.util.HashMap;
import java.util.Map;

public class PromotionsAPI implements IOrderObserver {

    public static int test() {
        return 2;
    }

    public void runPromotionsForCustomer(String customerKey, HashMap<String, ItemPromotionData> itemsDataMap) {
        PromotionsDataManager.getInstance().resetAllBonusData();
        PromotionsDataManager.getInstance().queryForDealKeys(customerKey);
        PromotionsDataManager.getInstance().resetPromotions();
        PromotionsDataManager.getInstance().setObserver(this);
        int position = -1;
        for (Map.Entry<String, ItemPromotionData> itemDataEntry : itemsDataMap.entrySet()) {
            String itemCode = itemDataEntry.getKey();
            ItemPromotionData itemPromotionData = itemDataEntry.getValue();

            position++;
            itemPromotionData.setUiIndex(0);

            PromotionsDataManager.getInstance().updateItemPricing(itemCode, itemPromotionData.getUiIndex(), itemPromotionData.getItemPricingData().getTotalStartValue(), itemPromotionData.getItemPricingData().getTotalDiscountValue(), itemPromotionData.getItemPricingData().getPriceUnitType());
            PromotionsDataManager.getInstance().updateDealForItemCode(position, itemCode, itemPromotionData.getTotalQuantityByUnitType());
        }
    }

    @Override
    public void onDealsUpdate(int position, String itemCode, PromotionHeader promotionHeader) {
        if (promotionHeader != null) {
            promotionHeader.updateItemsPriceAndDiscount(PromotionsDataManager.getInstance().getItemsDataMap());
            ItemPromotionData itemPromotionData = PromotionsDataManager.getInstance().getOrderUIItem(itemCode);
            if (itemPromotionData != null) {
                itemPromotionData.setStepDetailDescription(promotionHeader.getSelectedStepDetailDescription(itemCode));
            }
        }
    }

    @Override
    public void onPriorityUpdate(ItemPromotionData itemPromotionData, PromotionHeader promotionHeader) {

    }
}
