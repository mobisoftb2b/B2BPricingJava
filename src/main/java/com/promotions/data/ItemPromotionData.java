package com.promotions.data;

import com.promotions.models.Item;

public class ItemPromotionData {
    public static final String KARTON_UNIT = "KAR";
    public static final String PC_UNIT = "PC";
    public static final String KG_UNIT = "KG";
    private int unitInKar = 1;
    private float unitWeight = 1;
    private int pcQuantity = 0;
    private int karQuantity = 0;
    private float kgQuantity = 0;
    private String itemCode;
    private ItemPricingPromotionsData itemPricingData;
    private Item item;
    //
    private PromotionHeader promotionHeader;
    private int uiIndex = 0;
    //
    private boolean partFromDeal;
    private String stepDetailDescription = "";
    private int nextStepQuantity;
    private String espNumber = "";
    private String espDescription = "";


    public ItemPromotionData(String itemCode) {
        this.itemCode = itemCode;
        itemPricingData = new ItemPricingPromotionsData();
    }

    public String getEspNumber() {
        return espNumber == null ? "" : espNumber;
    }

    public String getEspDescription() {
        return espDescription == null ? "" : espDescription;
    }

    public void setPromotionHeader(PromotionHeader promotionHeader) {
        this.promotionHeader = promotionHeader;
        if (promotionHeader != null)
        {
            espNumber = promotionHeader.ESPNumber;
            espDescription = promotionHeader.ESPDescription;
        }
    }

    public PromotionHeader getPromotionHeader() {
        return promotionHeader;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public int getUiIndex() {
        return uiIndex;
    }

    public void setUiIndex(int uiIndex) {
        this.uiIndex = uiIndex;
    }

    public boolean isHaveAmount() {
        return getTotalQuantityByUnitType() > 0;
    }

    public float getTotalQuantityByUnitType() {
        float quantity = 0;
        if (getItemPricingData().getPriceUnitType().equalsIgnoreCase(PC_UNIT)) {
            quantity = getTotalPCItemUserAmount();
        } else if (getItemPricingData().getPriceUnitType().equalsIgnoreCase(KARTON_UNIT)) {
            quantity = getTotalKARItemUserAmount();
        } else if (getItemPricingData().getPriceUnitType().equalsIgnoreCase(KG_UNIT)) {
            quantity = getTotalKGItemUserAmount();
        }
        return quantity;
    }

    public int getTotalPCItemUserAmount() {
        return karQuantity * unitInKar + pcQuantity;
    }

    public int getTotalKARItemUserAmount() {
        return karQuantity + (pcQuantity / unitInKar);
    }

    public float getTotalKGItemUserAmount() {
        return ((karQuantity * unitInKar + pcQuantity) * unitWeight) + kgQuantity;
    }

    public boolean isItemPricingInitialized() {
        return true;
    }

    public ItemPricingPromotionsData getItemPricingData() {
        return itemPricingData;
    }

    public void setItemPricingData(ItemPricingPromotionsData itemPricingData) {
        this.itemPricingData = itemPricingData;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public float getUnitInKar() {
        return unitInKar;
    }

    public void setUnitInKar(int unitInKar) {
        this.unitInKar = unitInKar;
    }

    public float getUnitWeight() {
        return unitWeight;
    }

    public void setUnitWeight(float unitWeight) {
        this.unitWeight = unitWeight;
    }

    public int getPcQuantity() {
        return pcQuantity;
    }

    public void setPcQuantity(int pcQuantity) {
        this.pcQuantity = pcQuantity;
    }

    public int getKarQuantity() {
        return karQuantity;
    }

    public void setKarQuantity(int karQuantity) {
        this.karQuantity = karQuantity;
    }

    public float getKgQuantity() {
        return kgQuantity;
    }

    public void setKgQuantity(float kgQuantity) {
        this.kgQuantity = kgQuantity;
    }

    public void setPartFromDeal(boolean partFromDeal) {
        this.partFromDeal = partFromDeal;
    }

    public boolean isPartFromDeal() {
        return partFromDeal;
    }

    /*public ArrayList<ItemBonusData> getAllItemBonusData() {
        return new ArrayList<>(itemBonusDataMap.values());
    }

    public HashMap<String, ItemBonusData> getAllItemBonusDataMap() {
        return itemBonusDataMap;
    }
*/
    public String getStepDetailDescription() {
        return stepDetailDescription;
    }

    public void setStepDetailDescription(String stepDetailDescription) {
        this.stepDetailDescription = stepDetailDescription;
    }

    public int getNextStepQuantity() { return nextStepQuantity;}
    public void setNextStepQuantity(int nextStepQuantity) {
        this.nextStepQuantity = nextStepQuantity;
    }
}
