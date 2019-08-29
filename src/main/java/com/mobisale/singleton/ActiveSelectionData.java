package com.mtn.mobisale.singleton;


import com.mtn.mobisale.constants.Tables;
import com.mtn.mobisale.utils.DbUtil;
import com.mtn.mobisale.utils.LogUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class ActiveSelectionData {

    public static final String ACTIVE_SELECTION_SUPPLY_DATE = "supplyDate";
    public static final String TABLE_CUSTOMER_DETAILS = "Customers";
    public static final String TABLE_ITEMS = "Items";

    private static ActiveSelectionData m_instance = null;
    private HashMap<String, String> activeSelectionMap = new HashMap<String, String>();



    public ResultSet executeQueryasdasd(String query) {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            conn = DbUtil.connect(conn);


            st = conn.createStatement();
            LogUtil.LOG.error(query);

            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return null;
            }

        } catch (SQLException e) {

            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
        return rs;
    }

    public static ActiveSelectionData getInstance() {
        if (m_instance == null) {
            m_instance = new ActiveSelectionData();
        }
        // Return the instance
        return m_instance;
    }

    private ActiveSelectionData() {
    }

    public void clearSelection() {
        activeSelectionMap.clear();
    }

    public void updateCustomerSelection(String custKey) {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;

        HashMap<String, String> customerDataMap = new HashMap<String, String>();
        String query ="SELECT * "+ " FROM " + Tables.TABLE_CUSTOMERS + "WHERE Cust_Key="+custKey;
        try {
            conn = DbUtil.connect(conn);


            st = conn.createStatement();
            LogUtil.LOG.error(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs.next()) {
                customerDataMap.put("Cust_Key",rs.getString("Cust_Key"));
                customerDataMap.put("CustID",rs.getString("CustID"));
                customerDataMap.put("CustVatType",rs.getString("CustVatType"));
                customerDataMap.put("DistributionChannel",rs.getString("DistributionChannel"));
                customerDataMap.put("Division",rs.getString("Division"));
                customerDataMap.put("ArutzHafaza",rs.getString("ArutzHafaza"));
                customerDataMap.put("ListingCode",rs.getString("ListingCode"));
                customerDataMap.put("Payer",rs.getString("Payer"));
                customerDataMap.put("PriceList",rs.getString("PriceList"));
                customerDataMap.put("SalesOrganization",rs.getString("SalesOrganization"));
                customerDataMap.put("ShowPricing",rs.getString("ShowPricing"));
                customerDataMap.put("TAXK1",rs.getString("TAXK1"));
                customerDataMap.put("TanaiHovala",rs.getString("TanaiHovala"));
            }

            if (customerDataMap == null) {
                return;
            }
            HashMap<String, ArrayList<MtnMappingData.FieldMapData>> mtnMappingData = MtnMappingData.getInstance().getMtnMappingData();
            ArrayList<MtnMappingData.FieldMapData> customerFieldMapDatas = mtnMappingData.get(TABLE_CUSTOMER_DETAILS);
            if (customerFieldMapDatas == null) {
                return;
            }
            for (MtnMappingData.FieldMapData fieldMapData : customerFieldMapDatas) {
                activeSelectionMap.put(fieldMapData.MTNFied, customerDataMap.get(fieldMapData.MTNFied));
            }

        } catch (SQLException e) {
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
    }


    public void updateItemSelection(String itemCode) {
        HashMap<String, String> itemsDataMap = new HashMap<String, String>();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;

        String query ="SELECT * "+ " FROM " + Tables.TABLE_ITEMS + "WHERE ItemID="+itemCode;

        try {
            conn = DbUtil.connect(conn);


            st = conn.createStatement();
            LogUtil.LOG.error(query);

            // execute the query, and get a java resultset
            rs = st.executeQuery(query);
            if (rs.next()) {
                itemsDataMap.put("ItemID",rs.getString("ItemID"));
                itemsDataMap.put("ItemVatType",rs.getString("ItemVatType"));
                itemsDataMap.put("ProdHierarchy4",rs.getString("ProdHierarchy4"));
                itemsDataMap.put("TAXM1",rs.getString("TAXM1"));
                itemsDataMap.put("SaleUnit",rs.getString("SaleUnit"));
            }

            if (itemsDataMap == null) {
                return;
            }
            HashMap<String, ArrayList<MtnMappingData.FieldMapData>> mtnMappingData = MtnMappingData.getInstance().getMtnMappingData();
            ArrayList<MtnMappingData.FieldMapData> itemsFieldMapDatas = mtnMappingData.get(TABLE_ITEMS);
            if (itemsFieldMapDatas == null) {
                return;
            }
            for (MtnMappingData.FieldMapData fieldMapData : itemsFieldMapDatas) {
                activeSelectionMap.put(fieldMapData.MTNFied, itemsDataMap.get(fieldMapData.MTNFied));
            }

        } catch (SQLException e) {
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
    }


    public void updateSupplyDateSelection(String supplyDate) {
        activeSelectionMap.put(ACTIVE_SELECTION_SUPPLY_DATE, supplyDate);
    }

    public void resetSupplyDateSelection() {
        activeSelectionMap.put(ACTIVE_SELECTION_SUPPLY_DATE, null);
    }

    public String getValue(String key) {
        String selectedValue = activeSelectionMap.get(key);
        return selectedValue == null ? "0" : selectedValue;
    }
}
