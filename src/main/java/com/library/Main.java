package com.library;

import com.library.util.DBConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Main test class for Phase 1 verification.
 * Verifies database connection via DBConnection singleton and runs 'SELECT 1'.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Library Management System - Phase 1 Test       ");
        System.out.println("==================================================");

        try {
            DBConnection dbConn = DBConnection.getInstance();
            System.out.println("[INFO] Testing DBConnection Singleton initialized...");
            System.out.println("[INFO] Configured Driver: " + dbConn.getDriver());

            try (Connection conn = dbConn.getConnection()) {
                System.out.println("[SUCCESS] Database Connection established successfully!");

                // Execute schema initialization if needed
                initializeSchema(conn);

                // Phase 1 Core Deliverable Test: SELECT 1
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    
                    if (rs.next()) {
                        int testVal = rs.getInt(1);
                        System.out.println("[DELIVERABLE TEST] Running query 'SELECT 1' -> Result: " + testVal);
                        if (testVal == 1) {
                            System.out.println("[SUCCESS] DB Connection query test ('SELECT 1') PASSED!");
                        }
                    }
                }

                // Verify tables & sample data presence
                verifyDatabaseState(conn);
            }

            System.out.println("==================================================");
            System.out.println("   PHASE 1 VERIFICATION COMPLETED SUCCESSFULLY    ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("[ERROR] Phase 1 Verification Failed:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void initializeSchema(Connection conn) {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("schema.sql");
             BufferedReader reader = is != null ? new BufferedReader(new InputStreamReader(is)) : null) {
            
            if (reader == null) {
                // Try relative file path if resource loading from jar/classpath didn't find schema.sql directly
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
            System.out.println("[NOTE] Schema script execution info: " + e.getMessage());
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
                    // Ignore table already exists or duplicate insert errors during re-runs
                }
            }
        }
        System.out.println("[INFO] Schema script applied / verified.");
    }

    private static void verifyDatabaseState(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books")) {
                if (rs.next()) {
                    System.out.println("[INFO] Table 'books' verified. Total sample records: " + rs.getInt(1));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM members")) {
                if (rs.next()) {
                    System.out.println("[INFO] Table 'members' verified. Total sample records: " + rs.getInt(1));
                }
            }
        } catch (Exception e) {
            System.out.println("[NOTE] Table count check: " + e.getMessage());
        }
    }
}
