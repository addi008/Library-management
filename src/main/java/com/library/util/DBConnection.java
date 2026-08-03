package com.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class for Database Connection management.
 * Reads connection settings from db.properties.
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static DBConnection instance;

    private String driver;
    private String url;
    private String user;
    private String password;

    private DBConnection() {
        loadProperties();
        try {
            Class.forName(driver);
            LOGGER.info("JDBC Driver loaded successfully: " + driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load JDBC driver: " + driver, e);
            throw new RuntimeException("JDBC Driver not found", e);
        }
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                LOGGER.warning("db.properties not found on classpath, using default values.");
                useDefaultH2Properties(props);
            } else {
                props.load(input);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading db.properties file, using defaults.", e);
            useDefaultH2Properties(props);
        }

        this.driver = props.getProperty("db.driver", "org.h2.Driver");
        this.url = props.getProperty("db.url", "jdbc:h2:mem:library_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        this.user = props.getProperty("db.user", "sa");
        this.password = props.getProperty("db.password", "");
    }

    private void useDefaultH2Properties(Properties props) {
        props.setProperty("db.driver", "org.h2.Driver");
        props.setProperty("db.url", "jdbc:h2:mem:library_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        props.setProperty("db.user", "sa");
        props.setProperty("db.password", "");
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // If configured MySQL connection fails, attempt fallback to H2 embedded database for test environment readiness
            if (driver.contains("mysql") && !url.contains("h2")) {
                LOGGER.warning("Could not connect to MySQL server at (" + url + "). Attempting embedded test fallback...");
                try {
                    String fallbackDriver = "org.h2.Driver";
                    String fallbackUrl = "jdbc:h2:mem:library_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'schema.sql'";
                    Class.forName(fallbackDriver);
                    return DriverManager.getConnection(fallbackUrl, "sa", "");
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Fallback database connection also failed.", ex);
                }
            }
            throw e;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }
}
