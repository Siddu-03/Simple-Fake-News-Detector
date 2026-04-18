package com.fakenewsdetector.scoring;

import com.fakenewsdetector.model.AnalysisResult;

/**
 * ScoreCalculator
 * ---------------
 * Responsible for two tasks:
 *   1. Accumulating the raw penalty scores produced by each rule.
 *   2. Mapping the final score to a human-readable classification label.
 *
 * Scoring system  (v2 — tightened thresholds)
 * ─────────────────────────────────────────────
 *   Score  0 – 30  →  REAL        (credible, few or no red flags)
 *   Score 31 – 60  →  SUSPICIOUS  (notable red flags, verify before sharing)
 *   Score 61 – 100 →  FAKE        (strong misinformation indicators)
 *
 * Thresholds were lowered from the original (31-70 → 31-60 for SUSPICIOUS,
 * 71-100 → 61-100 for FAKE) because the v2 RuleEngine's new rules —
 * particularly the celebrity-crypto combo (+35) and short-big-claim (+15)
 * — produce higher raw scores for clearly fake content, so the classifier
 * boundary needed to move accordingly.
 *
 * The score is stored on the AnalysisResult so all components read from
 * a single source of truth.
 */
public class ScoreCalculator {

    // Classification thresholds — adjust here to recalibrate the whole system
    private static final int REAL_MAX        = 30;
    private static final int SUSPICIOUS_MAX  = 60;   // tightened from 70

    /**
     * Adds a penalty produced by one rule to the running total in result.
     * The value is clamped so the score never exceeds 100.
     *
     * @param result  the current analysis result object
     * @param penalty points to add (must be ≥ 0)
     */
    public void addPenalty(AnalysisResult result, int penalty) {
        if (penalty <= 0) return;          // Ignore zero / negative penalties
        int newScore = result.getScore() + penalty;
        result.setScore(Math.min(newScore, 100));   // AnalysisResult also clamps, but belt-and-braces
    }

    /**
     * Determines the classification label from the final score and writes
     * it back into the result object.
     *
     * Call this once after all rules have been applied.
     *
     * @param result the analysis result (score must already be set)
     */
    public void classify(AnalysisResult result) {
        int score = result.getScore();

        if (score <= REAL_MAX) {
            result.setClassification("REAL");
        } else if (score <= SUSPICIOUS_MAX) {
            result.setClassification("SUSPICIOUS");
        } else {
            result.setClassification("FAKE");
        }
    }
}
