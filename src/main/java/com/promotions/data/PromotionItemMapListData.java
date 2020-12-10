package com.promotions.data;


import com.promotions.manager.PromotionsDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by israel on 4/22/14.
 */
public class PromotionItemMapListData {
    public final int PopulationCode;
    public final List<PromotionItemMapData> PromotionItemMapDatas = Collections.synchronizedList(new ArrayList<PromotionItemMapData>());

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

    public ArrayList<String> getItems(String whereCode, Set<String> possibleItemIDs, PromotionsDataManager promotionsDataManager) {
        ArrayList<String> items = new ArrayList<String>();
        PromotionItemMapData promotionItemMapData = PromotionItemMapDatas.get(0);
        if (promotionItemMapData.PopulationCode == 0) {
            items.addAll(PromotionsDataManager.getAllItemsCode());
        } else {
            for (PromotionItemMapData promotionItemData : PromotionItemMapDatas) {
                items.addAll(promotionsDataManager.getItemCodesFromField(promotionItemData.ItemFiledCode, whereCode, possibleItemIDs));
            }
        }
        return items;
    }
}
