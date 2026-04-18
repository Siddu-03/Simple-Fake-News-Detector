package com.fakenewsdetector.ui;

import com.fakenewsdetector.analyzer.NewsAnalyzer;
import com.fakenewsdetector.model.AnalysisResult;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * MainWindow
 * ----------
 * Swing GUI for the Fake News Detector.
 *
 * Aesthetic direction: "Dark Investigative Newsroom"
 *   - Deep charcoal background with off-white ink typography
 *   - Accent colour switches dynamically: green (REAL), amber (SUSPICIOUS), red (FAKE)
 *   - Monospaced body font for a forensic / terminal feel
 *   - Custom-painted score bar that fills with the verdict colour
 *   - Subtle panel borders that evoke a dossier / evidence folder look
 *
 * Integration: drop this file into the existing project — it only
 * depends on NewsAnalyzer and AnalysisResult which are already present.
 */
public class MainWindow extends JFrame {

    // ── Palette ─────────────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(18,  20,  24);
    private static final Color BG_PANEL      = new Color(26,  29,  36);
    private static final Color BG_INPUT      = new Color(32,  36,  44);
    private static final Color BORDER_SUBTLE = new Color(55,  60,  72);
    private static final Color TEXT_PRIMARY  = new Color(220, 220, 215);
    private static final Color TEXT_DIM      = new Color(130, 135, 145);
    private static final Color ACCENT_REAL   = new Color( 72, 199, 142);   // emerald
    private static final Color ACCENT_SUSP   = new Color(255, 183,  77);   // amber
    private static final Color ACCENT_FAKE   = new Color(255,  82,  82);   // crimson
    private static final Color ACCENT_BTN    = new Color( 99, 132, 255);   // electric blue

    // ── Fonts ────────────────────────────────────────────────────────────
    private static final Font FONT_MONO_LG   = new Font("Monospaced", Font.BOLD,  15);
    private static final Font FONT_MONO_MD   = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font FONT_MONO_SM   = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font FONT_SANS_LG   = new Font("SansSerif",  Font.BOLD,  22);
    private static final Font FONT_SANS_SM   = new Font("SansSerif",  Font.PLAIN, 12);

    // ── Backend ──────────────────────────────────────────────────────────
    private final NewsAnalyzer analyzer = new NewsAnalyzer();

    // ── UI Components ────────────────────────────────────────────────────
    private JTextArea   inputArea;
    private JButton     analyzeButton;
    private JLabel      verdictLabel;
    private JLabel      scoreValueLabel;
    private ScoreBar    scoreBar;
    private JTextPane   reasonsPane;
    private JLabel      charCountLabel;
    private JPanel      resultCard;

    // ═══════════════════════════════════════════════════════════════════ //
    //  Constructor
    // ═══════════════════════════════════════════════════════════════════ //

    public MainWindow() {
        super("Fake News Detector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 620));
        setPreferredSize(new Dimension(860, 700));

        applyDarkLookAndFeel();
        buildUI();
        pack();
        setLocationRelativeTo(null);   // centre on screen
    }

    // ── Look & feel ──────────────────────────────────────────────────────

    private void applyDarkLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global overrides so every default component picks up dark colours
        UIManager.put("Panel.background",          BG_DARK);
        UIManager.put("ScrollPane.background",     BG_DARK);
        UIManager.put("Viewport.background",       BG_INPUT);
        UIManager.put("TextArea.background",       BG_INPUT);
        UIManager.put("TextArea.foreground",       TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",  ACCENT_BTN);
        UIManager.put("TextPane.background",       BG_PANEL);
        UIManager.put("TextPane.foreground",       TEXT_PRIMARY);
        UIManager.put("ScrollBar.background",      BG_DARK);
        UIManager.put("ScrollBar.thumb",           BORDER_SUBTLE);
        UIManager.put("ScrollBar.track",           BG_DARK);
        UIManager.put("Button.focus",              new Color(0,0,0,0));
    }

    // ═══════════════════════════════════════════════════════════════════ //
    //  UI Construction
    // ═══════════════════════════════════════════════════════════════════ //

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Header ───────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        // Logo / title
        JLabel title = new JLabel("FAKE NEWS DETECTOR");
        title.setFont(FONT_SANS_LG);
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Rule-based NLP analysis engine  •  Java Edition");
        subtitle.setFont(FONT_SANS_SM);
        subtitle.setForeground(TEXT_DIM);

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setBackground(BG_DARK);
        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(3));
        titleGroup.add(subtitle);

        // Thin accent line on the left of the title
        JPanel accent = new JPanel();
        accent.setBackground(ACCENT_BTN);
        accent.setPreferredSize(new Dimension(4, 42));
        accent.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(BG_DARK);
        left.add(accent);
        left.add(titleGroup);

        header.add(left, BorderLayout.WEST);

        // Horizontal separator
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_SUBTLE);
        sep.setBackground(BORDER_SUBTLE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_DARK);
        wrapper.add(header,   BorderLayout.NORTH);
        wrapper.add(sep,      BorderLayout.SOUTH);
        return wrapper;
    }

    // ── Centre: input + results ──────────────────────────────────────────

    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                buildInputPanel(),
                buildResultPanel());
        split.setDividerLocation(240);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(BG_DARK);
        split.setContinuousLayout(true);
        return split;
    }

    // ── Input panel ──────────────────────────────────────────────────────

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));

        // Section label
        JLabel sectionLabel = sectionLabel("📋  ENTER NEWS TEXT");
        panel.add(sectionLabel, BorderLayout.NORTH);

        // Text area
        inputArea = new JTextArea();
        inputArea.setFont(FONT_MONO_MD);
        inputArea.setBackground(BG_INPUT);
        inputArea.setForeground(TEXT_PRIMARY);
        inputArea.setCaretColor(ACCENT_BTN);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setMargin(new Insets(12, 14, 12, 14));
        inputArea.setBorder(null);

        // Placeholder text
        setPlaceholder(inputArea, "Paste a news headline or paragraph here…");

        // Char counter
        charCountLabel = new JLabel("0 characters");
        charCountLabel.setFont(FONT_SANS_SM);
        charCountLabel.setForeground(TEXT_DIM);
        charCountLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));

        inputArea.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                int len = inputArea.getText().length();
                charCountLabel.setText(len + " character" + (len == 1 ? "" : "s"));
            }
        });

        JScrollPane scroll = styledScrollPane(inputArea);
        scroll.setBorder(dossierBorder());

        // Analyse button
        analyzeButton = buildAnalyzeButton();

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(BG_DARK);
        bottomBar.add(charCountLabel,  BorderLayout.WEST);
        bottomBar.add(analyzeButton,   BorderLayout.EAST);

        panel.add(scroll,    BorderLayout.CENTER);
        panel.add(bottomBar, BorderLayout.SOUTH);
        return panel;
    }

    // ── Result panel ─────────────────────────────────────────────────────

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        panel.add(sectionLabel("🔍  ANALYSIS REPORT"), BorderLayout.NORTH);

        // Card that holds verdict + score bar + reasons
        resultCard = new JPanel(new BorderLayout(0, 12));
        resultCard.setBackground(BG_PANEL);
        resultCard.setBorder(new CompoundBorder(dossierBorder(),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        // Top: verdict label + score
        verdictLabel = new JLabel("— awaiting input —");
        verdictLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        verdictLabel.setForeground(TEXT_DIM);

        scoreValueLabel = new JLabel("");
        scoreValueLabel.setFont(FONT_MONO_LG);
        scoreValueLabel.setForeground(TEXT_DIM);
        scoreValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel verdictRow = new JPanel(new BorderLayout());
        verdictRow.setBackground(BG_PANEL);
        verdictRow.add(verdictLabel,    BorderLayout.WEST);
        verdictRow.add(scoreValueLabel, BorderLayout.EAST);

        // Score bar
        scoreBar = new ScoreBar();

        JPanel topBlock = new JPanel();
        topBlock.setLayout(new BoxLayout(topBlock, BoxLayout.Y_AXIS));
        topBlock.setBackground(BG_PANEL);
        topBlock.add(verdictRow);
        topBlock.add(Box.createVerticalStrut(10));
        topBlock.add(scoreBar);

        // Reasons text pane
        reasonsPane = new JTextPane();
        reasonsPane.setEditable(false);
        reasonsPane.setFont(FONT_MONO_SM);
        reasonsPane.setBackground(BG_PANEL);
        reasonsPane.setForeground(TEXT_PRIMARY);
        reasonsPane.setBorder(null);

        JScrollPane reasonsScroll = styledScrollPane(reasonsPane);
        reasonsScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SUBTLE));

        resultCard.add(topBlock,       BorderLayout.NORTH);
        resultCard.add(reasonsScroll,  BorderLayout.CENTER);

        panel.add(resultCard, BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════ //
    //  Button
    // ═══════════════════════════════════════════════════════════════════ //

    private JButton buildAnalyzeButton() {
        JButton btn = new JButton("  ANALYZE  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isPressed() ? ACCENT_BTN.darker()
                           : getModel().isRollover() ? ACCENT_BTN.brighter()
                           : ACCENT_BTN;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 38));

        btn.addActionListener(e -> runAnalysis());

        // Also trigger on Ctrl+Enter in the text area
        inputArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                "analyze");
        inputArea.getActionMap().put("analyze", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { runAnalysis(); }
        });

        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════ //
    //  Analysis Logic
    // ═══════════════════════════════════════════════════════════════════ //

    private void runAnalysis() {
        String text = inputArea.getText().trim();

        if (text.isEmpty() || text.equals("Paste a news headline or paragraph here…")) {
            shakeWindow();
            verdictLabel.setText("⚠  Please enter some text first.");
            verdictLabel.setForeground(ACCENT_SUSP);
            scoreValueLabel.setText("");
            scoreBar.setScore(0, BG_PANEL);
            reasonsPane.setText("");
            return;
        }

        // Disable button during processing (good habit even for fast ops)
        analyzeButton.setEnabled(false);
        analyzeButton.setText("  ANALYZING…  ");

        SwingWorker<AnalysisResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AnalysisResult doInBackground() {
                return analyzer.analyze(text);
            }

            @Override
            protected void done() {
                try {
                    AnalysisResult result = get();
                    displayResult(result);
                } catch (Exception ex) {
                    verdictLabel.setText("Error: " + ex.getMessage());
                    verdictLabel.setForeground(ACCENT_FAKE);
                } finally {
                    analyzeButton.setEnabled(true);
                    analyzeButton.setText("  ANALYZE  ");
                }
            }
        };
        worker.execute();
    }

    private void displayResult(AnalysisResult result) {
        String classification = result.getClassification();
        int    score          = result.getScore();

        // Pick accent colour
        Color accent = switch (classification) {
            case "REAL"       -> ACCENT_REAL;
            case "SUSPICIOUS" -> ACCENT_SUSP;
            default           -> ACCENT_FAKE;
        };

        // Verdict label
        String icon = switch (classification) {
            case "REAL"       -> "✔  REAL";
            case "SUSPICIOUS" -> "⚠  SUSPICIOUS";
            default           -> "✘  FAKE";
        };
        verdictLabel.setText(icon);
        verdictLabel.setForeground(accent);

        // Score
        scoreValueLabel.setText("Score: " + score + " / 100");
        scoreValueLabel.setForeground(accent);

        // Animated score bar
        scoreBar.animateTo(score, accent);

        // Reasons
        displayReasons(result, accent);

        // Flash the result card border
        flashBorder(resultCard, accent);
    }

    // ── Styled reason display ────────────────────────────────────────────

    private void displayReasons(AnalysisResult result, Color accent) {
        StyledDocument doc = reasonsPane.getStyledDocument();

        // Styles
        Style base = StyleContext.getDefaultStyleContext()
                .getStyle(StyleContext.DEFAULT_STYLE);

        Style headerStyle = doc.addStyle("header", base);
        StyleConstants.setForeground(headerStyle, TEXT_DIM);
        StyleConstants.setFontFamily(headerStyle, "Monospaced");
        StyleConstants.setFontSize(headerStyle, 11);

        Style ruleStyle = doc.addStyle("rule", base);
        StyleConstants.setForeground(ruleStyle, TEXT_PRIMARY);
        StyleConstants.setFontFamily(ruleStyle, "Monospaced");
        StyleConstants.setFontSize(ruleStyle, 12);

        Style accentStyle = doc.addStyle("accent", base);
        StyleConstants.setForeground(accentStyle, accent);
        StyleConstants.setFontFamily(accentStyle, "Monospaced");
        StyleConstants.setFontSize(accentStyle, 12);
        StyleConstants.setBold(accentStyle, true);

        try {
            doc.remove(0, doc.getLength());

            if (!result.hasViolations()) {
                doc.insertString(doc.getLength(), "\n  ✓  No red flags detected — text looks credible.\n", accentStyle);
                return;
            }

            doc.insertString(doc.getLength(),
                    "\n  RED FLAGS DETECTED (" + result.getTriggeredRules().size() + ")\n\n",
                    headerStyle);

            for (String rule : result.getTriggeredRules()) {
                doc.insertString(doc.getLength(), "  ", ruleStyle);
                doc.insertString(doc.getLength(), rule + "\n\n", ruleStyle);
            }

            // Score interpretation
            String interpretation = switch (result.getClassification()) {
                case "REAL"       -> "  Score 0–30: Text shows few or no signs of misinformation.\n"
                                   + "  Verify with a trusted source before sharing.";
                case "SUSPICIOUS" -> "  Score 31–70: Some red flags present. Content may be\n"
                                   + "  misleading. Cross-check with reputable outlets.";
                default           -> "  Score 71–100: Multiple strong indicators detected.\n"
                                   + "  This text uses common misinformation techniques. Do NOT share.";
            };
            doc.insertString(doc.getLength(), "\n" + interpretation + "\n", headerStyle);

        } catch (BadLocationException ignored) {}

        reasonsPane.setCaretPosition(0);
    }

    // ═══════════════════════════════════════════════════════════════════ //
    //  Helper widgets
    // ═══════════════════════════════════════════════════════════════════ //

    /** Custom animated score bar. */
    private static class ScoreBar extends JPanel {
        private int   currentScore  = 0;
        private int   targetScore   = 0;
        private Color barColor      = BG_PANEL;
        private Timer animTimer;

        ScoreBar() {
            setPreferredSize(new Dimension(100, 12));
            setBackground(BG_PANEL);
            setOpaque(true);
        }

        void setScore(int score, Color color) {
            this.currentScore = score;
            this.barColor = color;
            repaint();
        }

        void animateTo(int score, Color color) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            this.targetScore = score;
            this.barColor    = color;
            animTimer = new Timer(12, null);
            animTimer.addActionListener(e -> {
                if (currentScore < targetScore)      currentScore = Math.min(currentScore + 2, targetScore);
                else if (currentScore > targetScore) currentScore = Math.max(currentScore - 2, targetScore);
                repaint();
                if (currentScore == targetScore) ((Timer) e.getSource()).stop();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int filled = (int) (w * currentScore / 100.0);

            // Track
            g2.setColor(BORDER_SUBTLE);
            g2.fillRoundRect(0, 0, w, h, h, h);

            // Fill
            if (filled > 0) {
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, filled, h, h, h);
            }

            // Score tick marks every 10 %
            g2.setColor(BG_PANEL.brighter());
            for (int i = 1; i < 10; i++) {
                int x = (int) (w * i / 10.0);
                g2.drawLine(x, 2, x, h - 2);
            }

            g2.dispose();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(TEXT_DIM);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return lbl;
    }

    private JScrollPane styledScrollPane(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG_DARK);
        sp.getViewport().setBackground(c.getBackground());
        sp.setBorder(null);
        return sp;
    }

    private Border dossierBorder() {
        return new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    /** Brief animated border flash on verdict display. */
    private void flashBorder(JPanel panel, Color accent) {
        Border original = panel.getBorder();
        Border flash = new CompoundBorder(
                BorderFactory.createLineBorder(accent, 2),
                BorderFactory.createEmptyBorder(15, 17, 15, 17));
        panel.setBorder(flash);
        Timer t = new Timer(600, e -> panel.setBorder(original));
        t.setRepeats(false);
        t.start();
    }

    /** Placeholder text support for JTextArea. */
    private void setPlaceholder(JTextArea area, String placeholder) {
        area.setForeground(TEXT_DIM);
        area.setText(placeholder);

        area.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (area.getText().isEmpty()) {
                    area.setForeground(TEXT_DIM);
                    area.setText(placeholder);
                }
            }
        });
    }

    /** Quick shake animation to indicate empty input. */
    private void shakeWindow() {
        Point origin = getLocation();
        Timer shake = new Timer(30, null);
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        int[] step = {0};
        shake.addActionListener(e -> {
            if (step[0] < offsets.length) {
                setLocation(origin.x + offsets[step[0]], origin.y);
                step[0]++;
            } else {
                setLocation(origin);
                ((Timer) e.getSource()).stop();
            }
        });
        shake.start();
    }

    // ═══════════════════════════════════════════════════════════════════ //
    //  Entry point (standalone launch)
    // ═══════════════════════════════════════════════════════════════════ //

    /**
     * Launch the GUI.
     * Can be called from Main.java instead of (or alongside) ConsoleUI.
     */
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainWindow().setVisible(true);
        });
    }

    /** Standalone main – useful during development. */
    public static void main(String[] args) {
        launch();
    }
}
