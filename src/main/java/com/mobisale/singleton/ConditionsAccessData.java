package com.mobisale.singleton;


import com.mobisale.columns.ConditionsAccess;
import com.mobisale.constants.Tables;
import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;
import com.mobisale.utils.SqlLiteUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class ConditionsAccessData {

    private HashMap<String, ConditionAccessData> conditionsAccessMap = new HashMap<String, ConditionAccessData>();
    private static ConditionsAccessData m_instance = null;
    private SqlLiteUtil sqlLiteUtil = new SqlLiteUtil();


    public static ConditionsAccessData getInstance() {
        if (m_instance == null) {
            m_instance = new ConditionsAccessData();
        }
        // Return the instance
        return m_instance;
    }

    private ConditionsAccessData() {
    }

    public void executeQuery() {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        conditionsAccessMap.clear();
        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.Connect();
            else
                conn = DbUtil.connect(conn);
            String query ="SELECT DISTINCT " + ConditionsAccess.CONDITION_ACCESS_TYPE + "," + ConditionsAccess.CONDITION_ACCESS_SEQUENCE + "," + ConditionsAccess.CONDITION_REF_COND
                    + " FROM " + Tables.TABLE_PRICING_CONDITIONS_ACCESS + " ORDER BY " + ConditionsAccess.CONDITION_ACCESS_TYPE + " ASC";

            st = conn.createStatement();
            LogUtil.LOG.info(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return;
            }
            while (rs.next()) {


                String ConditionType = rs.getString(ConditionsAccess.CONDITION_ACCESS_TYPE);
                String AccessSequence = rs.getString(ConditionsAccess.CONDITION_ACCESS_SEQUENCE);
                String RefCond = rs.getString(ConditionsAccess.CONDITION_REF_COND);
                if (AccessSequence == null || AccessSequence.isEmpty()) {
                    AccessSequence = ConditionType;
                }
                if (RefCond == null || RefCond.isEmpty()) {
                    RefCond = ConditionType;
                }
                ConditionAccessData conditionAccessData = new ConditionAccessData(AccessSequence, RefCond);
                conditionsAccessMap.put(ConditionType, conditionAccessData);
            }
        } catch (SQLException e) {
            LogUtil.LOG.error("Error :"+e.getMessage());
        } finally {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                sqlLiteUtil.Disconnect(conn);
            else
                DbUtil.CloseConnection(conn,rs,st);
        }
    }

    public String getAccessSequence(String conditionType) {
        String accessSequence = conditionType;
        try {
            if (conditionsAccessMap.containsKey(conditionType))
                accessSequence = conditionsAccessMap.get(conditionType).AccessSequence;
        } catch (Exception e) {
            System.out.println("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 122");
        }
        return accessSequence == null ? "" : accessSequence;
    }

    public String getRefCond(String conditionType) {
        String refCond = conditionType;
        try {
            refCond = conditionsAccessMap.get(conditionType).RefCond;
        } catch (Exception e) {
            System.out.println("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + "123");
        }
        return refCond == null ? conditionType : refCond;
    }

    class ConditionAccessData {
        public final String AccessSequence;
        public final String RefCond;

        public ConditionAccessData(String accessSequence, String refCond) {
            AccessSequence = accessSequence;
            RefCond = refCond;
        }
    }

}
