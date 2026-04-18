# Fake News Detector — Java CLI

A fully rule-based NLP system that analyses news text and classifies it as
**REAL**, **SUSPICIOUS**, or **FAKE** using a transparent scoring system.
Built entirely with standard Java libraries — no external ML dependencies.

---

## Project Structure

```
FakeNewsDetector/
├── pom.xml
└── src/main/java/com/fakenewsdetector/
    ├── app/
    │   └── Main.java              ← Entry point
    ├── analyzer/
    │   └── NewsAnalyzer.java      ← Detection pipeline coordinator
    ├── rules/
    │   └── RuleEngine.java        ← All 5 detection rules
    ├── scoring/
    │   └── ScoreCalculator.java   ← Score accumulation & classification
    ├── model/
    │   └── AnalysisResult.java    ← Result data model
    ├── ui/
    │   └── ConsoleUI.java         ← CLI menu, input, formatted output
    └── utils/
        └── TextUtils.java         ← Stateless text-processing helpers
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17 or higher |
| Maven | 3.6 or higher |

---

## How to Build & Run

### Option A — Maven (recommended)

```bash
# 1. Navigate into the project folder
cd FakeNewsDetector

# 2. Compile and package
mvn package

# 3. Run the JAR
java -jar target/FakeNewsDetector.jar
```

### Option B — Manual javac

```bash
# 1. Create output directory
mkdir -p target/classes

# 2. Compile all sources (order matters for dependencies)
javac -d target/classes \
  src/main/java/com/fakenewsdetector/model/AnalysisResult.java \
  src/main/java/com/fakenewsdetector/utils/TextUtils.java \
  src/main/java/com/fakenewsdetector/rules/RuleEngine.java \
  src/main/java/com/fakenewsdetector/scoring/ScoreCalculator.java \
  src/main/java/com/fakenewsdetector/analyzer/NewsAnalyzer.java \
  src/main/java/com/fakenewsdetector/ui/ConsoleUI.java \
  src/main/java/com/fakenewsdetector/app/Main.java

# 3. Run
java -cp target/classes com.fakenewsdetector.app.Main
```

---

## Sample Outputs

### Example 1 — FAKE (score 65/100)
```
Input: SHOCKING!!! You won't believe what doctors HATE about this leaked secret!!!
       MUST SHARE before deleted!!!

  Credibility Score : 65 / 100
  [█████████████░░░░░░░]

  Verdict : SUSPICIOUS

  Red flags detected:
    ⚠ Suspicious keyword detected: "shocking"
    ⚠ Suspicious keyword detected: "must share"
    ⚠ Suspicious keyword detected: "secret"
    ⚠ Suspicious keyword detected: "share before deleted"
    ⚠ Suspicious keyword detected: "leaked"
    ❗ Excessive exclamation marks detected (9 found)
    🎣 Clickbait phrase detected: "you won't believe"
    🎣 Clickbait phrase detected: "doctors hate"
```

### Example 2 — FAKE (score 100/100)
```
Input: BREAKING!!! SHOCKING GOVERNMENT EXPOSED!!! YOU WON'T BELIEVE THIS BOMBSHELL LEAKED SECRET!!!
       WAKE UP SHEEPLE!!! SHARE BEFORE DELETED!!!

  Credibility Score : 100 / 100
  [████████████████████]

  Verdict : FAKE
```

### Example 3 — REAL (score 0/100)
```
Input: The Reserve Bank of India kept interest rates unchanged at its
       quarterly policy meeting, citing stable inflation data.

  Credibility Score : 0 / 100
  [░░░░░░░░░░░░░░░░░░░░]

  Verdict : REAL

  ✓ No red flags detected – text looks credible.
```

---

## Detection Rules

| Rule | Max Penalty | Trigger |
|------|------------|---------|
| Suspicious Keywords | +30 pts | Words like BREAKING, SHOCKING, LEAKED, URGENT, BOMBSHELL |
| ALL-CAPS Usage | +25 pts | >70 % uppercase letters = extreme; >40 % = high |
| Excessive Punctuation | +20 pts | 2+ exclamation marks OR 2+ question marks |
| Clickbait Phrases | +25 pts | "you won't believe", "doctors hate", "what happened next" … |
| Text Length Anomaly | +10 pts | Fewer than 20 chars (no detail) or more than 1000 chars |

## Scoring

```
 0 – 30   →  REAL        (green)
31 – 70   →  SUSPICIOUS  (yellow)
71 – 100  →  FAKE        (red)
```

---

## Why Rule-Based NLP?

Machine-learning models need large labelled datasets and significant compute.
Rule-based systems are:

- **Transparent** — you can read exactly why a text was flagged.
- **Explainable** — ideal for academic presentations.
- **Lightweight** — runs on any JVM with no dependencies.

Real fake news consistently exploits emotional language patterns (alarm words,
shouting in caps, manufactured urgency) that are easy to detect with string
rules, making this a perfect first step before exploring ML approaches such as
TF-IDF + Naive Bayes or BERT fine-tuning.

---

## Extending the Project

Ideas for further development:

1. **Add more rules** — URL shortener detection, missing author byline, no date.
2. **Add a source reputation list** — known satirical sites score higher.
3. **REST API layer** — expose `NewsAnalyzer` via Spring Boot.
4. **ML upgrade** — replace `RuleEngine` with a trained classifier while keeping
   the same `AnalysisResult` / `ScoreCalculator` interface.
5. **Unit tests** — add JUnit 5 tests in `src/test/java/`.
