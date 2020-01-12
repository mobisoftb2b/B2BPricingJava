package com.mobisale.singleton;


import com.mobisale.constants.Tables;
import com.mobisale.singleton.MtnMappingData;
import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.mobisale.utils.SqlLiteUtil;

public class ActiveSelectionData {

    public static final String ACTIVE_SELECTION_SUPPLY_DATE = "supplyDate";
    public static final String TABLE_CUSTOMER_DETAILS = "B2B_Customers";
    public static final String TABLE_CUSTOMER_DETAILS_TAMBOUR = "Customers";
    public static final String TABLE_ITEMS = "B2B_Items";
    public static final String TABLE_ITEMS_TAMBOUR = "Items";

    private static ActiveSelectionData m_instance = null;
    private HashMap<String, String> activeSelectionMap = new HashMap<String, String>();
    private HashMap<String, HashMap<String, String>> customersAllDataMap = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, HashMap<String, String>> customerCategoryAllDataMap = new HashMap<String, HashMap<String, String>>();
    public HashMap<String, HashMap<String, String>> itemsAllDataMap = new HashMap<String, HashMap<String, String>>();

    SqlLiteUtil sqlLiteUtil = new SqlLiteUtil();

    public ResultSet executeQueryasdasd(String query) {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            conn = DbUtil.connect(conn);


            st = conn.createStatement();
            LogUtil.LOG.info(query);

            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return null;
            }

        } catch (SQLException e) {

            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 116");
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

    /*
    public void updateCustomerSelectionStrauss(String Cust_Key) {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;

        HashMap<String, String> customerDataMap = new HashMap<String, String>();
        String query ="SELECT * "+ " FROM " + TABLE_CUSTOMERS_STRAUSS + " WHERE Cust_Key='"+Cust_Key + "'";
        try {
            conn = DbUtil.connect(conn);


            st = conn.createStatement();
            LogUtil.LOG.info(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs.next()) {
                customerDataMap.put("CustID", rs.getString("CustID"));
                customerDataMap.put("Cust_Key",rs.getString("Cust_Key"));
                customerDataMap.put("State",rs.getString("State"));
                customerDataMap.put("BusinessType1",rs.getString("BusinessType1"));
                customerDataMap.put("Division",rs.getString("Division"));
                customerDataMap.put("CustHier1",rs.getString("CustHier1"));
                customerDataMap.put("CustHier2",rs.getString("CustHier2"));
                customerDataMap.put("CustHier3",rs.getString("CustHier3"));
                customerDataMap.put("Payer",rs.getString("Payer"));
                customerDataMap.put("CustomerPriceList",rs.getString("CustomerPriceList"));
                customerDataMap.put("TAXK1",rs.getString("TAXK1"));
                customerDataMap.put("SalesOrganization",rs.getString("SalesOrganization"));
                customerDataMap.put("DistributionChannel",rs.getString("DistributionChannel"));
                customerDataMap.put("BusinessType2",rs.getString("BusinessType2"));
                customerDataMap.put("VIP",rs.getString("VIP"));
                customerDataMap.put("FoodLaw",rs.getString("FoodLaw"));
                customerDataMap.put("ListingCode",rs.getString("ListingCode"));
                customerDataMap.put("MarketType",rs.getString("MarketType"));
                customerDataMap.put("Sector",rs.getString("Sector"));
                customerDataMap.put("TradeDiscountCode",rs.getString("TradeDiscountCode"));
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
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 118");
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
    }
    */
    public void UpdateCustomerTablesSelection(String custID, String Cust_Key) {
        if (System.getenv("PROVIDER").equalsIgnoreCase("noa")) {
            updateCustomerSelection(custID, Tables.TABLE_CUSTOMERS);
        }
        else
        {
            updateCustomerSelection(Cust_Key, Tables.TABLE_CUSTOMERS_PRICING);
        }
        if (System.getenv("PROVIDER").equalsIgnoreCase("strauss")) {
            updateCustCategorySelection(Cust_Key);
        }
    }

    private void updateCustCategorySelection(String Cust_Key)
    {
        ArrayList<MtnMappingData.FieldMapData> fieldMappingData =  MtnMappingData.getInstance().getMtnMappingData().get(Tables.TABLE_CUSTCATEGORY);
        if (fieldMappingData == null) {
            return;
        }
        HashMap<String, String> customerCategoryDataMap = null;
        customerCategoryDataMap = customerCategoryAllDataMap.get(Cust_Key);
        if (customerCategoryDataMap == null) {
            customerCategoryDataMap = new HashMap<String, String>();
            ResultSet rs = null;
            Statement st = null;
            Connection conn = null;

            String query = "SELECT * " + " FROM " + Tables.GetFullTableName(Tables.TABLE_CUSTCATEGORY) + " WHERE Cust_Key='" + Cust_Key + "'";
            try {
                conn = DbUtil.connect(conn);


                st = conn.createStatement();
                LogUtil.LOG.info(query);
                // execute the query, and get a java resultset
                rs = st.executeQuery(query);

                if (rs.next()) {
                    customerCategoryDataMap.put("Cust_Key", rs.getString("Cust_Key"));
                    customerCategoryDataMap.put("CustID", rs.getString("CustID"));
                    for (MtnMappingData.FieldMapData fieldMapData : fieldMappingData) {
                        String fieldValue = rs.getString(fieldMapData.MTNFied);
                        fieldValue = fieldValue != null ? fieldValue.trim() : fieldValue;
                        customerCategoryDataMap.put(fieldMapData.MTNFied, fieldValue);
                    }
                }
                customerCategoryAllDataMap.put(Cust_Key, customerCategoryDataMap);
            } catch (SQLException e) {
                LogUtil.LOG.error("Error in line: " + e.getStackTrace()[0].getLineNumber() + ", Error Message:" + e.getMessage() + " 118");
            } finally {
                DbUtil.CloseConnection(conn, rs, st);
            }
        }
        for (MtnMappingData.FieldMapData fieldMapData : fieldMappingData) {
                activeSelectionMap.put(fieldMapData.MTNFied, customerCategoryDataMap.get(fieldMapData.MTNFied));
        }


    }

    private void updateCustomerSelection(String Cust_Key, String tableName)
    {
        ArrayList<MtnMappingData.FieldMapData> fieldMappingData =  MtnMappingData.getInstance().getMtnMappingData().get(tableName);
        if (fieldMappingData == null) {
            return;
        }

        HashMap<String, String> customerDataMap = null;
        customerDataMap = customersAllDataMap.get(Cust_Key);
        if (customerDataMap == null) {
            customerDataMap = new HashMap<String, String>();
            ResultSet rs = null;
            Statement st = null;
            Connection conn = null;

            String query = "SELECT * " + " FROM " + Tables.GetFullTableName(tableName) + " WHERE Cust_Key='" + Cust_Key + "'";
            try {
                conn = DbUtil.connect(conn);

                st = conn.createStatement();
                LogUtil.LOG.info(query);
                // execute the query, and get a java resultset
                rs = st.executeQuery(query);

                if (rs.next()) {
                    customerDataMap.put("Cust_Key", rs.getString("CustID"));
                    //customerDataMap.put("CustID",rs.getString("CustID"));
                    for (MtnMappingData.FieldMapData fieldMapData : fieldMappingData) {
                        String fieldValue = rs.getString(fieldMapData.MTNFied);
                        fieldValue = fieldValue != null ? fieldValue.trim() : fieldValue;
                        customerDataMap.put(fieldMapData.MTNFied, fieldValue);
                    }
                }
                customersAllDataMap.put(Cust_Key, customerDataMap);
            } catch (SQLException e) {
                LogUtil.LOG.error("Error in line: " + e.getStackTrace()[0].getLineNumber() + ", Error Message:" + e.getMessage() + " 118");
            } finally {
                DbUtil.CloseConnection(conn, rs, st);
            }
        }

        for (MtnMappingData.FieldMapData fieldMapData : fieldMappingData) {
                activeSelectionMap.put(fieldMapData.MTNFied, customerDataMap.get(fieldMapData.MTNFied));
        }
    }

    /*
    public void updateCustomerSelectionCommon(String custID) {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;

        HashMap<String, String> customerDataMap = new HashMap<String, String>();
        String query ="SELECT * "+ " FROM " + Tables.TABLE_CUSTOMERS + " WHERE CustID='"+custID + "'";
        try {
            conn = DbUtil.connect(conn);


            st = conn.createStatement();
            LogUtil.LOG.info(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs.next()) {
                customerDataMap.put("Cust_Key", rs.getString("CustID"));
                customerDataMap.put("CustID",rs.getString("CustID"));
                customerDataMap.put("CustVatType",rs.getString("CustVatType"));
                customerDataMap.put("DistributionChannel",rs.getString("DistributionChannel"));
                customerDataMap.put("Division",rs.getString("Division"));
                if (System.getenv("PROVIDER") == "tambour")
                    customerDataMap.put("ArutzHafaza",rs.getString("ArutzHafaza"));
                customerDataMap.put("ListingCode",rs.getString("ListingCode"));
                customerDataMap.put("Payer",rs.getString("Payer"));
                customerDataMap.put("PriceList",rs.getString("PriceList"));
                customerDataMap.put("SalesOrganization",rs.getString("SalesOrganization"));
                customerDataMap.put("ShowPricing",rs.getString("ShowPricing"));
                if (System.getenv("PROVIDER") == "tambour") {
                    customerDataMap.put("TAXK1", rs.getString("TAXK1"));
                    customerDataMap.put("TanaiHovala", rs.getString("TanaiHovala"));
                }
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
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 118");
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
    }
    */
    public void UpdateItemTablesSelection(String itemCode)
    {
        if (System.getenv("PROVIDER").equalsIgnoreCase("noa")) {
            updateItemSelection(itemCode, Tables.TABLE_ITEMS);
        }
        else
        {
            updateItemSelection(itemCode, Tables.TABLE_ITEMS_PRICING);
        }
    }
    public void updateItemSelection(String itemCode, String tableName) {
        ArrayList<MtnMappingData.FieldMapData> fieldMappingData =  MtnMappingData.getInstance().getMtnMappingData().get(tableName);
        if (fieldMappingData == null) {
            return;
        }

        HashMap<String, String> itemsDataMap = null;
        itemsDataMap = itemsAllDataMap.get(itemCode);
        if (itemsDataMap == null) {
            itemsDataMap = new HashMap<String, String>();
            ResultSet rs = null;
            Statement st = null;
            Connection conn = null;

            String query = null;
            query = "SELECT * " + " FROM " + Tables.GetFullTableName(tableName) + " WHERE ItemID=" + itemCode;

            try {
                conn = DbUtil.connect(conn);


                st = conn.createStatement();
                LogUtil.LOG.info(query);

                // execute the query, and get a java resultset
                rs = st.executeQuery(query);
                if (rs.next()) {
                    itemsDataMap.put("ItemID", rs.getString("ItemID"));
                    for (MtnMappingData.FieldMapData fieldMapData : fieldMappingData) {
                        String fieldValue = rs.getString(fieldMapData.MTNFied);
                        fieldValue = fieldValue != null ? fieldValue.trim() : fieldValue;
                        itemsDataMap.put(fieldMapData.MTNFied, fieldValue);
                    }
                    itemsDataMap.put("UnitInCart", rs.getString("UnitInCart"));
                /*
                itemsDataMap.put("ItemVatType",rs.getString("ItemVatType"));
                if (System.getenv("PROVIDER").equalsIgnoreCase("tambour")) {
                    itemsDataMap.put("ProdHierarchy4", rs.getString("ProdHierarchy4"));
                    itemsDataMap.put("TAXM1", rs.getString("TAXM1"));
                    itemsDataMap.put("SaleUnit", rs.getString("SaleUnit"));
                }
                if (System.getenv("PROVIDER").equalsIgnoreCase( "strauss")) {
                    itemsDataMap.put("PCBarcode", rs.getString("PCBarcode"));
                    itemsDataMap.put("MaterialType", rs.getString("MaterialType"));
                    itemsDataMap.put("LeadProd", rs.getString("LeadProd"));
                    itemsDataMap.put("ProdHierarchy4", rs.getString("ProdHierarchy4"));
                    itemsDataMap.put("ProdHierarchy1", rs.getString("ProdHierarchy1"));
                    itemsDataMap.put("ProdHierarchy2", rs.getString("ProdHierarchy2"));
                    itemsDataMap.put("ProdHierarchy3", rs.getString("ProdHierarchy3"));
                    itemsDataMap.put("ItemDivision", rs.getString("ItemDivision"));
                    itemsDataMap.put("TaxCategory", rs.getString("TaxCategory"));
                    itemsDataMap.put("DepositType", rs.getString("DepositType"));
                    itemsDataMap.put("OperationalCategory", rs.getString("OperationalCategory"));
                    itemsDataMap.put("KashrutPassOV", rs.getString("KashrutPassOV"));
                }
                */
                }

                if (itemsDataMap == null) {
                    return;
                }
                itemsAllDataMap.put(itemCode, itemsDataMap);
            }
            catch (SQLException e) {
                LogUtil.LOG.error("Error in line: " + e.getStackTrace()[0].getLineNumber() + ", Error Message:" + e.getMessage() + "119");
            } finally {
                DbUtil.CloseConnection(conn, rs, st);
            }

            /*HashMap<String, ArrayList<MtnMappingData.FieldMapData>> mtnMappingData = MtnMappingData.getInstance().getMtnMappingData();
            ArrayList<MtnMappingData.FieldMapData> itemsFieldMapDatas = null;
            if (System.getenv("PROVIDER") == "tambour")
                itemsFieldMapDatas = mtnMappingData.get(TABLE_ITEMS_TAMBOUR);
            else
                itemsFieldMapDatas = mtnMappingData.get(TABLE_ITEMS);

            if (itemsFieldMapDatas == null) {
                return;
            }
            /*
             */
        }

        for (MtnMappingData.FieldMapData fieldMapData : fieldMappingData) {
               activeSelectionMap.put(fieldMapData.MTNFied, itemsDataMap.get(fieldMapData.MTNFied));
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

    public  String getItemValue(String ItemID, String key)
    {
        String returnValue = null;
        HashMap<String, String> itemDataMap = itemsAllDataMap.get(ItemID);
        if (itemDataMap != null)
            returnValue = itemsAllDataMap.get(ItemID).get(key);
        return  returnValue;
    }

    public Set<String> getAllItemCodes(){
        return  itemsAllDataMap.keySet();
    }
}
