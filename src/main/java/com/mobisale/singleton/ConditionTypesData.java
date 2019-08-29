package com.mtn.mobisale.singleton;


import com.mtn.mobisale.columns.ConditionTypes;
import com.mtn.mobisale.constants.Tables;
import com.mtn.mobisale.utils.DbUtil;
import com.mtn.mobisale.utils.LogUtil;

import java.sql.*;
import java.util.HashMap;

public class ConditionTypesData {

    private static final String TAG = "ConditionTypesData";
    private HashMap<String, ConditionTypeData> conditionTypesMap = new HashMap<String, ConditionTypeData>();
    private static ConditionTypesData m_instance = null;


    public static ConditionTypesData getInstance() {
        if (m_instance == null) {
            m_instance = new ConditionTypesData();
        }
        // Return the instance
        return m_instance;
    }

    private ConditionTypesData() {
    }

    public void executeQuery() {
        //conditionTypesMap.clear();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            conn = DbUtil.connect(conn);

            String query ="SELECT * FROM " + Tables.TABLE_PRICING_CONDITION_TYPES;
            // create the java statement
            st = conn.createStatement();
            LogUtil.LOG.error(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return;
            }

            while (rs.next()) {
                String Condition = rs.getString(ConditionTypes.CONDITION_TYPES_CONDITION);
                int ConditionType = Integer.parseInt(rs.getString(ConditionTypes.CONDITION_TYPES_CONDITION_TYPE));
                String Comment =  "";//todo rs.getString(ConditionTypes.CONDITION_TYPES_COMMENT);
                conditionTypesMap.put(Condition, new ConditionTypeData(Condition, ConditionType, Comment));
            }
        } catch (SQLException e) {
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        } finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
    }

    public ConditionTypeData getConditionType(String condition) {
        ConditionTypeData conditionTypeData = null;
        if (conditionTypesMap.get(condition) != null) {
            conditionTypeData = conditionTypesMap.get(condition);
        }
        return conditionTypeData;
    }

    public class ConditionTypeData{
        public final String Condition;
        public final int ConditionType;
        public final String Comment;

        public ConditionTypeData(String condition, int conditionType, String comment) {
            Condition = condition;
            ConditionType = conditionType;
            Comment = comment;
        }
    }
}
