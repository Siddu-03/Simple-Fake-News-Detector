package com.fakenewsdetector.analyzer;

import com.fakenewsdetector.model.AnalysisResult;
import com.fakenewsdetector.rules.RuleEngine;
import com.fakenewsdetector.scoring.ScoreCalculator;
import com.fakenewsdetector.utils.TextUtils;

/**
 * NewsAnalyzer  (v3)
 * ──────────────────
 * Orchestrates the full detection pipeline:
 *
 *   Phase 1 — Individual category rules (C01–C17)
 *             Each rule contributes its own capped penalty.
 *
 *   Phase 2 — Combination boosts
 *             Fired after all individual rules so they can inspect
 *             the co-occurrence of category signals.
 *
 *   Phase 3 — Classification
 *             ScoreCalculator maps total score → REAL / SUSPICIOUS / FAKE.
 */
public class NewsAnalyzer {

    private final RuleEngine      ruleEngine;
    private final ScoreCalculator scoreCalculator;

    public NewsAnalyzer() {
        this.ruleEngine      = new RuleEngine();
        this.scoreCalculator = new ScoreCalculator();
    }

    public AnalysisResult analyze(String rawText) {

        if (rawText == null || rawText.isBlank())
            throw new IllegalArgumentException("News text must not be empty.");

        String text        = TextUtils.normalize(rawText);
        AnalysisResult result = new AnalysisResult(text);

        // ── Phase 1: Individual category rules ───────────────────────────

        // C01 – Sensational language
        scoreCalculator.addPenalty(result,
                ruleEngine.checkSensationalLanguage(text, result));

        // C02 – Clickbait patterns
        scoreCalculator.addPenalty(result,
                ruleEngine.checkClickbaitPatterns(text, result));

        // C03 – Celebrity entities
        scoreCalculator.addPenalty(result,
                ruleEngine.checkCelebrityEntities(text, result));

        // C04 – Financial scam keywords
        scoreCalculator.addPenalty(result,
                ruleEngine.checkFinancialScamKeywords(text, result));

        // C05 – Large numbers (regex)
        scoreCalculator.addPenalty(result,
                ruleEngine.checkLargeNumbers(text, result));

        // C06 – Urgency / pressure tactics
        scoreCalculator.addPenalty(result,
                ruleEngine.checkUrgencyPressure(text, result));

        // C07 – Authority impersonation
        scoreCalculator.addPenalty(result,
                ruleEngine.checkAuthorityImpersonation(text, result));

        // C08 – Health misinformation
        scoreCalculator.addPenalty(result,
                ruleEngine.checkHealthMisinformation(text, result));

        // C09 – Emotional manipulation
        scoreCalculator.addPenalty(result,
                ruleEngine.checkEmotionalManipulation(text, result));

        // C10 – Excess punctuation (regex)
        scoreCalculator.addPenalty(result,
                ruleEngine.checkExcessPunctuation(text, result));

        // C11 – ALL-CAPS ratio
        scoreCalculator.addPenalty(result,
                ruleEngine.checkAllCaps(text, result));

        // C12 – Poor grammar signals
        scoreCalculator.addPenalty(result,
                ruleEngine.checkPoorGrammarSignals(text, result));

        // C13 – Lack of credible source
        scoreCalculator.addPenalty(result,
                ruleEngine.checkLackOfSource(text, result));

        // C14 – Fake social proof
        scoreCalculator.addPenalty(result,
                ruleEngine.checkFakeSocialProof(text, result));

        // C15 – Suspicious URLs
        scoreCalculator.addPenalty(result,
                ruleEngine.checkSuspiciousUrls(text, result));

        // C16 – Religious / superstitious hooks
        scoreCalculator.addPenalty(result,
                ruleEngine.checkReligiousEmotionalHooks(text, result));

        // C17 – Political manipulation
        scoreCalculator.addPenalty(result,
                ruleEngine.checkPoliticalManipulation(text, result));

        // ── Phase 2: Combination boosts (must run AFTER all individual rules)
        scoreCalculator.addPenalty(result,
                ruleEngine.checkCombinations(text, result));

        // ── Phase 3: Classify
        scoreCalculator.classify(result);

        return result;
    }
}
