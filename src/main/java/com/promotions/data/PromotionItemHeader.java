package com.promotions.data;

import com.promotions.manager.PromotionsDataManager;

import java.util.ArrayList;

public class PromotionItemHeader {

    public final String ItemCode;
    private ArrayList<PromotionItem> itemsData = new ArrayList<>();
    private int currentFocusIndex = 0;

    public PromotionItemHeader(String itemCode, PromotionItem promotionItem) {
        ItemCode = itemCode;
        itemsData.add(promotionItem);
        promotionItem.setUiIndex(size() - 1);
        currentFocusIndex = 0;
    }

    public int getCurrentFocusIndex() {
        return currentFocusIndex;
    }

    public int size() {
        return itemsData.size();
    }

    public PromotionItem duplicateItem(int quantity) {
        PromotionItem promotionItem = getItem(0);
        if (promotionItem == null) return null;
        PromotionItem duplicateItem = new PromotionItem(promotionItem.ItemCode, promotionItem.RealPrice, promotionItem.RealDiscount, promotionItem.UnitsQuantity, promotionItem.BaseUnit);
        duplicateItem.setDuplicateItem(true);
        itemsData.add(duplicateItem);
        duplicateItem.setUiIndex(size() - 1);

        return duplicateItem;
    }

    public PromotionItem getItem() {
        try {
            return itemsData.get(currentFocusIndex) == null ? getItem(0) : itemsData.get(currentFocusIndex);
        } catch (Exception e) {
            // TODO: 2019-07-29 add log
            return getItem(0);
        }
    }

    public PromotionItem getItem(int index) {
        try {
            return itemsData.get(index);
        } catch (Exception e) {
            // TODO: 2019-07-29 add log
        }
        return null;
    }

    public ArrayList<PromotionItem> getAllItem() {
        try {
            return itemsData;
        } catch (Exception e) {
            // TODO: 2019-07-29 add log
        }
        return new ArrayList<>();
    }

    public float getTotalPCQuantity() {
        int totalPCQuantity = 0;
        for (PromotionItem promotionItem : itemsData) {
            totalPCQuantity += promotionItem.getPCQuantity();
        }
        return totalPCQuantity;
    }

    public float getTotalKARQuantity() {
        int totalKarQuantity = 0;
        for (PromotionItem promotionItem : itemsData) {
            totalKarQuantity += promotionItem.getKARQuantity();
        }
        return totalKarQuantity;
    }

    public float getTotalValQuantity() {
        int totalValQuantity = 0;
        for (PromotionItem promotionItem : itemsData) {
            totalValQuantity += promotionItem.getValQuantity();
        }
        return totalValQuantity;
    }

    public void updateQuantity(float newQuantity) {
        ItemPromotionData itemPromotionData = PromotionsDataManager.getInstance().getOrderUIItem(ItemCode);
        for (PromotionItem promotionItem : itemsData) {
            if (itemPromotionData != null) {
                promotionItem.updateQuantity(itemPromotionData.getTotalPCItemUserAmount());
            }
        }
    }

    public void resetQuantity() {
        for (PromotionItem promotionItem : itemsData) {
            promotionItem.resetQuantity();
        }
    }

    public void resetPriceAndDiscount(String espNumber) {
        for (PromotionItem promotionItem : itemsData) {
            promotionItem.resetPriceAndDiscount(espNumber);
        }
    }

    public void resetPromotions(String espNumber) {
        for (PromotionItem promotionItem : itemsData) {
            promotionItem.resetQuantity();
            promotionItem.resetPriceAndDiscount(espNumber);
        }
    }
}
