package com.promotions.data;

import java.util.ArrayList;

public class StepDescription {
    public Boolean IsBonus=false;
    public String BuyBoxOrUnit;
    public String GetBoxOrUnit;
    public String BonusBoxOrUnit;
    public String BonusMultiBoxOrUnit;
    public String Description = "";
    public String EspNumber;
    public int MinQtyForBonus = -1;
    public double PromotionDiscount;
    public double BonusDiscount;
    public int QtyForBonus;
    public int BonusQuantity;
    public ArrayList<String> BonusItemCodes;
}
