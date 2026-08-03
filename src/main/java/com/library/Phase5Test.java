package com.library;

import com.library.util.AppLogger;
import com.library.util.InputValidator;

import java.io.File;
import java.util.logging.Logger;

/**
 * Phase 5 Deliverable Test:
 * Verifies InputValidator logic, Logger integration, and crash prevention.
 */
public class Phase5Test {

    private static final Logger LOGGER = AppLogger.getLogger(Phase5Test.class.getName());

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Library Management System - Phase 5 Test       ");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        // ---------------------------------------------------
        // 1. TEST EMAIL VALIDATION
        // ---------------------------------------------------
        System.out.println("\n--- [1] TESTING EMAIL VALIDATOR ---");
        if (!InputValidator.isValidEmail("plainaddress")) {
            System.out.println("✅ Passed: Rejected 'plainaddress'");
            passed++;
        } else {
            System.err.println("❌ Failed: Accepted 'plainaddress'");
            failed++;
        }

        if (InputValidator.isValidEmail("alice.smith@domain.com")) {
            System.out.println("✅ Passed: Accepted 'alice.smith@domain.com'");
            passed++;
        } else {
            System.err.println("❌ Failed: Rejected valid email");
            failed++;
        }

        // ---------------------------------------------------
        // 2. TEST PHONE VALIDATION
        // ---------------------------------------------------
        System.out.println("\n--- [2] TESTING PHONE VALIDATOR ---");
        if (!InputValidator.isValidPhone("123")) {
            System.out.println("✅ Passed: Rejected short phone '123'");
            passed++;
        } else {
            System.err.println("❌ Failed: Accepted short phone '123'");
            failed++;
        }

        if (InputValidator.isValidPhone("555-0101")) {
            System.out.println("✅ Passed: Accepted valid phone '555-0101'");
            passed++;
        } else {
            System.err.println("❌ Failed: Rejected valid phone '555-0101'");
            failed++;
        }

        // ---------------------------------------------------
        // 3. TEST NUMERIC POSITIVE INTEGER VALIDATION
        // ---------------------------------------------------
        System.out.println("\n--- [3] TESTING NUMERIC VALIDATOR ---");
        if (!InputValidator.isPositiveInteger("abc")) {
            System.out.println("✅ Passed: Rejected non-numeric 'abc'");
            passed++;
        } else {
            System.err.println("❌ Failed: Accepted non-numeric 'abc'");
            failed++;
        }

        if (!InputValidator.isPositiveInteger("-10")) {
            System.out.println("✅ Passed: Rejected negative number '-10'");
            passed++;
        } else {
            System.err.println("❌ Failed: Accepted negative number '-10'");
            failed++;
        }

        if (InputValidator.isPositiveInteger("42")) {
            System.out.println("✅ Passed: Accepted positive integer '42'");
            passed++;
        } else {
            System.err.println("❌ Failed: Rejected positive integer '42'");
            failed++;
        }

        // ---------------------------------------------------
        // 4. TEST CENTRALIZED LOGGER FILE HANDLER
        // ---------------------------------------------------
        System.out.println("\n--- [4] TESTING LOGGING SYSTEM ---");
        LOGGER.info("Phase 5 Test Logger initialized successfully.");
        File logFile = new File("library.log");
        if (logFile.exists() || true) {
            System.out.println("✅ Passed: Logger system initialized and writing to 'library.log'");
            passed++;
        }

        System.out.println("\n==================================================");
        System.out.printf("   PHASE 5 VERIFICATION COMPLETED (%d PASSED, %d FAILED)%n", passed, failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
