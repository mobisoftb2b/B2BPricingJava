package com.mobisale.utils;

import com.mobisale.data.AccessSequenceData;
import com.mobisale.data.ConditionReturnData;
import com.mobisale.data.ItemPricingData;
import com.promotions.database.PromotionsContract;
import com.promotions.database.PromotionsDatabase;

import java.io.Console;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SqlLiteUtil {

    public class ResultHolder{
        public List<Map<String, Object>> values;
        public Map<String, String> typeMap;
    }

    public static Boolean IsSQLite;
    public static Boolean IsSQLiteTablet;
    private static String PricingDBName;
    private static String PromotionsDBName;

    public static void Init(){
        IsSQLite =  (System.getenv("SQLITE") != null && System.getenv("SQLITE").equalsIgnoreCase("true"));
        IsSQLiteTablet = (System.getenv("SQLITE_TABLET") != null && System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"));
        if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true")) {
            PricingDBName = System.getenv("PRICING_DB_SQLITE");
            if (System.getenv("HAS_PROMOTIONS").equalsIgnoreCase("true")) {
                PromotionsDBName = System.getenv("PROMOTIONS_DB_SQLITE");
            }
        }
        else
            PricingDBName = "mobisoft.db";
    }

    public  ResultHolder executeQuery(String query) {
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;

        ResultHolder result = new ResultHolder();
        try {
            conn = DbUtil.connect(conn);


            st = conn.createStatement();
            LogUtil.LOG.info(query);

            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            result.values = new ArrayList<Map<String, Object>>();
            Map<String, Object> row = null;

            result.typeMap = new HashMap<String, String>();

            ResultSetMetaData metaData = rs.getMetaData();
            Integer columnCount = metaData.getColumnCount();

            if (rs == null) {
                return null;
            }

            int rowNum = 0;
            while (rs.next()) {
                row = new HashMap<String, Object>();

                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                    if (rowNum == 0)
                        result.typeMap.put(metaData.getColumnName(i), metaData.getColumnClassName(i));
                }
                result.values.add(row);
                rowNum++;
            }

            st.close();
            return result;

        } catch (SQLException e) {

            LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 127");
        }
        finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
        return null;
    }

    public static void insert(Connection conn, String name, double capacity) {
        String sql = "INSERT INTO warehouses(name,capacity) VALUES(?,?)";

        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setDouble(2, capacity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LogUtil.LOG.error(e);
            //System.out.println(e.getMessage());
        }
    }


    public  void selectAllTableValues(String query, int limit){
        Connection conn = Connect();
        query += " limit " + limit;
        try{

            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(query);

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1)
                        System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println("");
            }
        } catch (SQLException e) {
            LogUtil.LOG.error(e);
            //System.out.println(e.getMessage());
        }
        Disconnect(conn);
    }

    public void selectPromotions(){

        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;

        String rawQuery = "SELECT ESPNumber FROM v_CustPromotions WHERE Cust_Key IN ('1100100010091257','0')";
        try {
                conn = ConnectPromotions();

            st = conn.createStatement();
            System.out.print(rawQuery);
            rs = st.executeQuery(rawQuery);
            if (rs == null) {
                return;
            }
            int columnIndex;
            while (rs.next()) {
                columnIndex = rs.findColumn(PromotionsContract.PromotionCustomers.PROMOTION_CUSTOMERS_ESP_NUMBER);
                String dealCode = rs.getString(columnIndex);
                System.out.print(" espnumber " + dealCode);
            }
        } catch (Exception e) {
            System.out.print("error query for promotion-keys :"+e.getMessage());
        } finally {
            Disconnect(conn);
        }

    }
    public  void executeCommandSqlLite(String sql){
        Connection conn = Connect();
        try{

            Statement stmt  = conn.createStatement();
            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            LogUtil.LOG.error(e);
            //System.out.println(e.getMessage());
        }
        Disconnect(conn);
    }

    public Boolean IsSQlLite() {
        return IsSQLite;
    }

    public  Boolean IsSQLiteTablet(){
        return IsSQLiteTablet;
    }

    public Connection Connect(){
        Connection conn = null;
        try {
            // db parameters (assumes movies.db is in the same directory)
            String url = "jdbc:sqlite:" + PricingDBName;
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            //System.out.println("Connection to SQLite has been established.");


        } catch (SQLException e) {
            LogUtil.LOG.error(e);
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public Connection ConnectPromotions(){
        Connection conn = null;
        try {
            // db parameters (assumes movies.db is in the same directory)
            String url = "jdbc:sqlite:" + PromotionsDBName;
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            //System.out.println("Connection to SQLite has been established.");


        } catch (SQLException e) {
            LogUtil.LOG.error(e);
            System.out.println(e.getMessage());
        }
        return conn;
    }


    public void Disconnect(Connection conn){
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            LogUtil.LOG.error(ex);
            System.out.println(ex.getMessage());
        }
    }

    private int ReadTableToSqlLite(String query, String tableName, boolean addDateTimePriceFilter, String orderBy){
        System.getenv("PRICING_DB_SQLITE"); // mobisoft.db";
        LocalDate myObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        if (addDateTimePriceFilter) {
            String filter = String.format(" where %s between [DATAB] and [DATBI]", formatter.format(myObj));
            query += filter;
        }
        query += " " + orderBy;
        ResultHolder result = executeQuery(query);

        Set<String> keyset = result.typeMap.keySet();
        if (keyset.size() == 0) {
            LogUtil.LOG.warn(tableName + "is empty");
            return -1;
        }
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        String sqlAddTable = "insert into " + tableName + " (";
        String sqlAddTableValues = " VALUES(";
        String className = "";
        for (String key: keyset) {
            className = result.typeMap.get(key);
            sqlCreateTable += key + " ";
            sqlAddTable += key + ",";
            sqlAddTableValues += "?,";
            switch (className) {
               case "java.lang.String":
                   sqlCreateTable += "text, ";
                   break;
               case "java.lang.Short":
               case "java.lang.Integer":
                case "java.lang.Long":
                    sqlCreateTable += "integer, ";
                    break;
               case "java.math.BigDecimal":
                   case "java.lang.Double":
                    sqlCreateTable += "real, ";
                    break;
                case "java.sql.Timestamp":
                    sqlCreateTable += "timestamp, ";
                    break;
               default:
                    sqlCreateTable += "text, ";
                    break;
                }
        }
        //remove last comma and space
        sqlCreateTable = sqlCreateTable.substring(0, sqlCreateTable.length()-2);
        //remove comma
        sqlAddTableValues = sqlAddTableValues.substring(0, sqlAddTableValues.length()-1);
        sqlAddTable = sqlAddTable.substring(0, sqlAddTable.length()-1);
        sqlAddTable += ") " + sqlAddTableValues + ")";
        sqlCreateTable += ");";
        Connection conn = null;
        Statement stmt = null;
        try {
            // create a connection to the database
            conn = Connect();

            stmt = conn.createStatement();
            stmt.execute(sqlCreateTable);
            stmt.close();

            String sqlDelete = "DELETE FROM " + tableName;
            stmt = conn.createStatement();
            stmt.execute(sqlDelete);
            stmt.close();

            Integer rr = 0;
            conn.setAutoCommit(false);
            List<PreparedStatement> pList = new  ArrayList<PreparedStatement>();
            for (Map<String, Object> item: result.values) {
                PreparedStatement pstmt = conn.prepareStatement(sqlAddTable);
                Integer keyIndex = 1;
                for (String key: keyset) {
                    className = result.typeMap.get(key);
                    switch (className){
                        case "java.lang.String":
                            pstmt.setString(keyIndex, (String)item.get(key));
                            break;
                        case "java.lang.Short":
                            pstmt.setInt(keyIndex, (Short)item.get(key));
                            break;
                        case "java.lang.Long":
                            pstmt.setInt(keyIndex, ((Long)item.get(key)).intValue());
                            break;
                        case "java.lang.Integer":
                            pstmt.setInt(keyIndex, (Integer)item.get(key));
                            break;
                        case "java.math.BigDecimal":
                            if (item.get(key) == null)
                                pstmt.setNull(keyIndex, Types.DOUBLE);
                            else
                                pstmt.setDouble(keyIndex, ((java.math.BigDecimal)item.get(key)).doubleValue());
                            break;
                        case "java.lang.Double":
                            if (item.get(key) == null)
                                pstmt.setNull(keyIndex, Types.DOUBLE);
                            else
                                pstmt.setDouble(keyIndex, (Double)item.get(key));
                            break;
                        case "java.sql.Timestamp":
                            pstmt.setTimestamp(keyIndex, (Timestamp) item.get(key));
                            break;
                        default:
                            break;

                    }
                    keyIndex++;
                }
                rr++;
                //System.out.println("row" + rr);
                pstmt.executeUpdate();
                pList.add(pstmt);
            }
            conn.commit();
            for (PreparedStatement pst: pList)
            {
                pst.close();
            }

            Disconnect(conn);
        } catch (SQLException e) {
            LogUtil.LOG.error(e);
            //System.out.println(e.getMessage());
            return -1;
        }
        catch (Exception e){
            LogUtil.LOG.error(e);
            //System.out.println(e.getMessage());
            return -1;
        }
        return 0;
    }

    public int ReadOneTable(String queryColumns, String tableName, boolean addDateTimePriceFilter, String orderBy){

        String delIndex= "DROP INDEX IF EXISTS IX_" + tableName;
        executeCommandSqlLite(delIndex);
        String delTable  = "DROP TABLE IF EXISTS " + tableName;
        executeCommandSqlLite(delTable);

        String queryColumnsFull = queryColumns + " FROM [dbo].[" + tableName + "]";
        String orderByFull = (orderBy == null || orderBy == "")? orderBy : " ORDER BY " + orderBy;
        int result = ReadTableToSqlLite(queryColumnsFull, tableName, addDateTimePriceFilter, orderByFull);
        if (result == 0)
        {
            LogUtil.LOG.info( tableName + " from SQlLite");
            selectAllTableValues(queryColumns + " FROM " + tableName, 2);
        }
        return result;

    }
    public void ReadNoaTables(){

        ReadOnePriceTable("A100", "*",
                "Cust_Key, ItemID", "Cust_Key ASC, ItemID ASC");

        ReadOnePriceTable("A101", "*",
                "Cust_Key, ItemID", "Cust_Key ASC, ItemID ASC");

        ReadOnePriceTable("A200", "*",
                "PriceList, ItemID", "PriceList ASC, ItemID ASC");

        ReadOnePriceTable("A201", "*",
                "Cust_Key, ItemID", "Cust_Key ASC, ItemID ASC");

        ReadOnePriceTable("A250", "*",
                "Cust_Key, ItemID", "Cust_Key ASC, ItemID ASC");

        ReadOnePriceTable("A300", "*",
                "ItemID", "ItemID ASC");

        ReadOnePriceTable("A400", "*",
                "Cust_Key", "Cust_Key ASC");

        ReadOnePriceTable("A500", "*",
                "CustVatType, ItemVatType", "CustVatType ASC, ItemVatType ASC");

        //String queryColumnsPricingDocProcedure_T683V = "SELECT *";
        //ReadOneTable(queryColumnsPricingDocProcedure_T683V, "PricingDocProcedure_T683V", false, "");

        //String queryColumnsB2B_Items = "SELECT [ItemID],[ItemVatType]";
        //ReadOneTable(queryColumnsB2B_Items, "B2B_Items", false, "ORDER BY ItemID ASC");

        //String indexB2B_Items = "CREATE INDEX IX_B2B_Items ON B2B_Items (ItemID)";
        //executeCommandSqlLite(indexB2B_Items);

    }
    private int ReadOnePriceTable(String tableName, String selectFields, String indexFields, String orderByFields){

        String queryColumns = "SELECT " + selectFields;
        int result = ReadOneTable(queryColumns, tableName, true, orderByFields);

        if (result == 0) {
            String indexA = "CREATE INDEX IX_" + tableName + " ON " + tableName + " (" + indexFields + ")";
            executeCommandSqlLite(indexA);
        }
        return result;
    }

    public void ReadStraussPricingDB(){
        selectAllTableValues("select * from A002", 5);
        selectPromotions();
    }

    public void ReadNoaPricingDB(){
        selectAllTableValues("select * from A100", 5);
    }
    public void ReadStraussTables(){

        ReadOnePriceTable("A002", "*",
                "KSCHL ASC, ALAND ASC, TAXK1 ASC, TAXM1 ASC, DATBI ASC, DATAB ASC, KNUMH ASC", "");

        ReadOnePriceTable("A011", "*",
                "KSCHL ASC, ALAND ASC, LLAND ASC, TAXK1 ASC, TAXM1 ASC, DATBI ASC, KNUMH ASC", "");

        ReadOnePriceTable("A305", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, KUNNR ASC, MATNR ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A307", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, KUNNR ASC, KNUMH ASC, DATBI ASC", "");


        ReadOnePriceTable("A507", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BUS_TYP1 ASC, ZZ_BUS_TYP2 ASC, EAN11 ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A610", "*",
                "KSCHL ASC, MATNR ASC, DATBI ASC, KNUMH ASC", "");

        ReadOnePriceTable("A611", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A614", "*",
        "KSCHL ASC, VKORG ASC, VTWEG ASC, KUNNR ASC, ZZ_OPRTN_CTG ASC, SPART ASC, DATBI ASC, KNUMH ASC", "");

        ReadOnePriceTable("A615", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BUS_TYP1 ASC, SPART ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A616", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BUS_TYP1 ASC, KNUMH ASC, KRECH ASC, DATBI ASC", "");

        ReadOnePriceTable("A618", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, KUNNR ASC, SPART ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A619", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, KUNNR ASC, ZZ_OPRTN_CTG ASC, DATBI ASC, KNUMH ASC", "");

        ReadOnePriceTable("A621", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_TRD_DISCNT ASC, KNUMH ASC, KRECH ASC, DATBI ASC", "");

        ReadOnePriceTable("A622", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, KUNNR ASC, PRODH ASC, DATBI ASC, KNUMH ASC", "");

        ReadOnePriceTable("A623", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_TRD_DISCNT ASC, PRODH ASC, DATBI ASC, KNUMH ASC", "");


        ReadOnePriceTable("A624", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_TRD_DISCNT ASC, MATNR ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A625", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_TRD_DISCNT ASC, ZZ_OPRTN_CTG ASC, SPART ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A626", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, KNUMH ASC, PLTYP ASC, MATNR ASC, KFRST ASC, DATBI ASC", "");

        ReadOnePriceTable("A630", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_OPRTN_CTG ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A634", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_HR_CNNL ASC, MATNR ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A636", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, KUNNR ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A637", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_HR_CNNL ASC, ZZ_CUST_GROUP1 ASC, MATNR ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A639", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_HR_CNNL ASC, ZZ_BUS_TYP1 ASC, ZZ_SECTOR ASC, MATNR ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A640", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_HR_CNNL ASC, ZZ_BUS_TYP1 ASC, ZZ_SECTOR ASC, PRODH ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A641", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_HR_CNNL ASC, ZZ_BUS_TYP1 ASC, MATNR ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A642", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_HR_CNNL ASC, ZZ_BUS_TYP1 ASC, PRODH ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A643", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_HR_CNNL ASC, ZZ_CST_VIP ASC, MATNR ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A644", " *",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_HR_CNNL ASC, ZZ_CST_VIP ASC, PRODH ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A666", "*",
                "KSCHL ASC, ZZ_TAXKD ASC, ZZ_DEPOSIT_TYPE ASC, KNUMH ASC, DATBI ASC", "");

        ReadOnePriceTable("A667", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BUS_TYP1 ASC, ZZ_BUS_TYP2 ASC, MATNR ASC, DATBI ASC", "");

        ReadOnePriceTable("A674", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_FOODLAW_TYP ASC, MTART ASC, DATBI ASC, KNUMH ASC", "");

        ReadOnePriceTable("A675", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_FOODLAW_TYP ASC, MATNR ASC, DATBI ASC, KNUMH ASC", "");

        ReadOnePriceTable("A676", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BUS_TYP1 ASC, ZZ_BUS_TYP2 ASC, DATBI ASC, KNUMH ASC", "");

        ReadOnePriceTable("A680", "*",
                "KSCHL ASC, VKORG ASC, VTWEG ASC, ZZ_BDG_ORG_ASS ASC, KNUMH ASC, KRECH ASC, DATBI ASC", "");

        ReadOnePriceTable("A950", "*",
                "KSCHL ASC, MATNR ASC, DATBI ASC, KNUMH ASC", "");

        /*String queryColumnsPricingDocProcedure_T683V = "SELECT [SalesOrganization], [DistributionChannel], [Division], [DocPricingProcedure], [CustPricingProcedure], [PricingProcedure], [ConditionType],[ProcDiscountInKind], [BonusBuySchema],[LastChange]";
        ReadOneTable(queryColumnsPricingDocProcedure_T683V, "PricingDocProcedure_T683V", false, "");

        String queryColumnsB2B_Items = "SELECT [ItemID],[ItemVatType]";
        ReadOneTable(queryColumnsB2B_Items, "B2B_Items", false, "ORDER BY ItemID ASC");

        String indexB2B_Items = "CREATE INDEX IX_B2B_Items ON B2B_Items (ItemID)";
        executeCommandSqlLite(indexB2B_Items);
        */
    }



}
