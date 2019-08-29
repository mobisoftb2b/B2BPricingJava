package com.promotions.interfaecs;


import com.promotions.data.ItemPromotionData;
import com.promotions.data.PromotionHeader;

/**
 * Created with IntelliJ IDEA.
 * User: israel
 * Date: 2/13/13
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IOrderObserver {

    public void onDealsUpdate(int position, String itemCode, PromotionHeader promotionHeader);

    public void onPriorityUpdate(ItemPromotionData itemPromotionData, PromotionHeader promotionHeader);

}
