package com.mtn.mobisale.singleton;;

import com.mtn.mobisale.utils.DbUtil;
import com.mtn.mobisale.utils.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


public class MtnMappingData {

    private HashMap<String, ArrayList<FieldMapData>> mtnMappingData = new HashMap<String, ArrayList<FieldMapData>>();
    private HashMap<String, String> sapToMtnData = new HashMap<String, String>();
    private static MtnMappingData m_instance = null;


    public static MtnMappingData getInstance() {
        if (m_instance == null) {
            m_instance = new MtnMappingData();
        }
        // Return the instance
        return m_instance;
    }

    private MtnMappingData() {
    }

    public void executeQuery() {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {

            mtnMappingData.clear();
            sapToMtnData.clear();
            conn = DbUtil.connect(conn);

            String query = "SELECT * FROM " + "MTN_Mapping" + " ORDER BY " + "TableName";
            LogUtil.LOG.error(query);
            // create the java statement
            st = conn.createStatement();

            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return;
            }
            while (rs.next()) {
                String sapField = rs.getString("SAPField");  //cursor.getString(cursor.getColumnIndex(PricingContract.MtnMapping.MTN_MAPPING_SAP_FIELD));
                String mtnField = rs.getString("MTNField");   //cursor.getString(cursor.getColumnIndex(PricingContract.MtnMapping.MTN_MAPPING_MTN_FIELD));
                String tableName = rs.getString("TABLEName");   //cursor.getString(cursor.getColumnIndex(PricingContract.MtnMapping.MTN_MAPPING_TABLE_NAME));
                String fieldType = rs.getString("FieldType");   //cursor.getString(cursor.getColumnIndex(PricingContract.MtnMapping.MTN_MAPPING_FIELD_TYPE));
                FieldMapData fieldMapData = new FieldMapData(sapField, mtnField, tableName, fieldType);
                ArrayList<FieldMapData> mtnMapDatas = mtnMappingData.get(tableName);
                if (mtnMapDatas == null) {
                    mtnMapDatas = new ArrayList<FieldMapData>();
                }
                mtnMapDatas.add(fieldMapData);
                mtnMappingData.put(tableName, mtnMapDatas);
                sapToMtnData.put(sapField, mtnField);
            }
        } catch (SQLException e) {
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        } finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
    }

    public HashMap<String, ArrayList<FieldMapData>> getMtnMappingData() {
        return mtnMappingData;
    }

    public String getMtnField(String sapFiled) {
        String mtnField = sapToMtnData.get(sapFiled);
        return mtnField == null ? "" : mtnField;
    }

    public class FieldMapData {
        public final String SAPField;
        public final String MTNFied;
        public final String TableName;
        public final String FieldType;

        FieldMapData(String SAPField, String MTNFied, String tableName, String fieldType) {
            this.SAPField = SAPField;
            this.MTNFied = MTNFied;
            TableName = tableName;
            FieldType = fieldType;
        }
    }
}
