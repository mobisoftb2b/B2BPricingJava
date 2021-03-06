package com.mobisale.singleton;

import com.mobisale.data.AccessSequenceData;
import com.mobisale.data.ConditionReturnData;
import com.mobisale.data.ItemPricingData;
import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;
import com.mobisale.utils.SqlLiteUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class PricingExistsTablesData {

    private ConcurrentHashMap<String, Boolean> pricingExistsTablesMap = new ConcurrentHashMap<String, Boolean>();
    private static PricingExistsTablesData m_instance = null;

    SqlLiteUtil sqlLiteUtil = new SqlLiteUtil();

    public static PricingExistsTablesData getInstance() {
        if (m_instance == null) {
            m_instance = new PricingExistsTablesData();
        }
        // Return the instance
        return m_instance;
    }

    private PricingExistsTablesData() {
    }

    public void clearResources(){
        pricingExistsTablesMap.clear();
    }

    public void executeQuerySqlLite(){
        if (sqlLiteUtil.IsSQlLite()) {
            ResultSet rs = null;
            Statement st = null;
            Connection conn = null;
            try {
                if (sqlLiteUtil.IsSQlLite())
                    conn = sqlLiteUtil.Connect();

                String query = "SELECT  name  FROM  sqlite_master  WHERE  type ='table' AND name NOT LIKE 'sqlite_%'";

                st = conn.createStatement();
                LogUtil.LOG.info(query);
                rs = st.executeQuery(query);

                if (rs == null) {
                    conn.close();
                    return;
                }
                while (rs.next()) {
                    String tableName = rs.getString(rs.findColumn("name"));
                    pricingExistsTablesMap.put(tableName, true);
                }
            } catch (SQLException e) {

                LogUtil.LOG.error("Error 1067 in line: " + e.getStackTrace()[0].getLineNumber() + ", Error Message:" + e.getMessage());
            } finally {
                if (sqlLiteUtil.IsSQlLite())
                    sqlLiteUtil.Disconnect(conn);
            }

        }
    }
    public void executeQuery()     {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            conn = DbUtil.connect(conn);
            String query ="SELECT TABLE_NAME\n" +
                    "FROM INFORMATION_SCHEMA.TABLES\n" +
                    "WHERE TABLE_TYPE = 'BASE TABLE'";
            LogUtil.LOG.info(query);
            st = conn.createStatement();
            LogUtil.LOG.info(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return;
            }
            while (rs.next()) {

                String tableName = rs.getString(rs.findColumn("TABLE_NAME"));
                pricingExistsTablesMap.put(tableName, true);
            }
        } catch (SQLException e) {
            LogUtil.LOG.error("Error 1068 in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:" + e.getMessage());
        } finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
   }

    public boolean isTableExist(String tableName) {
        return pricingExistsTablesMap.get(tableName) != null;
    }
}
