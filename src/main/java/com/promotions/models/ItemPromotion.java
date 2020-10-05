package com.promotions.models;

import com.promotions.data.StepDescription;

import java.util.ArrayList;

public class ItemPromotion {
    public boolean PartFromDeal;
    public String EspNumber;
    public double PromotionValue;
    public String PromotionDescription;
    public ArrayList<StepDescription> PromotionDetails;
    public int NextStepQuantity;
}
