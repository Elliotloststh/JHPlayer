package Utils;

import java.sql.*;

public class dbConnecter {
    // JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/MusicPlayer?serverTimezone=UTC&characterEncoding=utf-8&useSSL=false";

    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "19980508yjh";
    static Connection conn = null;
    static Statement stmt = null;
    public static void db_connect () throws Exception{
        Class.forName(JDBC_DRIVER);
        // 打开链接
        System.out.println("连接数据库...");
        conn = DriverManager.getConnection(DB_URL,USER,PASS);
        System.out.println("实例化Statement对象...");
        stmt = conn.createStatement();
        System.out.println("初始化完成");
    }

    public static ResultSet sqlQuery(String sql) throws Exception {
        return stmt.executeQuery(sql);
    }

    public static int sqlExecute(String sql) throws Exception {
        return stmt.executeUpdate(sql);
    }


}
