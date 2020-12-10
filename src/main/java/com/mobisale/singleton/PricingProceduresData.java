package com.mobisale.singleton;


import com.mobisale.columns.PricingDocProcedure;
import com.mobisale.columns.PricingProcedures;
import com.mobisale.constants.Tables;
import com.mobisale.data.PricingProcedureData;
import com.mobisale.data.PricingProcedureListData;
import com.mobisale.data.PricingProcedureUpdateData;
import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;
import com.mobisale.utils.SqlLiteUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class PricingProceduresData {

    private static PricingProceduresData m_instance = null;
    private ConcurrentHashMap<String, PricingProcedureListData> pricingProceduresMap = new ConcurrentHashMap<String, PricingProcedureListData>();
    private ConcurrentHashMap<String, String> pricingProceduresCust = new ConcurrentHashMap<String, String>();

    private SqlLiteUtil sqlLiteUtil = new SqlLiteUtil();

    private PricingProceduresData() {
    }

    public static PricingProceduresData getInstance() {
        if (m_instance == null) {
            m_instance = new PricingProceduresData();
        }
        // Return the instance
        return m_instance;
    }

    public void updateSubtotalPricingProcedure()
    {
        if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet()) {

            Connection conn = null;

            ArrayList< PricingProcedureUpdateData> arrayToUpdate = new ArrayList<>();
           try {
                if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                    conn = sqlLiteUtil.Connect();

                String query = "SELECT distinct p1." + PricingProcedures.PRICING_PROCEDURES_SQLITE_ID  + ", p1." + PricingProcedures.PRICING_PROCEDURES_PROCEDURE + ", " +
                        " p1." + PricingProcedures.PRICING_PROCEDURES_STEP_NUMBER + ", p2." + PricingProcedures.PRICING_PROCEDURES_SUBTOTAL + " as NewSubtotal from "
                        + Tables.TABLE_PRICING_PROCEDURES +
                        " p1 join " + Tables.TABLE_PRICING_PROCEDURES + " p2 on p1." + PricingProcedures.PRICING_PROCEDURES_PROCEDURE + "=" + "p2." + PricingProcedures.PRICING_PROCEDURES_PROCEDURE +
                        " where p2." + PricingProcedures.PRICING_PROCEDURES_FROM_STEP + "!=0 AND p2." +  PricingProcedures.PRICING_PROCEDURES_TO_STEP + "!=0 and p2." +
                        PricingProcedures.PRICING_PROCEDURES_SUBTOTAL + " IS NOT NULL AND p1." +
                        PricingProcedures.PRICING_PROCEDURES_STEP_NUMBER + ">p2." + PricingProcedures.PRICING_PROCEDURES_FROM_STEP + " AND p1." + PricingProcedures.PRICING_PROCEDURES_STEP_NUMBER + "< p2." +
                        PricingProcedures.PRICING_PROCEDURES_TO_STEP +
                        " AND p1." + PricingProcedures.PRICING_PROCEDURES_SUBTOTAL + " IS NULL";

               Statement st = conn.createStatement();
                LogUtil.LOG.info(query);
                // execute the query, and get a java resultset
               ResultSet rs = st.executeQuery(query);

                if (rs == null) {
                    return;
                }
                while (rs.next()) {
                    int id = Integer.parseInt(rs.getString(PricingProcedures.PRICING_PROCEDURES_SQLITE_ID));
                    String Procedure = rs.getString(PricingProcedures.PRICING_PROCEDURES_PROCEDURE);
                    int StepNumber = Integer.parseInt(rs.getString(PricingProcedures.PRICING_PROCEDURES_STEP_NUMBER));
                    String NewSubTotal = rs.getString(PricingProcedures.PRICING_PROCEDURES_NEW_SUBTOTAL);
                    PricingProcedureUpdateData data = new PricingProcedureUpdateData(id, Procedure, StepNumber, NewSubTotal);
                    arrayToUpdate.add(data);
                }

                for (PricingProcedureUpdateData data : arrayToUpdate) {
                    String query2 = "UPDATE " + Tables.TABLE_PRICING_PROCEDURES + " SET " + PricingProcedures.PRICING_PROCEDURES_SUBTOTAL + "=" + data.NewSubtotal + " where " + PricingProcedures.PRICING_PROCEDURES_SQLITE_ID + "=" + data.Id;
                    LogUtil.LOG.info(query2);
                    st.executeUpdate(query2);
                }
            } catch (SQLException e) {
                LogUtil.LOG.error("Error in line: " + e.getStackTrace()[0].getLineNumber() + ", Error Message:" + e.getMessage() + " 125");
            } finally {
                    sqlLiteUtil.Disconnect(conn);
            }
        }
    }

    public void clearResources(){
        pricingProceduresMap.clear();
    }
    public synchronized void executeQuery() {
        pricingProceduresMap.clear();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.Connect();
            else
                conn = DbUtil.connect(conn);

            String PRICING_PROCEDURES_TABLE =  Tables.TABLE_PRICING_PROCEDURES;
            if (System.getenv("PROVIDER").equalsIgnoreCase("strauss")) {
                PRICING_PROCEDURES_TABLE = Tables.TABLE_PRICING_PROCEDURES_B2B;
            }

            //" WHERE " + PricingProcedures.PRICING_PROCEDURES_CONDITION_TYPE + " IS NOT NULL
            String query = "SELECT * FROM " + PRICING_PROCEDURES_TABLE + " ORDER BY '" +
                    PricingProcedures.PRICING_PROCEDURES_PROCEDURE +"'"+
                    "," + PricingProcedures.PRICING_PROCEDURES_STEP_NUMBER + " ASC";

            st = conn.createStatement();
            LogUtil.LOG.info(query);
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            if (rs == null) {
                return;
            }
            while (rs.next()) {
                String Procedure = rs.getString(PricingProcedures.PRICING_PROCEDURES_PROCEDURE);//cursor.getString(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_PROCEDURE));
                int StepNumber = Integer.parseInt(rs.getString(PricingProcedures.PRICING_PROCEDURES_STEP_NUMBER));//cursor.getInt(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_STEP_NUMBER));
                String ConditionType = rs.getString(PricingProcedures.PRICING_PROCEDURES_CONDITION_TYPE);//cursor.getString(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_CONDITION_TYPE));
                int FromStep = Integer.parseInt(rs.getString(PricingProcedures.PRICING_PROCEDURES_FROM_STEP));//cursor.getInt(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_FROM_STEP));
                int ToStep = Integer.parseInt(rs.getString(PricingProcedures.PRICING_PROCEDURES_TO_STEP));//cursor.getInt(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_TO_STEP));
                String ManualOnly = rs.getString(PricingProcedures.PRICING_PROCEDURES_MANUAL_ONLY);//cursor.getString(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_MANUAL_ONLY));
                String Requirement = rs.getString(PricingProcedures.PRICING_PROCEDURES_REQUIREMENT);//cursor.getString(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_REQUIREMENT));
                String Subtotal = rs.getString(PricingProcedures.PRICING_PROCEDURES_SUBTOTAL);//cursor.getString(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_SUBTOTAL));
                String Statistical = rs.getString(PricingProcedures.PRICING_PROCEDURES_STATISTICAL);//cursor.getString(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_STATISTICAL));
                String AltCondBaseValue = rs.getString(PricingProcedures.PRICING_PROCEDURES_ALT_CONDITION_BASE_VALUE);//cursor.getString(cursor.getColumnIndex(PricingContract.PricingProcedures.PRICING_PROCEDURES_ALT_CONDITION_BASE_VALUE));
                String SkipToCondition = "";
                int IsResetOnPromotion = 0;
                if (System.getenv("PROVIDER").equalsIgnoreCase("tambour")) {
                    SkipToCondition = rs.getString(PricingProcedures.PRICING_PROCEDURES_SKIP_TO_CONDITION);//DbUtil.getString(cursor, PricingContract.PricingProcedures.PRICING_PROCEDURES_SKIP_TO_CONDITION, "");
                    IsResetOnPromotion = Integer.parseInt(rs.getString(PricingProcedures.PRICING_PROCEDURES_IS_RESET_ON_PROMOTION));//DbUtil.getInt(cursor, PricingContract.PricingProcedures.PRICING_PROCEDURES_IS_RESET_ON_PROMOTION, 0);
                }
                if (FromStep > 0 && ToStep <= 0) {
                    ToStep = FromStep;
                }
                PricingProcedureData pricingProcedureData = new PricingProcedureData(StepNumber, ConditionType, FromStep, ToStep, ManualOnly != null && ManualOnly.equalsIgnoreCase("X"), Requirement, Subtotal == null ? "" : Subtotal, Statistical != null && Statistical.equalsIgnoreCase("X"), AltCondBaseValue, SkipToCondition, IsResetOnPromotion == 1);
                PricingProcedureListData pricingProcedureListData = pricingProceduresMap.get(Procedure);
                if (pricingProcedureListData == null) {
                    pricingProcedureListData = new PricingProcedureListData(Procedure);
                }
                pricingProcedureListData.addPricingProcedureData(pricingProcedureData);
                pricingProceduresMap.put(Procedure, pricingProcedureListData);

            }
        }
        catch (SQLException e) {
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 125");
        } finally {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                sqlLiteUtil.Disconnect(conn);
            else
                DbUtil.CloseConnection(conn,rs,st);
        }

    }

    private synchronized String getPricingProcedureNameFromSql(String Cust_Key, String DocNum, String RequestId) {

        String Procedure = "";
        if (System.getenv("PROVIDER").equalsIgnoreCase("strauss"))
            Procedure = "Z20002";
        else
        if (System.getenv("PROVIDER").equalsIgnoreCase("hcohen"))
            Procedure = "Mobi";
        else
        if (System.getenv("PROVIDER").equalsIgnoreCase("tambour"))
            Procedure = "MOBI";
        else {
            ResultSet rs = null;
            CallableStatement st = null;
            Connection conn = null;
            try {
                //if (sqlLiteUtil.IsSQlLite())
                //   conn = sqlLiteUtil.Connect();
                //else
                conn = DbUtil.connect(conn);
                //String query = "SELECT " + PricingDocProcedure.PRICING_DOC_PROCEDURE_VALUE + " FROM [PricingDocProcedure_T683V]";

                LogUtil.LOG.info(RequestId + " " + "DocNum=" + DocNum  + " call B2B_Pricing_GetPricingProcedure for cust_key=" + Cust_Key);
                st = conn.prepareCall("{call B2B_Pricing_GetPricingProcedure(?)}");
                st.setString(1, Cust_Key);
                st.execute();
                rs = st.getResultSet();

                if (rs == null)
                    return Procedure;

                rs.next();
                Procedure = rs.getString(PricingDocProcedure.PRICING_DOC_PROCEDURE_VALUE);

            } catch (SQLException e) {
                LogUtil.LOG.error(RequestId + " " + "DocNum=" + DocNum  + " Error in line: " + e.getStackTrace()[0].getLineNumber() + ", Error Message:" + e.getMessage() + " 126");
            } finally {
                //if (sqlLiteUtil.IsSQlLite())
                //   sqlLiteUtil.Disconnect(conn);
                //else
                DbUtil.CloseConnection(conn, rs, st);
            }
        }
        return Procedure;

    }
    public synchronized String getPricingProcedureName(String Cust_Key, String DocNum, String RequestId) {
        String procName = "";
        if (pricingProceduresCust.get(Cust_Key) == null) {
            procName = getPricingProcedureNameFromSql(Cust_Key, DocNum, RequestId);
            pricingProceduresCust.put(Cust_Key, procName);
        }
        else
            procName = pricingProceduresCust.get(Cust_Key);
        //LogUtil.LOG.info("pricing procedure name=" + procName);
        LogUtil.LOG.info(RequestId + " " + "DocNum=" + DocNum  + " pricing procedure name=" + procName);
        return procName;
    }

    public synchronized PricingProcedureListData getPricingProcedureData(String Cust_Key) {
        String procName = getPricingProcedureName(Cust_Key, "", "");
        LogUtil.LOG.info("getPricingProcedureData pricing procedure name=" + procName);
        return pricingProceduresMap.get(procName);
    }

    public synchronized PricingProcedureListData getPricingProcedureDataByProcedureName(String pricingProcedureName) {
        return pricingProceduresMap.get(pricingProcedureName);
    }
}
