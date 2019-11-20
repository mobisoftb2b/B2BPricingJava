package com.mobisale.singleton;

import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class PricingExistsTablesData {

    private HashMap<String, Boolean> pricingExistsTablesMap = new HashMap<String, Boolean>();
    private static PricingExistsTablesData m_instance = null;


    public static PricingExistsTablesData getInstance() {
        if (m_instance == null) {
            m_instance = new PricingExistsTablesData();
        }
        // Return the instance
        return m_instance;
    }

    private PricingExistsTablesData() {
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
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 120");
        } finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
   }

    public boolean isTableExist(String tableName) {
        return pricingExistsTablesMap.get(tableName) != null;
    }
}
