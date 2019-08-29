package com.promotions.data;


import com.promotions.manager.PromotionsDataManager;

import java.util.ArrayList;

/**
 * Created by israel on 4/22/14.
 */
public class PromotionItemMapListData {
    public final int PopulationCode;
    public final ArrayList<PromotionItemMapData> PromotionItemMapDatas = new ArrayList<PromotionItemMapData>();

    public PromotionItemMapListData(int populationCode) {
        PopulationCode = populationCode;
    }

    public void addPromotionItemMapData(PromotionItemMapData promotionItemMapData) {
        PromotionItemMapDatas.add(promotionItemMapData);
    }

    public String getItemQueryString(String whereCode) {
        StringBuilder query = new StringBuilder();
        for (PromotionItemMapData promotionItemMapData : PromotionItemMapDatas) {
            if (query.length() == 0) {
                query.append(query).append(promotionItemMapData.ItemFiledCode).append("=").append(whereCode);
            } else {
                query.append(query).append(" OR ").append(promotionItemMapData.ItemFiledCode).append("=").append(whereCode);
            }
        }
        if (query.length() > 0) {
            query = new StringBuilder("(" + query + ")");
        }
        return query.toString();
    }

    public ArrayList<String> getItems(String whereCode) {
        ArrayList<String> items = new ArrayList<String>();
        PromotionItemMapData promotionItemMapData = PromotionItemMapDatas.get(0);
        if (promotionItemMapData.PopulationCode == 0) {
            items.addAll(PromotionsDataManager.getInstance().getAllItemsCode());
        } else {
            for (PromotionItemMapData promotionItemData : PromotionItemMapDatas) {
                items.addAll(PromotionsDataManager.getInstance().getItemCodesFromField(promotionItemData.ItemFiledCode, whereCode));
            }
        }
        return items;
    }
}
