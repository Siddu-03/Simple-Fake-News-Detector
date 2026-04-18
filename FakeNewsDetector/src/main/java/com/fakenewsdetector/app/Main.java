package com.fakenewsdetector.app;

import com.fakenewsdetector.ui.ConsoleUI;
import com.fakenewsdetector.ui.MainWindow;

/**
 * Main
 * ----
 * Entry point of the Fake News Detector.
 *
 * Usage:
 *   java -jar FakeNewsDetector.jar           →  launches the Swing GUI (default)
 *   java -jar FakeNewsDetector.jar --cli     →  launches the original CLI
 *
 * To build:
 *   mvn package
 *   java -jar target/FakeNewsDetector.jar
 */
public class Main {

    public static void main(String[] args) {
        boolean useCLI = args.length > 0 && args[0].equalsIgnoreCase("--cli");

        if (useCLI) {
            // Original terminal-based interface
            new ConsoleUI().start();
        } else {
            // Swing graphical interface (default)
            MainWindow.launch();
        }
    }
}
