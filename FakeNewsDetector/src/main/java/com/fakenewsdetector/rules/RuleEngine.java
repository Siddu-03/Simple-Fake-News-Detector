package com.fakenewsdetector.rules;

import com.fakenewsdetector.model.AnalysisResult;
import com.fakenewsdetector.utils.TextUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RuleEngine  (v3 — comprehensive multi-category detection)
 * ═══════════════════════════════════════════════════════════
 *
 * Architecture
 * ────────────
 * Each public check*() method represents ONE detection category.
 * Every method:
 *   1. Works on a lower-cased copy of the text for case-insensitive matching.
 *   2. Calls result.addTriggeredRule() with a human-readable flag per match.
 *   3. Returns the integer penalty for that category (internally capped).
 *
 * Regex patterns are compiled ONCE as static final constants — never inside
 * a loop — so the engine stays fast on repeated calls.
 *
 * Combination rules live in checkCombinations() and are evaluated AFTER all
 * individual categories so they can read the already-flagged state.
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │  CATEGORY                        │  MAX PTS             │
 * ├─────────────────────────────────────────────────────────┤
 * │  C01  Sensational language        │   24                 │
 * │  C02  Clickbait patterns          │   30                 │
 * │  C03  Celebrity entities          │   20                 │
 * │  C04  Financial scam keywords     │   40                 │
 * │  C05  Large numbers (regex)       │   30                 │
 * │  C06  Urgency / pressure          │   30                 │
 * │  C07  Authority impersonation     │   36                 │
 * │  C08  Health misinformation       │   40                 │
 * │  C09  Emotional manipulation      │   36                 │
 * │  C10  Excess punctuation (regex)  │   20                 │
 * │  C11  ALL-CAPS ratio              │   10                 │
 * │  C12  Poor grammar signals        │   16                 │
 * │  C13  Lack of source heuristic    │   10                 │
 * │  C14  Fake social proof           │   24                 │
 * │  C15  Suspicious URLs             │   40                 │
 * │  C16  Religious emotional hooks   │   30                 │
 * │  C17  Political manipulation      │   30                 │
 * ├─────────────────────────────────────────────────────────┤
 * │  COMBO boosts (celebrity+scam, etc.)│  up to +40        │
 * └─────────────────────────────────────────────────────────┘
 *
 * The ScoreCalculator clamps the final cumulative total to 100.
 */
public class RuleEngine {

    // ═════════════════════════════════════════════════════════════════ //
    //  Pre-compiled regex Patterns  (static = compiled at class load)
    // ═════════════════════════════════════════════════════════════════ //

    /** 3+ digit numbers, OR  N  million / billion / crore / lakh / trillion */
    private static final Pattern P_LARGE_NUMBER = Pattern.compile(
            "\\b(\\d{3,}|\\d+\\s*(million|billion|crore|lakh|trillion))\\b",
            Pattern.CASE_INSENSITIVE);

    /** Two or more identical punctuation chars: !!  ???  ...  !?! */
    private static final Pattern P_EXCESS_PUNCT = Pattern.compile(
            "[!]{2,}|[?]{2,}|\\.{3,}");

    /** Numeric amount immediately followed by a crypto token */
    private static final Pattern P_CRYPTO_AMOUNT = Pattern.compile(
            "\\d+\\s*(bitcoin|btc|ethereum|eth|crypto|coin|token|nft|usdt)",
            Pattern.CASE_INSENSITIVE);

    /** Suspicious short-link / TLD patterns in text */
    private static final Pattern P_SUSPICIOUS_URL = Pattern.compile(
            "https?://[^\\s]*\\.(?:xyz|click|top|buzz|tk|ml|ga|cf)[^\\s]*"
            + "|bit\\.ly/[^\\s]+"
            + "|tinyurl\\.com/[^\\s]+"
            + "|short\\.link/[^\\s]+",
            Pattern.CASE_INSENSITIVE);

    /** Repeated word heuristic: same word appearing twice in a row */
    private static final Pattern P_REPEATED_WORD = Pattern.compile(
            "\\b(\\w{3,})\\s+\\1\\b",
            Pattern.CASE_INSENSITIVE);

    // ═════════════════════════════════════════════════════════════════ //
    //  C01 — Sensational language
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Alarm / hype words designed to trigger an emotional "share" reflex.
     * Each match: +12 pts.  Category cap: 24 pts.
     */
    public int checkSensationalLanguage(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "breaking news", "breaking:", "massive news", "huge announcement",
                "this just happened", "everyone is talking about", "big reveal",
                "must share", "you won't believe", "viral", "alert:",
                "breaking", "shocking", "urgent", "exclusive", "unbelievable"
        );
        return matchKeywords(text, result, patterns, 12, 24,
                "⚡ Sensational language");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C02 — Clickbait patterns
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Curiosity-gap formulas that tease a reveal without delivering facts.
     * Each match: +15 pts.  Category cap: 30 pts.
     */
    public int checkClickbaitPatterns(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "what happened next", "you won't believe", "this will shock you",
                "number 1 secret", "top secret", "hidden truth",
                "doctors hate this", "doctors hate", "experts stunned",
                "watch till the end", "read before it gets deleted",
                "one weird trick", "find out why", "going viral",
                "jaw-dropping", "this is insane"
        );
        return matchKeywords(text, result, patterns, 15, 30,
                "🎣 Clickbait pattern");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C03 — Celebrity entities
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Named celebrities are frequently impersonated in scams.
     * Appearance alone: +10 pts.  Category cap: 20 pts.
     * (Higher scores come from combination rules when combined with scam signals.)
     */
    public int checkCelebrityEntities(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "elon musk", "bill gates", "jeff bezos", "mark zuckerberg",
                "ambani", "adani", "mukesh ambani", "gautam adani",
                "narendra modi", "donald trump", "joe biden", "barack obama",
                "virat kohli", "taylor swift", "warren buffett",
                "jack dorsey", "vitalik buterin", "sam altman", "tim cook",
                "sundar pichai", "jensen huang"
        );
        return matchKeywords(text, result, patterns, 10, 20,
                "👤 Celebrity entity");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C04 — Financial scam keywords
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Keywords used to lure victims into financial fraud.
     * Each match: +20 pts.  Category cap: 40 pts.
     */
    public int checkFinancialScamKeywords(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "free money", "giveaway", "crypto giveaway",
                "bitcoin giveaway", "ethereum giveaway",
                "win money", "earn instantly", "double your money",
                "investment scheme", "airdrop", "profit guaranteed",
                "risk-free investment", "get rich quick",
                "financial freedom", "passive income guaranteed",
                "claim your reward", "cryptocurrency reward",
                "gives bitcoin", "gives ethereum", "gives crypto",
                "sending bitcoin", "sending ethereum",
                "claim now", "you have won", "you won",
                "congratulations you", "you are selected",
                "winner selected", "prize money"
        );
        return matchKeywords(text, result, patterns, 20, 40,
                "💸 Financial scam keyword");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C05 — Large numbers  (REGEX)
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Phantom large sums: "2000 bitcoins", "1 crore rupees", "₹50 lakh".
     * First match: +15 pts; each extra match: +5 pts.  Cap: 30 pts.
     */
    public int checkLargeNumbers(String text, AnalysisResult result) {
        Matcher m = P_LARGE_NUMBER.matcher(text);
        int count = 0;
        while (m.find()) {
            count++;
            if (count == 1)
                result.addTriggeredRule("🔢 Large number: \"" + m.group().trim() + "\"");
        }
        if (count == 0) return 0;
        if (count > 1)
            result.addTriggeredRule("🔢 Multiple large numbers (" + count + " occurrences)");

        // Crypto-specific amounts get flagged too
        Matcher cm = P_CRYPTO_AMOUNT.matcher(TextUtils.toLower(text));
        if (cm.find())
            result.addTriggeredRule("💰 Specific crypto amount: \"" + cm.group().trim() + "\"");

        return Math.min(15 + (count - 1) * 5, 30);
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C06 — Urgency / pressure tactics
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * FOMO-inducing time pressure.  Reliable reporting never demands speed.
     * Each match: +15 pts.  Category cap: 30 pts.
     */
    public int checkUrgencyPressure(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "act now", "act fast", "limited time", "only today",
                "last chance", "offer ends soon", "hurry up",
                "immediately", "right now", "don't wait",
                "expires tonight", "before it's too late",
                "before it's deleted", "limited offer",
                "only a few spots left", "for a limited time only",
                "click now", "click here", "register now",
                "sign up now", "verify now", "respond now"
        );
        return matchKeywords(text, result, patterns, 15, 30,
                "⏰ Urgency pressure tactic");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C07 — Authority impersonation
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Fake authoritative sources to add false legitimacy.
     * Each match: +18 pts.  Category cap: 36 pts.
     */
    public int checkAuthorityImpersonation(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "government approved", "government confirmed",
                "official notice", "official announcement",
                "central bank", "rbi approved", "rbi says",
                "who says", "who confirms", "world health organization says",
                "police warning", "police alert",
                "ministry alert", "ministry of",
                "un report", "united nations",
                "breaking from authorities", "verified by government",
                "as per official sources", "sources close to the government"
        );
        return matchKeywords(text, result, patterns, 18, 36,
                "🏛 Authority impersonation");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C08 — Health misinformation
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * False medical claims cause direct physical harm.  Any miracle-language
     * around health is a strong red flag.
     * Each match: +20 pts.  Category cap: 40 pts.
     */
    public int checkHealthMisinformation(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "miracle cure", "100% cure", "100 percent cure",
                "no side effects", "zero side effects",
                "instant weight loss", "lose weight overnight",
                "secret remedy", "secret cure",
                "doctor recommends this trick", "doctors recommend this",
                "natural cure", "all-natural cure",
                "cure cancer", "cancer cured",
                "cure diabetes", "diabetes cured",
                "cure covid", "big pharma doesn't want",
                "cure all diseases", "reverse aging overnight",
                "doctors hate this remedy"
        );
        return matchKeywords(text, result, patterns, 20, 40,
                "🏥 Health misinformation");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C09 — Emotional manipulation
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Explicit calls to bypass critical thinking and share emotionally.
     * Each match: +18 pts.  Category cap: 36 pts.
     */
    public int checkEmotionalManipulation(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "share this before it's deleted",
                "share before deleted",
                "they don't want you to know",
                "hidden truth revealed",
                "spread this message",
                "save lives by sharing",
                "everyone must know this",
                "mainstream media won't show",
                "media is hiding this",
                "wake up people",
                "open your eyes",
                "this is being suppressed",
                "share with everyone you know",
                "forward to all contacts"
        );
        return matchKeywords(text, result, patterns, 18, 36,
                "😡 Emotional manipulation");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C10 — Excess punctuation  (REGEX)
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Typographic screaming: !!!, ???, ...  (ellipsis abuse also counted).
     * Each run: +10 pts.  Category cap: 20 pts.
     */
    public int checkExcessPunctuation(String text, AnalysisResult result) {
        Matcher m = P_EXCESS_PUNCT.matcher(text);
        int penalty = 0, count = 0;
        while (m.find()) {
            count++;
            if (count <= 2)
                result.addTriggeredRule("❗ Excess punctuation: \"" + m.group() + "\"");
            penalty += 10;
        }
        if (count > 2)
            result.addTriggeredRule("❗ " + count + " excess punctuation runs total");
        return Math.min(penalty, 20);
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C11 — ALL-CAPS ratio
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * >40 % uppercase letters = shouting / panic inducing.
     * 40–70 %: +10 pts.  >70 %: +10 pts (same cap, severity flagged).
     */
    public int checkAllCaps(String text, AnalysisResult result) {
        double ratio = TextUtils.upperCaseRatio(text);
        if (ratio >= 0.70) {
            result.addTriggeredRule(
                    String.format("🔠 Extreme ALL-CAPS (%.0f%% uppercase)", ratio * 100));
            return 10;
        }
        if (ratio >= 0.40) {
            result.addTriggeredRule(
                    String.format("🔠 High ALL-CAPS (%.0f%% uppercase)", ratio * 100));
            return 10;
        }
        return 0;
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C12 — Poor grammar / structural signals
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Automated or hastily fabricated content often shows repeated words,
     * missing punctuation at sentence end, or random capitalization mid-word.
     * Each signal: +8 pts.  Category cap: 16 pts.
     */
    public int checkPoorGrammarSignals(String text, AnalysisResult result) {
        int penalty = 0;
        String norm = TextUtils.normalize(text);

        // Signal A: repeated consecutive words (regex)
        if (P_REPEATED_WORD.matcher(norm).find()) {
            result.addTriggeredRule("📝 Repeated words detected (grammar anomaly)");
            penalty += 8;
        }

        // Signal B: no sentence-ending punctuation at all in a longish text
        boolean longEnough = norm.length() > 60;
        boolean noEndPunct = !norm.matches(".*[.!?]\\s*$");
        if (longEnough && noEndPunct) {
            result.addTriggeredRule("📝 No terminal punctuation (grammar anomaly)");
            penalty += 8;
        }

        return Math.min(penalty, 16);
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C13 — Lack of credible source heuristic
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Real news almost always cites a publication, organisation, or named
     * spokesperson.  Text that contains big claims but no attribution is
     * suspicious.  +10 pts if no source indicator is found.
     *
     * Source indicators: "according to", "says", "reports", "published",
     * a quoted title (Reuters, BBC, AP, PTI, Hindu, TOI, …).
     */
    public int checkLackOfSource(String text, AnalysisResult result) {
        String lower = TextUtils.toLower(text);

        // Presence of any credible attribution token clears this rule
        List<String> sourceIndicators = List.of(
                "according to", "reports say", "as per", "said in a statement",
                "told reporters", "confirmed by", "published by",
                "reuters", "ap news", "bbc", "pti", "ani", "ndtv",
                "the hindu", "times of india", "mint", "bloomberg",
                "associated press", "per the study", "the report says"
        );

        boolean hasSource = sourceIndicators.stream().anyMatch(lower::contains);
        if (!hasSource && text.length() > 40) {
            result.addTriggeredRule("📰 No credible source or attribution found");
            return 10;
        }
        return 0;
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C14 — Fake social proof
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Invented crowd-consensus signals to trigger herd behaviour.
     * Each match: +12 pts.  Category cap: 24 pts.
     */
    public int checkFakeSocialProof(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "millions are using this", "millions are doing this",
                "everyone is investing", "people are earning daily",
                "viral worldwide", "trending everywhere",
                "thousands have already", "100,000 people",
                "join the millions", "everyone is talking about",
                "millions have already tried", "globally trending"
        );
        return matchKeywords(text, result, patterns, 12, 24,
                "👥 Fake social proof");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C15 — Suspicious URLs
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Known short-link services and shady TLDs used in scam distribution.
     * Regex match: +20 pts each.  Category cap: 40 pts.
     *
     * Also catches plain-text mentions like ".xyz" or "bit.ly" without a
     * full URL, since fake-news forwards often drop the scheme.
     */
    public int checkSuspiciousUrls(String text, AnalysisResult result) {
        // Regex check for full URLs with suspicious TLD / short-linkers
        int penalty = 0;
        Matcher m = P_SUSPICIOUS_URL.matcher(text);
        while (m.find()) {
            result.addTriggeredRule("🔗 Suspicious URL detected: \"" + m.group() + "\"");
            penalty += 20;
        }

        // Plain-text TLD / shortener mentions (no scheme required)
        List<String> tldMentions = List.of(
                ".xyz", ".click", ".top", ".buzz", ".tk",
                "bit.ly", "tinyurl", "short.link", "rb.gy", "t.ly"
        );
        String lower = TextUtils.toLower(text);
        for (String tld : tldMentions) {
            if (lower.contains(tld)) {
                result.addTriggeredRule("🔗 Suspicious domain/shortener mention: \"" + tld + "\"");
                penalty += 20;
            }
        }

        return Math.min(penalty, 40);
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C16 — Religious / superstitious emotional hooks
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Chain-letter style coercion using religious or superstitious framing.
     * Each match: +15 pts.  Category cap: 30 pts.
     */
    public int checkReligiousEmotionalHooks(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "god wants you to share", "god wants you",
                "blessing will come", "blessings await",
                "ignore and bad luck", "ignore at your own risk",
                "forward this message", "forward to 10 people",
                "send to all contacts", "a miracle will happen",
                "if you believe share", "share to receive blessings",
                "devil's work exposed", "prayer will protect you"
        );
        return matchKeywords(text, result, patterns, 15, 30,
                "🙏 Religious/superstitious hook");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  C17 — Political manipulation
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Conspiratorial framing designed to undermine trust in institutions.
     * Each match: +15 pts.  Category cap: 30 pts.
     */
    public int checkPoliticalManipulation(String text, AnalysisResult result) {
        List<String> patterns = List.of(
                "media won't show this", "media is hiding this",
                "hidden agenda", "deep state",
                "election scam", "election rigged",
                "corruption exposed", "secret political plan",
                "secret plan", "fake election", "government conspiracy",
                "politicians don't want you to know",
                "shadow government", "new world order",
                "mainstream media lies", "globalist agenda",
                "exposed corruption", "political scandal hidden"
        );
        return matchKeywords(text, result, patterns, 15, 30,
                "🗳 Political manipulation");
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  COMBINATION RULES — evaluated after all individual categories
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Combination boosts fire when multiple category signals co-occur,
     * reflecting the multiplicative danger of combined fake-news patterns.
     *
     * Each combination is only fired ONCE per analysis, and only when
     * the constituent rules have ALREADY produced at least one flag each.
     * The boost is an ADDITIONAL addPenalty() call in NewsAnalyzer.
     *
     * @return total additional boost penalty
     */
    public int checkCombinations(String text, AnalysisResult result) {
        String lower = TextUtils.toLower(text);
        int boost = 0;

        // ── Combo 1: Celebrity + Financial scam + Large number → +40
        boolean hasCelebrity  = hasCelebritySignal(lower);
        boolean hasFinScam    = hasFinancialScamSignal(lower);
        boolean hasLargeNum   = P_LARGE_NUMBER.matcher(text).find()
                             || P_CRYPTO_AMOUNT.matcher(lower).find();

        if (hasCelebrity && hasFinScam && hasLargeNum) {
            result.addTriggeredRule(
                    "🚨 COMBO: Celebrity + Financial scam + Large number → +40");
            boost += 40;
        }

        // ── Combo 2: Clickbait + Urgency + Excess punctuation → +25
        boolean hasClickbait  = hasClickbaitSignal(lower);
        boolean hasUrgency    = hasUrgencySignal(lower);
        boolean hasExcessPunct = P_EXCESS_PUNCT.matcher(text).find();

        if (hasClickbait && hasUrgency && hasExcessPunct) {
            result.addTriggeredRule(
                    "🚨 COMBO: Clickbait + Urgency + Excess punctuation → +25");
            boost += 25;
        }

        // ── Combo 3: Health misinformation + Miracle language + No source → +30
        boolean hasHealthMisinfo = hasHealthSignal(lower);
        boolean hasMiracle       = lower.contains("miracle") || lower.contains("secret cure")
                                || lower.contains("100% cure");
        // No-source already flagged by C13; check absence of source indicators here
        boolean noSource = !hasSourceIndicator(lower);

        if (hasHealthMisinfo && hasMiracle && noSource) {
            result.addTriggeredRule(
                    "🚨 COMBO: Health misinformation + Miracle claim + No source → +30");
            boost += 30;
        }

        // ── Combo 4: Authority impersonation + Urgency + Emotional manipulation → +25
        boolean hasAuthority  = hasAuthoritySignal(lower);
        boolean hasEmotional  = hasEmotionalSignal(lower);

        if (hasAuthority && hasUrgency && hasEmotional) {
            result.addTriggeredRule(
                    "🚨 COMBO: Authority impersonation + Urgency + Emotional manipulation → +25");
            boost += 25;
        }

        // ── Combo 5: Short text + High claim + Large number → +20
        String norm  = TextUtils.normalize(text);
        boolean isShort    = norm.length() <= 120;
        boolean highClaim  = hasCelebrity || hasHealthMisinfo || hasAuthority;
        if (isShort && highClaim && hasLargeNum) {
            result.addTriggeredRule(
                    "🚨 COMBO: Short headline + High claim + Large number → +20");
            boost += 20;
        }

        // ── Combo 6: Prize/lottery + large number + urgency → +25
        boolean hasPrizeLang = lower.contains("you won") || lower.contains("you have won")
                            || lower.contains("prize") || lower.contains("winner")
                            || lower.contains("lottery") || lower.contains("rupees")
                            || lower.contains("reward");
        if (hasPrizeLang && hasLargeNum && hasUrgency) {
            result.addTriggeredRule(
                    "🚨 COMBO: Prize/lottery claim + Large number + Urgency → +25");
            boost += 25;
        }

        // ── Combo 7: Religious hook + forward/share directive → chain-letter scam +20
        boolean hasReligiousHook = lower.contains("god wants") || lower.contains("blessing")
                                || lower.contains("bad luck") || lower.contains("miracle will")
                                || lower.contains("forward this") || lower.contains("forward to");
        boolean hasShareDirective = lower.contains("forward") || lower.contains("send to all")
                                 || lower.contains("share") || lower.contains("pass this");
        if (hasReligiousHook && hasShareDirective) {
            result.addTriggeredRule(
                    "🚨 COMBO: Religious manipulation + Share directive (chain-letter pattern) → +20");
            boost += 20;
        }

        // ── Combo 8: Political suppression narrative (media hiding + scandal) → +20
        boolean hasMediaSuppression = lower.contains("media won't") || lower.contains("media is hiding")
                                   || lower.contains("mainstream media") || lower.contains("they don't want");
        boolean hasPoliticalScandal = lower.contains("election scam") || lower.contains("corruption exposed")
                                   || lower.contains("secret plan") || lower.contains("hidden agenda")
                                   || lower.contains("election rigged") || lower.contains("exposed");
        if (hasMediaSuppression && hasPoliticalScandal) {
            result.addTriggeredRule(
                    "🚨 COMBO: Media suppression narrative + Political scandal claim → +20");
            boost += 20;
        }

        return boost;
    }

    // ═════════════════════════════════════════════════════════════════ //
    //  Private helpers
    // ═════════════════════════════════════════════════════════════════ //

    /**
     * Generic keyword matcher used by most category methods.
     *
     * @param text       original text (will be lower-cased internally)
     * @param result     AnalysisResult to append flags to
     * @param patterns   list of keywords/phrases to search for
     * @param pointsPer  score added per matched pattern
     * @param cap        maximum score this category can contribute
     * @param prefix     human-readable category label for the flag message
     * @return total penalty for this category (≤ cap)
     */
    private int matchKeywords(String text, AnalysisResult result,
                              List<String> patterns, int pointsPer, int cap,
                              String prefix) {
        String lower = TextUtils.toLower(text);
        int penalty = 0;
        for (String p : patterns) {
            if (lower.contains(p)) {
                result.addTriggeredRule(prefix + ": \"" + p + "\"");
                penalty += pointsPer;
                if (penalty >= cap) break;   // already at cap — stop early
            }
        }
        return Math.min(penalty, cap);
    }

    // Lightweight signal detectors used by combination rules
    // (avoid re-running full matchKeywords loops for performance)

    private boolean hasCelebritySignal(String lower) {
        for (String c : List.of("elon musk","bill gates","jeff bezos","mark zuckerberg",
                "ambani","adani","modi","donald trump","virat kohli","taylor swift",
                "obama","biden","jack dorsey","vitalik","tim cook","sundar pichai"))
            if (lower.contains(c)) return true;
        return false;
    }

    private boolean hasFinancialScamSignal(String lower) {
        for (String f : List.of("free money","giveaway","bitcoin","ethereum","crypto",
                "win money","double your money","profit guaranteed","get rich quick",
                "gives bitcoin","gives crypto","sending bitcoin","airdrop"))
            if (lower.contains(f)) return true;
        return false;
    }

    private boolean hasClickbaitSignal(String lower) {
        for (String cb : List.of("you won't believe","what happened next","doctors hate",
                "experts stunned","hidden truth","top secret","this will shock you"))
            if (lower.contains(cb)) return true;
        return false;
    }

    private boolean hasUrgencySignal(String lower) {
        for (String u : List.of("act now","limited time","last chance","hurry up",
                "immediately","right now","before it's deleted","offer ends soon"))
            if (lower.contains(u)) return true;
        return false;
    }

    private boolean hasHealthSignal(String lower) {
        for (String h : List.of("miracle cure","100% cure","no side effects",
                "cure cancer","cure diabetes","natural cure","secret remedy",
                "instant weight loss","big pharma","doctors hate this remedy"))
            if (lower.contains(h)) return true;
        return false;
    }

    private boolean hasAuthoritySignal(String lower) {
        for (String a : List.of("government approved","official notice","central bank",
                "rbi","who says","police warning","ministry alert","un report",
                "breaking from authorities"))
            if (lower.contains(a)) return true;
        return false;
    }

    private boolean hasEmotionalSignal(String lower) {
        for (String e : List.of("share before deleted","they don't want you to know",
                "spread this message","everyone must know","media is hiding",
                "wake up people","this is being suppressed"))
            if (lower.contains(e)) return true;
        return false;
    }

    private boolean hasSourceIndicator(String lower) {
        for (String s : List.of("according to","reports say","as per","reuters","ap news",
                "bbc","pti","ani","ndtv","bloomberg","the hindu","times of india",
                "confirmed by","published by","per the study","said in a statement"))
            if (lower.contains(s)) return true;
        return false;
    }

    // ── Legacy alias kept for backward compatibility ─────────────────
    /** @deprecated Use checkSensationalLanguage() — renamed in v3. */
    @Deprecated public int checkSensationalKeywords(String t, AnalysisResult r) {
        return checkSensationalLanguage(t, r);
    }
    /** @deprecated Use checkClickbaitPatterns() — renamed in v3. */
    @Deprecated public int checkClickbaitPhrases(String t, AnalysisResult r) {
        return checkClickbaitPatterns(t, r);
    }
    /** @deprecated Merged into checkCelebrityEntities + checkFinancialScamKeywords + combos. */
    @Deprecated public int checkCelebrityCryptoScam(String t, AnalysisResult r) {
        return checkCelebrityEntities(t, r) + checkFinancialScamKeywords(t, r);
    }
    /** @deprecated Use checkLargeNumbers(). */
    @Deprecated public int checkLargeMoneyNumbers(String t, AnalysisResult r) {
        return checkLargeNumbers(t, r);
    }
    /** @deprecated Use checkExcessPunctuation(). */
    @Deprecated public int checkExcessivePunctuation(String t, AnalysisResult r) {
        return checkExcessPunctuation(t, r);
    }
    /** @deprecated Use checkUrgencyPressure(). */
    @Deprecated public int checkScamPhrases(String t, AnalysisResult r) {
        return checkUrgencyPressure(t, r);
    }
    /** @deprecated Use checkCombinations(). */
    @Deprecated public int checkShortBigClaim(String t, AnalysisResult r) {
        return 0; // now handled inside checkCombinations()
    }
}
