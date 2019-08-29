package com.promotions.data;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class PromotionItemHeaderMap {

    private HashMap<String, PromotionItemHeader> promotionItems = new HashMap<String, PromotionItemHeader>();
    public PromotionItemHeaderMap() {
    }

    public void clear() {
        promotionItems.clear();
    }

    public int size() {
        return promotionItems.size();
    }

    public Set<String> keySet() {
        return promotionItems.keySet();
    }

    public void buildMainItem(PromotionItem promotionItem) {
        PromotionItemHeader promotionItemHeader = new PromotionItemHeader(promotionItem.ItemCode, promotionItem);
        promotionItems.put(promotionItem.ItemCode, promotionItemHeader);
    }

    public void remove(ArrayList<String> excludeItemCodes) {
        for (String excludeItemCode : excludeItemCodes) {
            boolean isRemoved = promotionItems.remove(excludeItemCode) != null;
        }
    }

    public PromotionItem duplicateItem(String itemCode, int quantity) {
        PromotionItemHeader promotionItemHeader = promotionItems.get(itemCode);
        if (promotionItemHeader == null) {
            return null;
        }
        return promotionItemHeader.duplicateItem(quantity);
    }

    public PromotionItemHeader getPromotionItemHeader(String itemCode) {
        return promotionItems.get(itemCode);
    }

    public Collection<PromotionItemHeader> getAllPromotionItemHeader() {
        return promotionItems.values();
    }

    public PromotionItem getPromotionItem(String itemCode) {
        PromotionItemHeader promotionItemHeader = promotionItems.get(itemCode);
        if (promotionItemHeader != null) {
            return promotionItemHeader.getItem();
        }
        return null;
    }

    public PromotionItem getPromotionItem(String itemCode, int index) {
        PromotionItemHeader promotionItemHeader = promotionItems.get(itemCode);
        if (promotionItemHeader != null) {
            return promotionItemHeader.getItem(index);
        }
        return null;
    }

    public ArrayList<PromotionItem> getAllItem() {
        ArrayList<PromotionItem> promotionsItems = new ArrayList<>();
        Collection<PromotionItemHeader> itemDataHeaders = promotionItems.values();
        for (PromotionItemHeader promotionItemHeader : itemDataHeaders) {
            promotionsItems.addAll(promotionItemHeader.getAllItem());
        }
        return promotionsItems;
    }

    public ArrayList<PromotionItem> getAllItem(String itemCode) {
        PromotionItemHeader itemDataHeader = promotionItems.get(itemCode);
        if (itemDataHeader != null) {
            return itemDataHeader.getAllItem();
        }
        return new ArrayList<>();
    }
}
