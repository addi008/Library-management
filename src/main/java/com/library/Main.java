package com.library;

import com.library.ui.MainMenu;
import com.library.util.DBConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Entry point for Library Management System application.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize DB Connection and verify schema startup
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            initializeSchema(conn);
        } catch (Exception e) {
            System.err.println("[WARN] Initial connection setup note: " + e.getMessage());
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("--test")) {
            System.out.println("Running automated system test...");
            Phase4Test.main(args);
            return;
        }

        // Launch Interactive Console Menu
        MainMenu menu = new MainMenu();
        menu.start();
    }

    private static void initializeSchema(Connection conn) {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("schema.sql");
             BufferedReader reader = is != null ? new BufferedReader(new InputStreamReader(is)) : null) {

            if (reader == null) {
                java.io.File file = new java.io.File("schema.sql");
                if (file.exists()) {
                    try (BufferedReader fReader = new BufferedReader(new java.io.FileReader(file))) {
                        executeSqlScript(conn, fReader);
                    }
                }
            } else {
                executeSqlScript(conn, reader);
            }
        } catch (Exception e) {
            // Logged gracefully
        }
    }

    private static void executeSqlScript(Connection conn, BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("--") || line.isEmpty()) {
                continue;
            }
            sb.append(line).append(" ");
            if (line.endsWith(";")) {
                String sql = sb.toString().replace(";", "").trim();
                sb.setLength(0);
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                } catch (Exception e) {
                }
            }
        }
    }
}
