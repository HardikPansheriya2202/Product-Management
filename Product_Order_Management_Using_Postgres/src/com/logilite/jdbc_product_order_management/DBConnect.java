package com.logilite.jdbc_product_order_management;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class DBConnect {
    
    private static final Logger adminLogger = LoggerConfig.getLogger("AdminLogger", "admin.log");
    
    private static String driver;
    private static String host;
    private static String port;
    private static String name;
    private static String user;
    private static String password;
    private static String url;
    public static Connection conn = null;
    
    static {
        loadConfigData();
    }
    
    public static void loadConfigData() {
        Properties p = new Properties();
        
        try (FileReader reader = new FileReader("db_config.properties")) {
            p.load(reader);
            
            driver = p.getProperty("db.driver");
            host = p.getProperty("db.host");
            port = p.getProperty("db.port");
            name = p.getProperty("db.name");
            user = p.getProperty("db.user");
            password = p.getProperty("db.password");
            
            url = String.format("%s://%s:%s/%s", driver, host, port, name);
            
            adminLogger.info("Database configuration loaded successfully.");
        } catch (Exception e) {
            adminLogger.severe("Error loading database configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() {
        try {
        	String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");
            System.out.println(dbUser);
            System.out.println(dbPassword);
            conn = DriverManager.getConnection(url, user, password);
            if (conn != null) {
                adminLogger.info("Successfully connected to the database.");
                System.out.println("Connected to the database!");
            } else {
                adminLogger.warning("Failed to make connection to the database.");
                System.out.println("==> Failed to make connection! <==");
            }
        } catch (SQLException e) {
            adminLogger.severe("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
}
