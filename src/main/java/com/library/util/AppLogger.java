package com.library.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Utility for application-wide logging setup.
 */
public class AppLogger {

    private static boolean isInitialized = false;

    public static synchronized void setup() {
        if (isInitialized) return;

        Logger rootLogger = Logger.getLogger("");
        try {
            // Add FileHandler to output logs to library.log
            FileHandler fileHandler = new FileHandler("library.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);
            rootLogger.addHandler(fileHandler);

            rootLogger.setLevel(Level.INFO);
            isInitialized = true;
        } catch (IOException e) {
            System.err.println("[WARN] Could not initialize library.log file handler: " + e.getMessage());
        }
    }

    public static Logger getLogger(String className) {
        setup();
        return Logger.getLogger(className);
    }
}
