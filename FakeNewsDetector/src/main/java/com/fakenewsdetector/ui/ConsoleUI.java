package com.fakenewsdetector.ui;

import com.fakenewsdetector.analyzer.NewsAnalyzer;
import com.fakenewsdetector.model.AnalysisResult;

import java.util.Scanner;

/**
 * ConsoleUI
 * ---------
 * Manages all user interaction on the command line.
 *
 * Responsibilities:
 *   • Print the welcome banner and menu.
 *   • Read user input in a loop.
 *   • Delegate analysis to NewsAnalyzer.
 *   • Format and print the result report.
 *   • Handle graceful exit.
 *
 * This class is intentionally kept free of business logic so that
 * the detection engine can be reused with a different UI (e.g., REST API).
 */
public class ConsoleUI {

    // ANSI colour codes for terminal output (ignored on Windows cmd by default)
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String RED    = "\u001B[31m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";
    private static final String WHITE  = "\u001B[37m";

    private final NewsAnalyzer analyzer;
    private final Scanner      scanner;

    public ConsoleUI() {
        this.analyzer = new NewsAnalyzer();
        this.scanner  = new Scanner(System.in);
    }

    // ------------------------------------------------------------------ //
    //  Main interaction loop
    // ------------------------------------------------------------------ //

    /**
     * Starts the interactive CLI session.
     * Continues until the user types 'exit' or 'quit'.
     */
    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> runAnalysis();
                case "2" -> printHowItWorks();
                case "3" -> {
                    printFarewell();
                    running = false;
                }
                default  -> System.out.println(YELLOW + "  Please enter 1, 2, or 3." + RESET);
            }
        }
        scanner.close();
    }

    // ------------------------------------------------------------------ //
    //  Menu screens
    // ------------------------------------------------------------------ //

    private void printBanner() {
        System.out.println();
        System.out.println(CYAN + BOLD);
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║       F A K E   N E W S   D E T E C T O R      ║");
        System.out.println("  ║       Rule-based NLP  •  Java Edition    ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    private void printMenu() {
        System.out.println(WHITE + BOLD + "\n  ══ MAIN MENU ══" + RESET);
        System.out.println("  1. Analyze a news text");
        System.out.println("  2. How does it work?");
        System.out.println("  3. Exit");
        System.out.print(CYAN + "\n  Enter choice: " + RESET);
    }

    // ------------------------------------------------------------------ //
    //  Analysis flow
    // ------------------------------------------------------------------ //

    private void runAnalysis() {
        System.out.println(WHITE + BOLD + "\n  ── Enter News Text ──" + RESET);
        System.out.println("  (Paste a headline or paragraph, then press ENTER)");
        System.out.print(CYAN + "\n  > " + RESET);

        String input = scanner.nextLine();

        if (input.isBlank()) {
            System.out.println(YELLOW + "\n  No text entered. Returning to menu." + RESET);
            return;
        }

        try {
            AnalysisResult result = analyzer.analyze(input);
            printReport(result);
        } catch (IllegalArgumentException e) {
            System.out.println(RED + "\n  Error: " + e.getMessage() + RESET);
        }
    }

    // ------------------------------------------------------------------ //
    //  Report formatting
    // ------------------------------------------------------------------ //

    /**
     * Prints a nicely formatted analysis report to stdout.
     *
     * @param result the populated AnalysisResult from the analyzer
     */
    private void printReport(AnalysisResult result) {
        System.out.println();
        System.out.println("  ┌─────────────────────────────────────────┐");
        System.out.println("  │           ANALYSIS  REPORT               │");
        System.out.println("  └─────────────────────────────────────────┘");

        // --- Score bar ---
        int score = result.getScore();
        System.out.printf("%n  Credibility Score : %s%d / 100%s%n",
                scoreColour(score), score, RESET);
        System.out.print("  ");
        printScoreBar(score);

        // --- Classification ---
        String label = result.getClassification();
        System.out.printf("%n  Verdict           : %s%s %s%s%n%n",
                BOLD, classificationColour(label), label, RESET);

        // --- Triggered rules ---
        if (result.hasViolations()) {
            System.out.println("  Red flags detected:");
            for (String rule : result.getTriggeredRules()) {
                System.out.println("    " + rule);
            }
        } else {
            System.out.println(GREEN + "  ✓ No red flags detected – text looks credible." + RESET);
        }

        System.out.println();
        System.out.println("  ─────────────────────────────────────────");

        // --- Plain-English explanation ---
        System.out.println("\n  What this means:");
        System.out.println(classificationExplanation(label));
        System.out.println();
    }

    /**
     * Prints a simple ASCII progress bar for the score (0–100).
     */
    private void printScoreBar(int score) {
        int filled = score / 5;       // 20 segments total
        System.out.print(scoreColour(score) + "[");
        for (int i = 0; i < 20; i++) {
            System.out.print(i < filled ? "█" : "░");
        }
        System.out.println("]" + RESET);
    }

    private String scoreColour(int score) {
        if (score <= 30)  return GREEN;
        if (score <= 70)  return YELLOW;
        return RED;
    }

    private String classificationColour(String label) {
        return switch (label) {
            case "REAL"       -> GREEN;
            case "SUSPICIOUS" -> YELLOW;
            case "FAKE"       -> RED;
            default           -> WHITE;
        };
    }

    private String classificationExplanation(String label) {
        return switch (label) {
            case "REAL"       -> GREEN  + "  ✓ REAL (score 0–30): The text shows few or no signs of\n"
                                        + "    misinformation. It may still be worth verifying with\n"
                                        + "    a trusted news source before sharing." + RESET;
            case "SUSPICIOUS" -> YELLOW + "  ⚠ SUSPICIOUS (score 31–70): Some red flags were found.\n"
                                        + "    The content may be misleading or sensationalised.\n"
                                        + "    Cross-check with reputable outlets before sharing." + RESET;
            case "FAKE"       -> RED    + "  ✗ FAKE (score 71–100): Multiple strong indicators of\n"
                                        + "    misinformation detected. This text uses techniques\n"
                                        + "    commonly found in fake news. Do NOT share." + RESET;
            default           -> "  Classification unknown.";
        };
    }

    // ------------------------------------------------------------------ //
    //  Educational section
    // ------------------------------------------------------------------ //

    private void printHowItWorks() {
        System.out.println(CYAN + BOLD + "\n  ══ HOW IT WORKS ══" + RESET);
        System.out.println();
        System.out.println("  This tool uses Rule-Based Natural Language Processing (NLP).");
        System.out.println("  Instead of a machine-learning model, it applies hand-crafted");
        System.out.println("  rules that look for patterns commonly found in fake news.");
        System.out.println();
        System.out.println(BOLD + "  Detection Rules:" + RESET);
        System.out.println();
        System.out.println("  1. Suspicious Keywords   (+up to 30 pts)");
        System.out.println("     Words like BREAKING, SHOCKING, URGENT, LEAKED suggest");
        System.out.println("     emotional manipulation rather than factual reporting.");
        System.out.println();
        System.out.println("  2. ALL-CAPS Text         (+up to 25 pts)");
        System.out.println("     Shouting in capital letters is a visual urgency trick.");
        System.out.println("     Credible outlets follow style guides that restrict caps.");
        System.out.println();
        System.out.println("  3. Excessive Punctuation (+up to 20 pts)");
        System.out.println("     Multiple !!!  or ???  manufacture artificial excitement.");
        System.out.println();
        System.out.println("  4. Clickbait Phrases     (+up to 25 pts)");
        System.out.println("     'You won't believe', 'What happened next' exploit the");
        System.out.println("     curiosity gap to drive clicks without providing facts.");
        System.out.println();
        System.out.println("  5. Text Length           (+up to 10 pts)");
        System.out.println("     Very short texts lack verifiable information;");
        System.out.println("     very long texts may bury claims in noise.");
        System.out.println();
        System.out.println(BOLD + "  Scoring:" + RESET);
        System.out.println("    0 – 30  pts  →  " + GREEN  + "REAL"       + RESET);
        System.out.println("   31 – 70  pts  →  " + YELLOW + "SUSPICIOUS" + RESET);
        System.out.println("   71 – 100 pts  →  " + RED    + "FAKE"       + RESET);
        System.out.println();
    }

    private void printFarewell() {
        System.out.println(GREEN + BOLD + "\n  Thank you for using Fake News Detector!" + RESET);
        System.out.println("  Remember: always verify news with trusted sources.\n");
    }
}
