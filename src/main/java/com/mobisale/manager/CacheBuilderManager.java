package com.mobisale.manager;

import com.mobisale.constants.Tables;
import com.mobisale.data.CustomerCache;
import com.mobisale.data.CustomerCache;
import com.mobisale.data.ItemMigvan;
import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.*;
import java.util.Set;

public class CacheBuilderManager {

    public ArrayList<CustomerCache> getCustomersForCache(){
        ResultSet rs = null;
        CallableStatement st = null;
        Connection conn = null;
        ArrayList<CustomerCache> customers = new ArrayList<>();
        try {
            conn = DbUtil.connect(conn);
            st = conn.prepareCall("{call sp_B2B_GetCustomers_ForCache ()}");
            st.execute();
            rs = st.getResultSet();

            if (rs == null) {
                return customers;
            }
            while (rs.next()) {
                CustomerCache cust = new CustomerCache();
                cust.CustID = rs.getString("CustID");
                cust.Cust_Key = rs.getString("Cust_Key");
                cust.CompanyID = Integer.parseInt(cust.Cust_Key.substring(0, 4));
                customers.add(cust);
            }
        }
        catch (Exception e) {
            LogUtil.LOG.error("Error :"+e.getMessage());
            // TODO: 2019-07-31 add log
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
        return customers;
    }

    public void forceRepricingOrderSummary(){
        CallableStatement st = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.connect(conn);
            st = conn.prepareCall("{call sp_ForceRepricingOnDBChange}");
            st.executeUpdate();
        }
        catch (Exception e) {
            LogUtil.LOG.error("Error :"+e.getMessage());
            // TODO: 2019-07-31 add log
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
    }

    public ArrayList<ItemMigvan> getItemsForCache(CustomerCache cust){
        ResultSet rs = null;
        CallableStatement st = null;
        Connection conn = null;
        ArrayList<ItemMigvan> items = new ArrayList<>();;
        try {
            conn = DbUtil.connect(conn);
            st = conn.prepareCall("{call B2B_GetActiveOrder_ForCache (?, ?, ?)}");
            st.setString(1, cust.CustID);
            st.setInt(2, cust.CompanyID);
            st.setString(3, cust.Cust_Key);
            st.execute();
            rs = st.getResultSet();

            if (rs == null) {
                return items;
            }
            while (rs.next()) {
                String itemID = rs.getString("ItemCode");
                String showCart = rs.getString("ShowCart");
                String showUnit = rs.getString("ShowUnit");
                ItemMigvan item = new ItemMigvan();
                item.ItemID = itemID;
                item.ShowCart = Boolean.parseBoolean(showCart);
                item.ShowUnit = Boolean.parseBoolean(showUnit);
                items.add(item);
            }
        }
        catch (Exception e) {
            LogUtil.LOG.error("Error :"+e.getMessage());
            // TODO: 2019-07-31 add log
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
        return items;
    }

    public ArrayList<ItemMigvan> getItemsTagsForCache(CustomerCache cust){
        ResultSet rs = null;
        CallableStatement st = null;
        Connection conn = null;
        ArrayList<ItemMigvan> items = new ArrayList<>();;
        try {
            conn = DbUtil.connect(conn);
            //st = conn.prepareCall("{call B2B_ItemsTags_Select_ForCache (?, ?, ?, ?, ?)}");
            st = conn.prepareCall("{call B2B_ActiveOrder_ForCache (?, ?, ?, ?, ?)}");
            st.setInt(1, cust.CompanyID);
            st.setString(2, cust.CustID);
            st.setInt(3, 100);
            st.setInt(4, 0);
            st.setInt(5, 0);
            st.execute();
            rs = st.getResultSet();

            if (rs == null) {
                return items;
            }
            while (rs.next()) {
                String itemID = rs.getString("ItemCode");
                String showCart = rs.getString("ShowCart");
                String showUnit = rs.getString("ShowUnit");
                ItemMigvan item = new ItemMigvan();
                item.ItemID = itemID;
                item.ShowCart = Boolean.parseBoolean(showCart);
                item.ShowUnit = Boolean.parseBoolean(showUnit);
                items.add(item);
            }
        }
        catch (Exception e) {
            LogUtil.LOG.error("Error :"+e.getMessage());
            // TODO: 2019-07-31 add log
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
        return items;
    }

    //call [dbo].[B2B_ItemsTags_Select_ForCache] 1100, '10091257', 100, 0,0
    //[dbo].[sp_B2B_GetCustomers_ForCache]
    public ArrayList<ItemMigvan> getItemsCodesMigvan(String cust_Key) {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        ArrayList<ItemMigvan> items = new ArrayList<>();;
        String query;
        try{
                query = "SELECT top 20 ItemID, ShowCart, ShowUnit FROM B2B_Migvan WHERE CustKey='" + cust_Key + "' and ItemID in (10896, 14525, 97311)";

                conn = DbUtil.connect(conn);
                st = conn.createStatement();

                LogUtil.LOG.error(query);

                rs = st.executeQuery(query);
                if (rs == null) {
                        return null;
                }
                while (rs.next()) {
                    String itemID = rs.getString("ItemID");
                    String showCart = rs.getString("ShowCart");
                    String showUnit = rs.getString("ShowUnit");
                    ItemMigvan item = new ItemMigvan();
                    item.ItemID = itemID;
                    item.ShowCart = Boolean.parseBoolean(showCart);
                    item.ShowUnit = Boolean.parseBoolean(showUnit);
                    items.add(item);
                }
        }
       catch (Exception e) {
                LogUtil.LOG.error("Error :"+e.getMessage());
                // TODO: 2019-07-31 add log
       }
       finally {
                DbUtil.CloseConnection(conn,rs,st);
       }

        return items;
    }
}
