package com.library.util;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Utility class for user input validation and crash prevention in console UI.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\-\\+\\s]{7,15}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isPositiveInteger(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            int val = Integer.parseInt(input.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            int val = Integer.parseInt(input.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (isPositiveInteger(line)) {
                return Integer.parseInt(line);
            }
            System.out.println("❌ Invalid input! Please enter a positive number (> 0).");
        }
    }

    public static int readNonNegativeInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (isNonNegativeInteger(line)) {
                return Integer.parseInt(line);
            }
            System.out.println("❌ Invalid input! Please enter a non-negative number (>= 0).");
        }
    }

    public static String readEmail(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (isValidEmail(line)) {
                return line;
            }
            System.out.println("❌ Invalid email format! Example: user@example.com");
        }
    }

    public static String readPhone(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (isValidPhone(line)) {
                return line;
            }
            System.out.println("❌ Invalid phone number! Must be 7-15 digits/hyphens. Example: 555-0101");
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("❌ Field cannot be empty. Please enter text.");
        }
    }
}
