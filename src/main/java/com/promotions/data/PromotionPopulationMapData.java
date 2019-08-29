package com.promotions.data;

import com.promotions.database.PromotionsContract;
import com.promotions.database.PromotionsDatabase;
import com.mtn.mobisale.utils.DbUtil;
import com.mtn.mobisale.utils.LogUtil;
import org.sqlite.SQLiteException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class PromotionPopulationMapData {

    private static final String TAG = "PromotionPopulationMapData";
    private HashMap<Integer, PromotionItemMapListData> promotionItemsMap = new HashMap<>();
    private static PromotionPopulationMapData m_instance = null;


    public static PromotionPopulationMapData getInstance() {
        if (m_instance == null) {
            m_instance = new PromotionPopulationMapData();
        }
        // Return the instance
        return m_instance;
    }

    private PromotionPopulationMapData() {
    }

    public void executeQuery() {
        promotionItemsMap.clear();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        String sqlQ = "SELECT * FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_ITEMS_MAPPING + " ORDER BY " + PromotionsContract.PromotionItemMapping.PROMOTION_ITEM_MAPPING_POPULATION_CODE + " ASC";
        try {
            conn = DbUtil.connect(conn);
            st = conn.createStatement();

            // execute the query, and get a java resultset
            rs = st.executeQuery(sqlQ);

            if (rs == null) {
                return;
            }
            while (rs.next()) {
                int PopulationCode = rs.getInt(rs.findColumn(PromotionsContract.PromotionItemMapping.PROMOTION_ITEM_MAPPING_POPULATION_CODE));
                String ItemsFiledCode = rs.getString(rs.findColumn(PromotionsContract.PromotionItemMapping.PROMOTION_ITEM_MAPPING_POPULATION_ITEMS_FILED_CODE));
                PromotionItemMapData promotionItemMapData = new PromotionItemMapData(PopulationCode, ItemsFiledCode);
                PromotionItemMapListData promotionItemMapListData = promotionItemsMap.get(PopulationCode);
                if (promotionItemMapListData == null) {
                    promotionItemMapListData = new PromotionItemMapListData(PopulationCode);
                }
                promotionItemMapListData.addPromotionItemMapData(promotionItemMapData);
                promotionItemsMap.put(PopulationCode, promotionItemMapListData);
            }
        } catch (SQLiteException e) {
            // TODO: 2019-07-31 add log
            LogUtil.LOG.error("Error :"+e.getMessage());
        } catch (SQLException e) {
            LogUtil.LOG.error("Error :"+e.getMessage());
        } finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
    }

    public String getItemQueryStringFromPopulationCode(int populationCode, String whereCode) {
        String query = "";
        PromotionItemMapListData promotionItemMapListData = promotionItemsMap.get(populationCode);
        if (promotionItemMapListData == null) {
            return query;
        }
        query = promotionItemMapListData.getItemQueryString(whereCode);
        return query;
    }

    public ArrayList<String> getItems(int populationCode, String whereCode) {
        ArrayList<String> items = new ArrayList<String>();
        PromotionItemMapListData promotionItemMapListData = promotionItemsMap.get(populationCode);
        if (promotionItemMapListData == null) {
            return items;
        }
        items = promotionItemMapListData.getItems(whereCode);
        return items;
    }
}
