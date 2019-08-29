package com.mtn.mobisale.singleton;


import com.mtn.mobisale.columns.PricingDocProcedure;
import com.mtn.mobisale.columns.PricingProcedures;
import com.mtn.mobisale.constants.Tables;
import com.mtn.mobisale.data.PricingProcedureData;
import com.mtn.mobisale.data.PricingProcedureListData;
import com.mtn.mobisale.utils.DbUtil;
import com.mtn.mobisale.utils.LogUtil;

import java.sql.*;
import java.util.HashMap;

public class PricingProceduresData {

    private static PricingProceduresData m_instance = null;
    private HashMap<String, PricingProcedureListData> pricingProceduresMap = new HashMap<String, PricingProcedureListData>();

    private PricingProceduresData() {
    }

    public static PricingProceduresData getInstance() {
        if (m_instance == null) {
            m_instance = new PricingProceduresData();
        }
        // Return the instance
        return m_instance;
    }

    public void executeQuery() {
        pricingProceduresMap.clear();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            conn = DbUtil.connect(conn);
            String query = "SELECT * FROM " + Tables.TABLE_PRICING_PROCEDURES + " ORDER BY '" +
                    PricingProcedures.PRICING_PROCEDURES_PROCEDURE +"'"+
                    "," + PricingProcedures.PRICING_PROCEDURES_STEP_NUMBER + " ASC";

            st = conn.createStatement();
            LogUtil.LOG.error(query);
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
                String SkipToCondition = rs.getString(PricingProcedures.PRICING_PROCEDURES_SKIP_TO_CONDITION);//DbUtil.getString(cursor, PricingContract.PricingProcedures.PRICING_PROCEDURES_SKIP_TO_CONDITION, "");
                int IsResetOnPromotion = Integer.parseInt(rs.getString(PricingProcedures.PRICING_PROCEDURES_IS_RESET_ON_PROMOTION));//DbUtil.getInt(cursor, PricingContract.PricingProcedures.PRICING_PROCEDURES_IS_RESET_ON_PROMOTION, 0);

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
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        } finally {
            DbUtil.CloseConnection(conn,rs,st);
        }

    }

    private String getPricingProcedureName() {

        String Procedure = "";

        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            conn = DbUtil.connect(conn);
            String query = "SELECT " + PricingDocProcedure.PRICING_DOC_PROCEDURE_VALUE + " FROM " + Tables.TABLE_PRICING_DOC_PROCEDURE;

            st = conn.createStatement();
            LogUtil.LOG.error(query);
            rs = st.executeQuery(query);

            if (rs == null)
                return Procedure;

            rs.next();
            Procedure = rs.getString(PricingDocProcedure.PRICING_DOC_PROCEDURE_VALUE);

        }
        catch (SQLException e) {
            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage());
        } finally {
            DbUtil.CloseConnection(conn,rs,st);
        }

        return Procedure;

    }

    public PricingProcedureListData getPricingProcedureData() {
        return pricingProceduresMap.get(getPricingProcedureName());
    }
}
