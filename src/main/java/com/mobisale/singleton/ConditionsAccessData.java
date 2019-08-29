package com.mtn.mobisale.singleton;


import com.mtn.mobisale.columns.ConditionsAccess;
import com.mtn.mobisale.constants.Tables;
import com.mtn.mobisale.utils.DbUtil;
import com.mtn.mobisale.utils.LogUtil;

import java.sql.*;
import java.util.HashMap;

public class ConditionsAccessData {

    private HashMap<String, ConditionAccessData> conditionsAccessMap = new HashMap<String, ConditionAccessData>();
    private static ConditionsAccessData m_instance = null;


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
            conn = DbUtil.connect(conn);
            String query ="SELECT DISTINCT " + ConditionsAccess.CONDITION_ACCESS_TYPE + "," + ConditionsAccess.CONDITION_ACCESS_SEQUENCE + "," + ConditionsAccess.CONDITION_REF_COND
                    + " FROM " + Tables.TABLE_PRICING_CONDITIONS_ACCESS + " ORDER BY " + ConditionsAccess.CONDITION_ACCESS_TYPE + " ASC";

            st = conn.createStatement();
            LogUtil.LOG.error(query);
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
            DbUtil.CloseConnection(conn,rs,st);
        }
    }

    public String getAccessSequence(String conditionType) {
        String accessSequence = conditionType;
        try {
            accessSequence = conditionsAccessMap.get(conditionType).AccessSequence;
        } catch (Exception e) {
            System.out.println("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        }
        return accessSequence == null ? "" : accessSequence;
    }

    public String getRefCond(String conditionType) {
        String refCond = conditionType;
        try {
            refCond = conditionsAccessMap.get(conditionType).RefCond;
        } catch (Exception e) {
            System.out.println("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
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
