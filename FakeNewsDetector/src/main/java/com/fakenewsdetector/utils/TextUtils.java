package com.fakenewsdetector.utils;

/**
 * TextUtils
 * ---------
 * Stateless helper methods for common text-processing operations.
 * All methods are static so no instance is needed.
 */
public class TextUtils {

    // Private constructor – utility class, not meant to be instantiated.
    private TextUtils() {}

    // ------------------------------------------------------------------ //
    //  Counting helpers
    // ------------------------------------------------------------------ //

    /**
     * Counts how many characters in the text are uppercase letters.
     *
     * @param text the input string
     * @return count of uppercase characters
     */
    public static int countUpperCaseLetters(String text) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) count++;
        }
        return count;
    }

    /**
     * Counts how many characters in the text are letters (A-Z, a-z).
     *
     * @param text the input string
     * @return count of letter characters
     */
    public static int countLetters(String text) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) count++;
        }
        return count;
    }

    /**
     * Counts occurrences of a specific character in the text.
     *
     * @param text the input string
     * @param ch   the character to count
     * @return number of times ch appears in text
     */
    public static int countCharOccurrences(String text, char ch) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    // ------------------------------------------------------------------ //
    //  Text transformation helpers
    // ------------------------------------------------------------------ //

    /**
     * Converts text to lower-case for case-insensitive keyword matching.
     *
     * @param text the input string (may be null)
     * @return lower-case version, or empty string if null
     */
    public static String toLower(String text) {
        return (text == null) ? "" : text.toLowerCase();
    }

    /**
     * Removes leading/trailing whitespace and collapses multiple internal
     * spaces into a single space.
     *
     * @param text the input string
     * @return cleaned string
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.trim().replaceAll("\\s+", " ");
    }

    // ------------------------------------------------------------------ //
    //  Ratio / percentage helpers
    // ------------------------------------------------------------------ //

    /**
     * Calculates the proportion of uppercase letters out of all letters.
     * Returns a value between 0.0 (no caps) and 1.0 (all caps).
     *
     * @param text the input string
     * @return caps ratio, or 0.0 if there are no letters
     */
    public static double upperCaseRatio(String text) {
        int letters = countLetters(text);
        if (letters == 0) return 0.0;
        return (double) countUpperCaseLetters(text) / letters;
    }

    /**
     * Checks whether the text contains a given keyword (case-insensitive).
     *
     * @param text    the input string
     * @param keyword the word/phrase to search for
     * @return true if text contains keyword (ignoring case)
     */
    public static boolean containsIgnoreCase(String text, String keyword) {
        if (text == null || keyword == null) return false;
        return text.toLowerCase().contains(keyword.toLowerCase());
    }
}
