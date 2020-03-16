package com.mobisale.singleton;

import com.mobisale.columns.PricingCondition;
import com.mobisale.columns.PricingSequence;
import com.mobisale.constants.Tables;
import com.mobisale.data.*;
import com.mobisale.data.ItemPricingData;
import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;
import com.mobisale.utils.SqlLiteUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class PricingSequenceData {

    private TreeMap<String, AccessSequencesData> accessSequencesDataMap = new TreeMap<String, AccessSequencesData>();
    private static PricingSequenceData m_instance = null;
    private SqlLiteUtil sqlLiteUtil = new SqlLiteUtil();

    public static PricingSequenceData getInstance() {
        if (m_instance == null) {
            m_instance = new PricingSequenceData();
        }
        // Return the instance
        return m_instance;
    }

    private PricingSequenceData() {
    }

    public void clearResources(){
        accessSequencesDataMap.clear();
    }
    public void executeQuery1() {
        ResultSet rs = null;
        Statement st = null;
        Statement conditionST = null;
        Connection conn = null;
        Connection conditionConn = null;

        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.Connect();
            else
                conn = DbUtil.connect(conn);

            String query = "SELECT DISTINCT " + PricingSequence.PRICING_ACCESS_SEQUENCE + "," +
                    PricingSequence.PRICING_ACCESS + "," + PricingSequence.PRICING_TABLE_NAME +
                    "," + PricingSequence.PRICING_EXCLUSIVE_ACCESS + "," + PricingSequence.PRICING_REQUIREMENT +
                    " FROM " + Tables.TABLE_PRICING_SEQUENCE + " ORDER BY " + PricingSequence.PRICING_ACCESS_SEQUENCE +
                    "," + PricingSequence.PRICING_ACCESS + " ASC";

            st = conn.createStatement();
            LogUtil.LOG.info(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return;
            }
            while (rs.next()) {
                String AccessSequence = rs.getString(PricingSequence.PRICING_ACCESS_SEQUENCE);
                int Access = Integer.parseInt(rs.getString(PricingSequence.PRICING_ACCESS));
                String TableName = rs.getString(PricingSequence.PRICING_TABLE_NAME);
                String ExclusiveAccess = rs.getString(PricingSequence.PRICING_EXCLUSIVE_ACCESS);
                int Requirement = Integer.parseInt(rs.getString(PricingSequence.PRICING_REQUIREMENT));

                AccessSequenceData accessSequenceData = new AccessSequenceData(AccessSequenceData.ACCESS_SEQUENCE_TYPE_PRICING, AccessSequence, Access, TableName,
                        ExclusiveAccess != null && ExclusiveAccess.equalsIgnoreCase("X"), Requirement);
                AccessSequencesData accessSequencesData = accessSequencesDataMap.get(AccessSequence);
                if (accessSequencesData == null) {
                    accessSequencesData = new AccessSequencesData(AccessSequence);
                }
                accessSequencesData.addAccessSequencesData(accessSequenceData);
                accessSequencesDataMap.put(AccessSequence, accessSequencesData);
                //
                ResultSet conditionCursor = null;
                String conditionQuery = "SELECT " + PricingCondition.PRICING_CONDITION_FIELD + "," +
                        PricingCondition.PRICING_CONDITION_DOC_STRUCT + "," +
                        PricingCondition.PRICING_CONDITION_ACTIVE_FIELD + " FROM " +
                        Tables.TABLE_PRICING_CONDITION + " WHERE " + PricingCondition.PRICING_CONDITION_ACCESS_SEQUENCE +
                        "=" + "'" + AccessSequence + "'" + " AND " + PricingCondition.PRICING_CONDITION_ACCESS + "=" + Access + " AND DocField IS NOT NULL";
                try {
                    if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                        conditionConn = sqlLiteUtil.Connect();
                    else
                        conditionConn = DbUtil.connect(conn);
                    conditionST = conditionConn.createStatement();
                    LogUtil.LOG.info(conditionQuery);
                    conditionCursor = conditionST.executeQuery(conditionQuery);
                    if (conditionCursor == null) {
                        continue;
                    }
                    while (conditionCursor.next()) {
                        String ConditionField = conditionCursor.getString(PricingCondition.PRICING_CONDITION_FIELD);//conditionCursor.getString(conditionCursor.getColumnIndex(PricingContract.PricingCondition.PRICING_CONDITION_FIELD));
                        String DocStruct = conditionCursor.getString(PricingCondition.PRICING_CONDITION_DOC_STRUCT);//conditionCursor.getString(conditionCursor.getColumnIndex(PricingContract.PricingCondition.PRICING_CONDITION_DOC_STRUCT));
                        String ActiveField = conditionCursor.getString(PricingCondition.PRICING_CONDITION_ACTIVE_FIELD);//conditionCursor.getString(conditionCursor.getColumnIndex(PricingContract.PricingCondition.PRICING_CONDITION_ACTIVE_FIELD));
                        ConditionData conditionData = new ConditionData(ConditionField, DocStruct, ActiveField);
                        accessSequenceData.addConditionData(conditionData);
                    }
                }
                catch (SQLException e) {
                    LogUtil.LOG.error("Error101 :"+e.getMessage());
                    //System.out.println(e.getStackTrace()[0].getLineNumber());
                }
                finally {
                    if (conditionCursor != null) {
                        conditionCursor.close();
                    }
                    if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                        sqlLiteUtil.Disconnect(conditionConn);
                    else
                        DbUtil.CloseConnection(conditionConn,conditionCursor,conditionST);

                    accessSequenceData.buildSequenceData();
                }

            }
        } catch (SQLException e) {
            LogUtil.LOG.error("Error102 :"+e.getMessage());
            //System.out.println(e.getStackTrace()[0].getLineNumber());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { LogUtil.LOG.error("Error103 :"+e.getMessage());}
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {  LogUtil.LOG.error("Error104 :"+e.getMessage());}
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {  LogUtil.LOG.error("Error105 :"+e.getMessage());}
            }
            //if (sqlLiteUtil.IsSQlLite())
            //    sqlLiteUtil.Disconnect(conn);
            //else
             //   DbUtil.CloseConnection(conn,rs,st);

        }
    }
    public void queryConditionData(String AccessSequence, int Access,  AccessSequenceData accessSequenceData){
        Statement conditionST = null;
        Connection conn = null;
        Connection conditionConn = null;

        ResultSet conditionCursor = null;
        String conditionQuery = "SELECT " + PricingCondition.PRICING_CONDITION_FIELD + "," +
                PricingCondition.PRICING_CONDITION_DOC_STRUCT + "," +
                PricingCondition.PRICING_CONDITION_ACTIVE_FIELD + " FROM " +
                Tables.TABLE_PRICING_CONDITION + " WHERE " + PricingCondition.PRICING_CONDITION_ACCESS_SEQUENCE +
                "=" + "'" + AccessSequence + "'" + " AND " + PricingCondition.PRICING_CONDITION_ACCESS + "=" + Access + " AND DocField IS NOT NULL";

        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conditionConn = sqlLiteUtil.Connect();
            else
                conditionConn = DbUtil.connect(conn);
            conditionST = conditionConn.createStatement();
            LogUtil.LOG.info(conditionQuery);
            conditionCursor = conditionST.executeQuery(conditionQuery);
            if (conditionCursor != null) {
                while (conditionCursor.next()) {
                    String ConditionField = conditionCursor.getString(PricingCondition.PRICING_CONDITION_FIELD);//conditionCursor.getString(conditionCursor.getColumnIndex(PricingContract.PricingCondition.PRICING_CONDITION_FIELD));
                    String DocStruct = conditionCursor.getString(PricingCondition.PRICING_CONDITION_DOC_STRUCT);//conditionCursor.getString(conditionCursor.getColumnIndex(PricingContract.PricingCondition.PRICING_CONDITION_DOC_STRUCT));
                    String ActiveField = conditionCursor.getString(PricingCondition.PRICING_CONDITION_ACTIVE_FIELD);//conditionCursor.getString(conditionCursor.getColumnIndex(PricingContract.PricingCondition.PRICING_CONDITION_ACTIVE_FIELD));
                    ConditionData conditionData = new ConditionData(ConditionField, DocStruct, ActiveField);
                    accessSequenceData.addConditionData(conditionData);
                }
            }
        }
        catch (SQLException e) {
            LogUtil.LOG.error("Error101 :"+e.getMessage());
            //System.out.println(e.getStackTrace()[0].getLineNumber());
        }
        finally {
            if (conditionCursor != null) {
                try {
                    conditionCursor.close();
                } catch (SQLException e) { LogUtil.LOG.error("Error103-1 :"+e.getMessage());}
            }

            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                sqlLiteUtil.Disconnect(conditionConn);
            else
                DbUtil.CloseConnection(conditionConn,conditionCursor,conditionST);

        }

    }

    public void executeQuery() {
        ResultSet rs = null;
        Statement st = null;
        Statement conditionST = null;
        Connection conn = null;
        Connection conditionConn = null;

        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.Connect();
            else
                conn = DbUtil.connect(conn);

            String query = "SELECT DISTINCT " + PricingSequence.PRICING_ACCESS_SEQUENCE + "," +
                    PricingSequence.PRICING_ACCESS + "," + PricingSequence.PRICING_TABLE_NAME +
                    "," + PricingSequence.PRICING_EXCLUSIVE_ACCESS + "," + PricingSequence.PRICING_REQUIREMENT +
                    " FROM " + Tables.TABLE_PRICING_SEQUENCE + " ORDER BY " + PricingSequence.PRICING_ACCESS_SEQUENCE +
                    "," + PricingSequence.PRICING_ACCESS + " ASC";

            st = conn.createStatement();
            LogUtil.LOG.info(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return;
            }
            while (rs.next()) {
                String AccessSequence = rs.getString(PricingSequence.PRICING_ACCESS_SEQUENCE);
                int Access = Integer.parseInt(rs.getString(PricingSequence.PRICING_ACCESS));
                String TableName = rs.getString(PricingSequence.PRICING_TABLE_NAME);
                String ExclusiveAccess = rs.getString(PricingSequence.PRICING_EXCLUSIVE_ACCESS);
                int Requirement = Integer.parseInt(rs.getString(PricingSequence.PRICING_REQUIREMENT));

                AccessSequenceData accessSequenceData = new AccessSequenceData(AccessSequenceData.ACCESS_SEQUENCE_TYPE_PRICING, AccessSequence, Access, TableName,
                        ExclusiveAccess != null && ExclusiveAccess.equalsIgnoreCase("X"), Requirement);
                AccessSequencesData accessSequencesData = accessSequencesDataMap.get(AccessSequence);
                if (accessSequencesData == null) {
                    accessSequencesData = new AccessSequencesData(AccessSequence);
                }
                accessSequencesData.addAccessSequencesData(accessSequenceData);
                accessSequencesDataMap.put(AccessSequence, accessSequencesData);
                //
                queryConditionData(AccessSequence, Access,  accessSequenceData);

                accessSequenceData.buildSequenceData();
                accessSequencesDataMap.put(AccessSequence, accessSequencesData);
            }
        } catch (SQLException e) {
            LogUtil.LOG.error("Error102 :"+e.getMessage());
            //System.out.println(e.getStackTrace()[0].getLineNumber());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { LogUtil.LOG.error("Error103 :"+e.getMessage());}
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {  LogUtil.LOG.error("Error104 :"+e.getMessage());}
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {  LogUtil.LOG.error("Error105 :"+e.getMessage());}
            }
            //if (sqlLiteUtil.IsSQlLite())
            //    sqlLiteUtil.Disconnect(conn);
            //else
            //   DbUtil.CloseConnection(conn,rs,st);

        }
    }

    public void startAccessSequence() {
        ArrayList<ConditionReturnListData> conditionReturnListDatas = new ArrayList<ConditionReturnListData>();
        long startTime = System.currentTimeMillis();
        LogUtil.LOG.info("start time for get AccessSequence Data is " + startTime);
        int i = 1;
        for (AccessSequencesData accessSequencesData : accessSequencesDataMap.values()) {
            LogUtil.LOG.info("AccessSequence is " + accessSequencesData.AccessSequence + " and total number is " + i);
            i++;
            LogUtil.LOG.info("start access Sequence -----------------------------------------------");
            ConditionReturnListData conditionReturnListData = getAccessSequenceData(accessSequencesData.AccessSequence, accessSequencesData.AccessSequence, false);
            if (conditionReturnListData != null) {
                conditionReturnListDatas.add(conditionReturnListData);
            }
            LogUtil.LOG.info("end access Sequence -----------------------------------------------");
        }
        long endTime = System.currentTimeMillis();
        long diff = endTime - startTime;
        double v = (double) diff / 1000;

        LogUtil.LOG.info("total time for get AccessSequence Data is " + v + " seconds");
    }

    public ConditionReturnListData getAccessSequenceData(String conditionType, String accessSequence, boolean isManual) {
        ConditionReturnListData conditionReturnListData = new ConditionReturnListData();
        AccessSequencesData accessSequencesData = accessSequencesDataMap.get(accessSequence);
        if (accessSequencesData == null) {
            return conditionReturnListData;
        }
        Set<Map.Entry<Integer, AccessSequenceData>> entrySet = accessSequencesData.accessSequenceDataTreeMap.entrySet();
        for (Map.Entry<Integer, AccessSequenceData> next : entrySet) {
            AccessSequenceData accessSequenceData = next.getValue();
            LogUtil.LOG.info("access=" + accessSequenceData.Access);

            if (!PricingExistsTablesData.getInstance().isTableExist(accessSequenceData.TableName)) {
                continue;
            }
            String[] selectionArgs = new String[accessSequenceData.selectionArgs.length];
            selectionArgs[0] = ConditionsAccessData.getInstance().getRefCond(conditionType);
            for (int i = 1; i < accessSequenceData.selectionArgs.length; i++) {
                String selectedKey = accessSequenceData.selectionArgs[i];
                selectionArgs[i] = ActiveSelectionData.getInstance().getValue(selectedKey);
            }
            ConditionReturnData conditionReturnData = executeQuery(accessSequenceData, selectionArgs, isManual);
            if (conditionReturnData == null) {
                continue;
            } else {
                conditionReturnListData.addConditionReturnData(conditionReturnData);
                if (accessSequenceData.ExclusiveAccess)
                    break;
            }
        }
        return conditionReturnListData;
    }

    private String[] badPatterns = {"KUNNR='0'"};

    private ConditionReturnData executeQuery(AccessSequenceData accessSequenceData, String[] selectionArgs, boolean isManual) {
        ConditionReturnData conditionReturnData = null;
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;


        try {
            if (sqlLiteUtil.IsSQlLite())
                conn = sqlLiteUtil.Connect();
            else
                conn = DbUtil.connect(conn);

            String query = accessSequenceData.query;
            for (String selectionArg : selectionArgs) {
                query = query.replaceFirst("\\?", "'" + selectionArg + "'");
            }

            for (String pattern : badPatterns){
                if (query.matches("(.*)" + pattern + "(.*)"))
                {
                    LogUtil.LOG.info("bad pattern:" + query);
                    return null;
                }
            }

            st = conn.createStatement();
            LogUtil.LOG.info(query);
            rs = st.executeQuery(query);

            if (rs == null) {
                conn.close();
                return conditionReturnData;
            }
            if (rs.next()) {
                String sapConstant = rs.getString(AccessSequenceData.SEQUENCE_SAP_CONSTANT);
                String valueType = rs.getString(AccessSequenceData.SEQUENCE_VALUE_TYPE);
                float value = rs.getFloat(AccessSequenceData.SEQUENCE_VALUE);
                float discountFrom = 0;
                float discountTo = 0;
                try {
                    discountFrom = rs.getFloat(AccessSequenceData.SEQUENCE_DISCOUNT_FROM);
                    discountTo = rs.getFloat(AccessSequenceData.SEQUENCE_DISCOUNT_TO);
                }
                catch (Exception ex) {}
                String creditTerms = "";
                if (accessSequenceData.ConditionType == ItemPricingData.CONDITION_TYPE_CREDIT_TERMS) {
                    try {
                        creditTerms = rs.getString(AccessSequenceData.SEQUENCE_CREDIT_TERMS);
                    } catch (Exception e) {
                        LogUtil.LOG.error("This Will Be Printed On Error8 " + e.getMessage());
                        //System.out.println("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
                    }
                }
                String unitType = null;
                String priceUnit = null;
                try {
                    unitType = rs.getString(AccessSequenceData.SEQUENCE_UNIT_TYPE);
                } catch (Exception e) {
                    LogUtil.LOG.error("This Will Be Printed On Error9 " + e.getMessage());
                    //System.out.println("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
                }
                int UnitType = -1;
                if (unitType == null || unitType.isEmpty()) {
                    UnitType = -1;
                } else if (unitType.equalsIgnoreCase(ItemPricingData.PC_UNIT)) {
                    UnitType = ConditionReturnData.UNIT_TYPE_PC;
                } else if (unitType.equalsIgnoreCase(ItemPricingData.KARTON_UNIT)) {
                    UnitType = ConditionReturnData.UNIT_TYPE_KAR;
                } else if (unitType.equalsIgnoreCase(ItemPricingData.KG_UNIT)) {
                    UnitType = ConditionReturnData.UNIT_TYPE_KG;
                }
                int divideValue = 1;
                if (valueType != null && valueType.equalsIgnoreCase("C")) {
                    try {
                        int index = rs.findColumn(AccessSequenceData.SEQUENCE_DIVIDE_VALUE);
                        if (index != -1)
                            divideValue = rs.getInt(rs.findColumn(AccessSequenceData.SEQUENCE_DIVIDE_VALUE));
                        if (divideValue == 0) {
                            divideValue = 1;
                        }
                    } catch (Exception e) {
                        LogUtil.LOG.error("This Will Be Printed On Error10 " + e.getMessage());
                        //System.out.println("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
                    }
                    String provider =  System.getenv("PROVIDER");
                    //System.out.println("PROVIDER=" + provider);
                    if (provider.equalsIgnoreCase("tambour")) {
                        try {
                            priceUnit = rs.getString(rs.findColumn(AccessSequenceData.SEQUENCE_PRICE_UNIT));
                        } catch (Exception e) {
                            LogUtil.LOG.error("This Will Be Printed On Error11 " + e.getMessage());
                            //System.out.println("Error in line: " + e.getStackTrace()[0].getLineNumber() + ", Error Message:" + e.getMessage());
                        }
                    }
                }
                conditionReturnData = new ConditionReturnData(accessSequenceData.AccessSequence, accessSequenceData.Access, accessSequenceData.TableName, sapConstant, (valueType != null && valueType.equalsIgnoreCase("A")) ? ConditionReturnData.RETURN_VALUE_TYPE_DISCOUNT : ConditionReturnData.RETURN_VALUE_TYPE_PRICE, value, creditTerms, discountFrom, discountTo, UnitType, divideValue, priceUnit);
            }
        } catch (SQLException e) {

            LogUtil.LOG.error("Error106 in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        } finally {
            if (sqlLiteUtil.IsSQlLite())
                sqlLiteUtil.Disconnect(conn);
            else
                DbUtil.CloseConnection(conn,rs,st);
        }
        return conditionReturnData;
    }
}
