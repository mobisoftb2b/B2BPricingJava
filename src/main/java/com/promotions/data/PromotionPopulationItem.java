package com.promotions.data;


import com.promotions.manager.PromotionsDataManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by israel on 4/22/14.
 */
public class PromotionPopulationItem {
    public final String ESPNumber;
    public final int RecordNumber;
    public final int RecordSequence;
    public final int SelectedCharacteristics;
    public final String MaterialCharValue;
    public final int MinVarProd;
    public final int MinTotQty;
    public final boolean Mandatory;
    public final String UOMForMinQty;
    public final int MinTotVal;
    public com.promotions.data.PromotionItemHeaderMap PromotionItemHeaderMap = new PromotionItemHeaderMap();
    public int totalPCQty;
    public int totalKarQty;
    public double totalVal;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    public PromotionPopulationItem(String ESPNumber, int recordNumber, int recordSequence, int selectedCharacteristics, String materialCharValue, int minVarProd, int minTotQty, boolean mandatory, String UOMForMinQty, int minTotVal, Set<String> possibleItemIDs) {
        this.ESPNumber = ESPNumber;
        RecordNumber = recordNumber;
        RecordSequence = recordSequence;
        SelectedCharacteristics = selectedCharacteristics;
        MaterialCharValue = materialCharValue;
        MinVarProd = minVarProd;
        MinTotQty = minTotQty;
        Mandatory = mandatory;
        this.UOMForMinQty = UOMForMinQty == null ? ItemPromotionData.PC_UNIT : UOMForMinQty;
        MinTotVal = minTotVal;
        setItems(possibleItemIDs);
    }

    private void setItems(Set<String> possibleItemIDs) {
        ArrayList<String> itemCodes = PromotionPopulationMapData.getInstance().getItems(SelectedCharacteristics, MaterialCharValue, possibleItemIDs);
        for (String itemCode : itemCodes) {
            try {
                ItemPromotionData itemPromotionData = PromotionsDataManager.getOrderUIItem(itemCode);
                if (itemPromotionData == null) continue;
                PromotionItem promotionItem = new PromotionItem(itemCode, itemPromotionData.getItemPricingData().getTotalStartValue(), 0, Math.round(itemPromotionData.getUnitInKar()), ItemPromotionData.PC_UNIT);
                PromotionItemHeaderMap.buildMainItem(promotionItem);
            } catch (Exception e) {
                // TODO: 2019-07-31 add log
            }
        }
    }

    public void excludeIfNeeded(ArrayList<String> excludeItemCodes) {
        PromotionItemHeaderMap.remove(excludeItemCodes);
    }


    public boolean updateQuantity(String itemCode, float newQuantity) {
        PromotionItemHeader promotionItemHeader = PromotionItemHeaderMap.getPromotionItemHeader(itemCode);
        boolean isItemExist = false;
        if (promotionItemHeader != null) {
            isItemExist = true;
            promotionItemHeader.updateQuantity(newQuantity);
        }
        totalPCQty = 0;
        totalKarQty = 0;
        totalVal = 0;
        for (PromotionItem item : PromotionItemHeaderMap.getAllItem()) {
            totalPCQty += item.getPCQuantity();
            totalKarQty += item.getKARQuantity();
            totalVal += item.getValQuantity();
        }
        return isItemExist;
    }

    public boolean isValid(int definitionMethod, String StepsBasedUOM) {
        boolean isValid = false;
        if (PromotionItemHeaderMap.size() == 0) {
            return !isValid;
        }
        if (Mandatory) {
            for (PromotionItem promotionItem : PromotionItemHeaderMap.getAllItem()) {
                if (promotionItem.getPCQuantity() > 0) {
                    isValid = true;
                    break;
                }
            }
        } else {
            isValid = true;
        }
        if (isValid) {
            if (definitionMethod == 1) {
                if (UOMForMinQty.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                    isValid = totalPCQty >= MinTotQty;
                } else {
                    isValid = totalKarQty >= MinTotQty;
                }
            } else {
                isValid = totalVal >= MinTotVal;
            }
        }
        if (isValid) {
            int count = 0;
            for (PromotionItemHeader promotionItemHeader : PromotionItemHeaderMap.getAllPromotionItemHeader()) {
                if (promotionItemHeader.getTotalPCQuantity() > 0) {
                    count++;
                }
            }
            isValid = count >= MinVarProd;
        }
        return isValid;
    }


    public void updatePriceAndDiscount(APromotionStep activePromotionStep, boolean excludeDiscount) {
        PromotionHeader activePromotionHeader = PromotionsDataManager.getPromotionHeaderByESPNumber(ESPNumber);
        boolean isAllowDiscountWithPromotion = true;
        for (PromotionItemHeader promotionItemHeader : PromotionItemHeaderMap.getAllPromotionItemHeader()) {
            for (PromotionItem promotionItem : promotionItemHeader.getAllItem()) {
                ItemPromotionData itemPromotionData = PromotionsDataManager.getOrderUIItem(promotionItemHeader.ItemCode, promotionItem.getUiIndex());
                if (itemPromotionData.isItemPricingInitialized()) {
                    double netoPrice = 0;
                    double netoDiscount = 0;
                    double promotionNetoDiscount = 0;
                    double originalSubTotalsPromotionDiscountValue = itemPromotionData.getItemPricingData().getSubTotalsPromotionDiscountValue();
                    double subTotalsPromotionDiscountValue = originalSubTotalsPromotionDiscountValue;
                    if (isAllowDiscountWithPromotion) {
                        subTotalsPromotionDiscountValue = 0;
                    }
                    if (activePromotionStep.PromotionType == 2 || activePromotionStep.PromotionType == 4) {
                        if (excludeDiscount) {
                            if (subTotalsPromotionDiscountValue < activePromotionStep.PromotionDiscount) {
//                            netoPrice = (1 - activePromotionStep.PromotionDiscount / 100) * promotionItem.RealPrice;
//                            netoDiscount = activePromotionStep.PromotionDiscount;
                                promotionNetoDiscount = activePromotionStep.PromotionDiscount - subTotalsPromotionDiscountValue;
                            } else {
                                //put 0 talk with avi
//                            netoPrice = (1 - subTotalsPromotionDiscountValue / 100) * promotionItem.RealPrice;
//                            netoDiscount = promotionItem.RealDiscount;
                                promotionNetoDiscount = 0;
                            }
                        } else {
//                        netoPrice = (1 - promotionItem.RealDiscount / 100) * promotionItem.RealPrice;
//                        netoPrice = (1 - activePromotionStep.PromotionDiscount / 100) * netoPrice;
//                        netoDiscount = (1 - (netoPrice / promotionItem.RealPrice)) * 100;
                            promotionNetoDiscount = activePromotionStep.PromotionDiscount;
                        }
//                    promotionItem.updatePrice(netoPrice + itemsListData.getItemPricingData().getDepositValue());
//                    promotionItem.updateDiscount(netoDiscount);
                        promotionItem.updatePromotionNetoDiscount(promotionNetoDiscount);
                    } else if (activePromotionStep.PromotionType == 3 || activePromotionStep.PromotionType == 5) {
                        if (promotionItem.BaseUnit.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                            if (activePromotionStep.PriceBQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                                netoPrice = activePromotionStep.PromotionPrice;
                            } else {
                                netoPrice = activePromotionStep.PromotionPrice / promotionItem.UnitsQuantity;
                            }
                        } else {
                            if (activePromotionStep.PriceBQtyUOM.equalsIgnoreCase(ItemPromotionData.PC_UNIT)) {
                                netoPrice = activePromotionStep.PromotionPrice * promotionItem.UnitsQuantity;
                            } else {
                                netoPrice = activePromotionStep.PromotionPrice;
                            }
                        }
                        netoDiscount = (1 - (netoPrice / promotionItem.RealPrice)) * 100;
                        if (originalSubTotalsPromotionDiscountValue < netoDiscount) {
                            promotionNetoDiscount = netoDiscount - originalSubTotalsPromotionDiscountValue;
                            excludeDiscount = true;
                        } else if (activePromotionStep.PromotionType == 5) {
                            promotionNetoDiscount = netoDiscount - originalSubTotalsPromotionDiscountValue;
                            excludeDiscount = true;
                        } else {
                            promotionNetoDiscount = 0;
                            excludeDiscount = false;
                        }

//                    promotionItem.updatePrice(netoPrice + itemsListData.getItemPricingData().getDepositValue());
//                    promotionItem.updateDiscount(netoDiscount);
//                    promotionNetoDiscount = (1 - (netoPrice / (itemsListData.getItemPricingData().getItemNetoPrice() - itemsListData.getItemPricingData().getDepositValue()))) * 100;
                        promotionItem.updatePromotionNetoDiscount(promotionNetoDiscount);
                    }
                    itemPromotionData.getItemPricingData().updatePromotionCondition(netoDiscount, promotionNetoDiscount, excludeDiscount, activePromotionHeader.IsClassification, (activePromotionStep.PromotionType == 2 || activePromotionStep.PromotionType == 4));
//                    promotionItem.updatePrice(itemPromotionData.getItemPricingData().getItemNetoPrice());
//                    promotionItem.updateDiscount(itemPromotionData.getItemPricingData().getItemDiscountValue());
//                    promotionItem.updateNetoTotalPrice(itemPromotionData.getItemPricingData().getTotalItemPriceNeto());
                }
            }
        }
    }

    public void resetPromotionDiscount() {
        for (PromotionItemHeader promotionItemHeader : PromotionItemHeaderMap.getAllPromotionItemHeader()) {
            promotionItemHeader.resetPriceAndDiscount(ESPNumber);
        }
    }

    public void resetPromotions() {
        for (PromotionItemHeader promotionItemHeader : PromotionItemHeaderMap.getAllPromotionItemHeader()) {
            promotionItemHeader.resetPromotions(ESPNumber);
        }
    }

    public boolean isItemExist(String itemCode) {
        PromotionItemHeader promotionItemHeader = PromotionItemHeaderMap.getPromotionItemHeader(itemCode);
        boolean isItemExist = false;
        if (promotionItemHeader != null) {
            isItemExist = true;
        }
        return isItemExist;
    }

    public ArrayList<ItemPromotionData> updateItemsPriceAndDiscount(HashMap<String, ItemPromotionData> itemsDataMap, boolean isActivePromotionStep, boolean isBonusStep) {
        ArrayList<ItemPromotionData> itemsData = new ArrayList<>();
        for (PromotionItemHeader promotionItemHeader : PromotionItemHeaderMap.getAllPromotionItemHeader()) {
            for (PromotionItem promotionItem : promotionItemHeader.getAllItem()) {
                ItemPromotionData itemPromotionData = PromotionsDataManager.getOrderUIItem(promotionItemHeader.ItemCode, promotionItem.getUiIndex());
//                itemPromotionData.setItemPercent(promotionItem.getNetoDiscount());
//                itemPromotionData.setItemPriceNeto(promotionItem.getNetoPrice());
//                itemPromotionData.setTotalItemPriceNeto(promotionItem.getNetoTotalPrice());
//                itemPromotionData.setItemPromotionNetoPercent(promotionItem.getPromotionNetoDiscount());
                itemPromotionData.setPartFromDeal(isActivePromotionStep);
                itemsData.add(itemPromotionData);
            }
        }
        return itemsData;
    }

    public void updateItemPricing(String itemCode, int uiIndex, double price, double discount, String unitType) {
        PromotionItem promotionItem = PromotionItemHeaderMap.getPromotionItem(itemCode, uiIndex);
        if (promotionItem != null) {
            promotionItem.updateItemPricing(price, discount, unitType);
        }
    }

    public void duplicateItem(String itemCode, int quantity) {
        PromotionItemHeaderMap.duplicateItem(itemCode, quantity);
    }
}
