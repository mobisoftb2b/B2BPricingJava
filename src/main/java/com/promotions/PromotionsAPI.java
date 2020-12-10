package com.promotions;

import com.mobisale.singleton.ActiveSelectionData;
import com.mobisale.utils.LogUtil;
import com.promotions.data.ItemPromotionData;
import com.promotions.data.PromotionHeader;
import com.promotions.data.PromotionPopulationMapData;
import com.promotions.interfaecs.IOrderObserver;
import com.promotions.manager.PromotionsDataManager;

import java.util.HashMap;
import java.util.Map;

public class PromotionsAPI //implements IOrderObserver
{

    public static int test() {
        return 2;
    }

    public synchronized void runPromotionsForCustomer(String customerKey, HashMap<String, ItemPromotionData> itemsDataMap, String DocNum, String RequestId, ActiveSelectionData activeSelectionData, PromotionsDataManager promotionsManager) {
        //promotionsManager.setObserver(this);
        PromotionPopulationMapData promotionPopulationMapData = new PromotionPopulationMapData();
        promotionsManager.resetAllBonusData();
        promotionsManager.startQueryForCustomer(customerKey, activeSelectionData);
        promotionsManager.queryForDealKeys(customerKey, DocNum, RequestId);

        promotionsManager.queryForDealsByCustomer(customerKey, itemsDataMap, DocNum, RequestId, activeSelectionData, promotionPopulationMapData);
        //promotionsManager.removeDealsWithNoItems();
        //promotionsManager.removeStepRecordsWithNoItems();

        promotionsManager.resetPromotions(promotionsManager);
        //PromotionsDataManager..setObserver(this);
        int position = -1;
        for (Map.Entry<String, ItemPromotionData> itemDataEntry : itemsDataMap.entrySet()) {
            try {
                String itemCode = itemDataEntry.getKey();
                ItemPromotionData itemPromotionData = itemDataEntry.getValue();

                position++;
                itemPromotionData.setUiIndex(0);

                promotionsManager.updateItemPricing(itemCode, itemPromotionData.getUiIndex(), itemPromotionData.getItemPricingData().getTotalStartValue(), itemPromotionData.getItemPricingData().getTotalDiscountValue(), itemPromotionData.getItemPricingData().getPriceUnitType());
                promotionsManager.updateDealForItemCode(position, itemCode, itemPromotionData.getTotalQuantityByUnitType(), promotionsManager);
            } catch (Exception e) {
                LogUtil.LOG.error(e);
            }

        }
    }

/*
    @Override
    public void onDealsUpdate(String itemCode, String customerKey, PromotionHeader promotionHeader) {
        if (promotionHeader != null) {
            promotionHeader.updateItemsPriceAndDiscount(PromotionsDataManager.getItemsDataMap());
            ItemPromotionData itemPromotionData = PromotionsDataManager.getOrderUIItem(itemCode);
            if (itemPromotionData != null) {
                itemPromotionData.setStepDetailDescription(promotionHeader.getSelectedStepDetailDescription(itemCode));
            }
        }
    }
*/

}
