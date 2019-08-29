package com.mtn.mobisale.utils;


import java.sql.*;

public class DbUtil {
    public static Connection connect(Connection conn) {
        try {
            // db parameters
            String url = System.getenv("DB_SERVER") != null ? "jdbc:sqlserver://" + System.getenv("DB_SERVER")  + ";databaseName=" + System.getenv("DB_DB")
                    : "jdbc:sqlserver://10.0.0.44\\MTNMSSQLSERVER;databaseName=Noa_B2B";
            String username=System.getenv("DB_USER") != null ? System.getenv("DB_USER"): "sa";
            String password=System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD"): "master2w";

            /*String url =  "jdbc:sqlserver://192.168.1.2;databaseName=Mobiplus_B2B";
            String username="MS";
            String password="MsNoa123!";

             */

            conn = java.sql.DriverManager.getConnection(url,username,password);


            return conn;
        } catch (SQLException e) {
            LogUtil.LOG.error("Error :"+e.getMessage());

        }

        return null;
    }
    public static void CloseConnection(Connection conn,ResultSet rs, Statement st){
        if (rs != null) {
            try {
                rs.close();
            }
            catch (SQLException e)
            {
                LogUtil.LOG.error("Error :"+e.getMessage());
            }
        }
        if (st != null) {
            try {
                st.close();
            }
            catch (SQLException e) {
                LogUtil.LOG.error("Error :"+e.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException e) {
                LogUtil.LOG.error("Error :"+e.getMessage());
            }
        }
    }

    public static Connection ProcedureConnection(Connection conn) {
        try {
            // db parameters

            String url = System.getenv("DB_SERVER") != null ? "jdbc:sqlserver://" + System.getenv("DB_SERVER")  + ";databaseName=" + System.getenv("DB_DB")
                    : "jdbc:sqlserver://10.0.0.44\\MTNMSSQLSERVER;databaseName=Noa_B2B";
            String username=System.getenv("DB_USER") != null ? System.getenv("DB_USER"): "sa";
            String password=System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD"): "master2w";
            conn = java.sql.DriverManager.getConnection(url,username,password);

            return conn;
        } catch (SQLException e) {
            LogUtil.LOG.error("Error :"+e.getMessage());
        }

        return null;
    }

}
