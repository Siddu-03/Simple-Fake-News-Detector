package com.fakenewsdetector.model;

import java.util.ArrayList;
import java.util.List;

/**
 * AnalysisResult
 * --------------
 * Data model that holds the complete result of analyzing a news text.
 * Stores the raw score, classification label, and the list of rule
 * violations found during analysis.
 */
public class AnalysisResult {

    /** The original news text that was analyzed. */
    private final String originalText;

    /** Credibility score in range 0–100.
     *  Higher score = more likely to be FAKE. */
    private int score;

    /**
     * Classification label based on the score:
     *   REAL       → score 0–30
     *   SUSPICIOUS → score 31–70
     *   FAKE       → score 71–100
     */
    private String classification;

    /** Human-readable list of rules that were triggered. */
    private final List<String> triggeredRules;

    // ------------------------------------------------------------------ //
    //  Constructor
    // ------------------------------------------------------------------ //

    public AnalysisResult(String originalText) {
        this.originalText   = originalText;
        this.score          = 0;
        this.classification = "REAL";
        this.triggeredRules = new ArrayList<>();
    }

    // ------------------------------------------------------------------ //
    //  Getters & Setters
    // ------------------------------------------------------------------ //

    public String getOriginalText()     { return originalText; }

    public int getScore()               { return score; }
    public void setScore(int score)     { this.score = Math.min(100, Math.max(0, score)); }

    public String getClassification()                   { return classification; }
    public void setClassification(String classification){ this.classification = classification; }

    public List<String> getTriggeredRules()             { return triggeredRules; }

    /** Adds a rule-violation description to the result. */
    public void addTriggeredRule(String ruleDescription) {
        triggeredRules.add(ruleDescription);
    }

    /** Convenience: true when at least one rule was triggered. */
    public boolean hasViolations() {
        return !triggeredRules.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("AnalysisResult{score=%d, classification='%s', rules=%s}",
                score, classification, triggeredRules);
    }
}
