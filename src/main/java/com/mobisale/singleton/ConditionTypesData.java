package com.mobisale.singleton;


import com.mobisale.columns.ConditionTypes;
import com.mobisale.constants.Tables;
import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;
import com.mobisale.utils.SqlLiteUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConditionTypesData {

    private static final String TAG = "ConditionTypesData";
    private ConcurrentHashMap<String, ConditionTypeData> conditionTypesMap = new ConcurrentHashMap<String, ConditionTypeData>();
    private static ConditionTypesData m_instance = null;
    private SqlLiteUtil sqlLiteUtil = new SqlLiteUtil();


    public static ConditionTypesData getInstance() {
        if (m_instance == null) {
            m_instance = new ConditionTypesData();
        }
        // Return the instance
        return m_instance;
    }

    private ConditionTypesData() {
    }

    public void clearResources(){
        conditionTypesMap.clear();
    }

    public synchronized void executeQuery() {
        //conditionTypesMap.clear();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {

            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.Connect();
            else
                conn = DbUtil.connect(conn);

            String query ="SELECT * FROM " + Tables.TABLE_PRICING_CONDITION_TYPES;
            // create the java statement
            st = conn.createStatement();
            LogUtil.LOG.info(query);
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
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 121");
        } finally {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                sqlLiteUtil.Disconnect(conn);
            else
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
