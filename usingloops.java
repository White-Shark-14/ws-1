import java.   .*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * FullFunctionalCalculator.java
 * A single-file Swing-based calculator demonstrating a wide range of features
 * - Basic arithmetic (+, -, *, /)
 * - Decimal input
 * - Unary ops: sqrt, reciprocal, +/-
 * - Percent, power (x^y), modulus
 * - Memory functions: M+, M-, MR, MC
 * - Backspace, Clear (C), All Clear (AC)
 *
 * Compile: javac FullFunctionalCalculator.java
 * Run:     java FullFimport javax.swing.*;unctionalCalculator
 *
 * Written for Class 11 / learners — clear logic, uses BigDecimal for accuracy
 */
public class FullFunctionalCalculator extends JFrame implements ActionListener {
    private final JTextField display;
    private BigDecimal currentValue = BigDecimal.ZERO;    // current stored value
    private BigDecimal memory = BigDecimal.ZERO;          // memory register
    private String pendingOp = null;                      // +, -, *, /, %, ^
    private boolean startNewNumber = true;                // whether next digit starts a new number

    public FullFunctionalCalculator() {
        super("Full Functional Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(360, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6, 6));

        display = new JTextField("0");
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font("Monospaced", Font.BOLD, 28));
        display.setBackground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(display, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(6, 4, 6, 6));
        String[] labels = {
                "MC", "MR", "M+", "M-",
                "AC", "C", "←", "/",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "+/-", "0", ".", "=",
        };

        for (String lab : labels) addButton(buttons, lab);

        // Extra row below grid for advanced ops
        JPanel advanced = new JPanel(new GridLayout(1, 4, 6, 6));
        addAdvancedButton(advanced, "sqrt");
        addAdvancedButton(advanced, "%");
        addAdvancedButton(advanced, "1/x");
        addAdvancedButton(advanced, "x^y");

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(buttons, BorderLayout.CENTER);
        center.add(advanced, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // keyboard support
        addKeyBindings(center);

        setVisible(true);
    }

    private void addButton(JPanel parent, String label) {
        JButton b = new JButton(label);
        b.setFont(new Font("SansSerif", Font.PLAIN, 18));
        b.addActionListener(this);
        parent.add(b);
    }

    private void addAdvancedButton(JPanel parent, String label) {
        JButton b = new JButton(label);
        b.setFont(new Font("SansSerif", Font.PLAIN, 16));
        b.addActionListener(this);
        parent.add(b);
    }

    private void addKeyBindings(JComponent root) {
        // map some keys to actions
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        String digits = "0123456789";
        for (char d : digits.toCharArray()) {
            im.put(KeyStroke.getKeyStroke(d), "digit" + d);
            final String dd = String.valueOf(d);
            am.put("digit" + d, new AbstractAction() {
                public void actionPerformed(ActionEvent e) { appendNumber(dd); }
            });
        }

        im.put(KeyStroke.getKeyStroke('.'), "dot");
        am.put("dot", new AbstractAction() { public void actionPerformed(ActionEvent e) { appendDot(); } });

        im.put(KeyStroke.getKeyStroke('\n'), "enter");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        am.put("enter", new AbstractAction() { public void actionPerformed(ActionEvent e) { doEquals(); } });

        im.put(KeyStroke.getKeyStroke('+'), "plus");
        am.put("plus", new AbstractAction() { public void actionPerformed(ActionEvent e) { applyOp("+"); } });
        im.put(KeyStroke.getKeyStroke('-'), "minus");
        am.put("minus", new AbstractAction() { public void actionPerformed(ActionEvent e) { applyOp("-"); } });
        im.put(KeyStroke.getKeyStroke('*'), "mul");
        am.put("mul", new AbstractAction() { public void actionPerformed(ActionEvent e) { applyOp("*"); } });
        im.put(KeyStroke.getKeyStroke('/'), "div");
        am.put("div", new AbstractAction() { public void actionPerformed(ActionEvent e) { applyOp("/"); } });
    }

    private BigDecimal parseDisplay() {
        try {
            return new BigDecimal(display.getText());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void setDisplay(BigDecimal val) {
        // strip trailing zeros, but keep at least one digit
        val = val.stripTrailingZeros();
        if (val.scale() < 0) val = val.setScale(0);
        display.setText(val.toPlainString());
    }

    private void appendNumber(String digit) {
        if (startNewNumber) {
            display.setText(digit.equals("0") ? "0" : digit);
            startNewNumber = false;
        } else {
            if (display.getText().equals("0") && !digit.equals("0"))
                display.setText(digit);
            else
                display.setText(display.getText() + digit);
        }
    }

    private void appendDot() {
        if (startNewNumber) {
            display.setText("0.");
            startNewNumber = false;
        } else if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
    }

    private void clearEntry() {
        display.setText("0");
        startNewNumber = true;
    }

    private void allClear() {
        display.setText("0");
        currentValue = BigDecimal.ZERO;
        pendingOp = null;
        startNewNumber = true;
    }

    private void backspace() {
        if (!startNewNumber) {
            String s = display.getText();
            if (s.length() <= 1) {
                display.setText("0");
                startNewNumber = true;
            } else {
                display.setText(s.substring(0, s.length() - 1));
            }
        }
    }

    private void applyUnary(String op) {
        BigDecimal x = parseDisplay();
        try {
            switch (op) {
                case "sqrt":
                    if (x.compareTo(BigDecimal.ZERO) < 0) throw new ArithmeticException("sqrt of negative");
                    double d = Math.sqrt(x.doubleValue());
                    setDisplay(new BigDecimal(d).setScale(10, RoundingMode.HALF_UP));
                    break;
                case "1/x":
                    if (x.compareTo(BigDecimal.ZERO) == 0) throw new ArithmeticException("divide by zero");
                    setDisplay(BigDecimal.ONE.divide(x, 12, RoundingMode.HALF_UP));
                    break;
                case "+/-":
                    setDisplay(x.negate());
                    break;
                case "%":
                    // make percent relative to currentValue if pending op exists
                    BigDecimal pct = x.divide(BigDecimal.valueOf(100), 12, RoundingMode.HALF_UP);
                    if (pendingOp != null) {
                        // treat as percentage of currentValue
                        setDisplay(currentValue.multiply(pct));
                    } else {
                        setDisplay(pct);
                    }
                    break;
            }
        } catch (ArithmeticException ex) {
            display.setText("Error");
            startNewNumber = true;
        }
    }

    private void applyOp(String op) {
        BigDecimal x = parseDisplay();
        if (pendingOp == null) {
            currentValue = x;
        } else if (!startNewNumber) {
            currentValue = compute(currentValue, x, pendingOp);
            setDisplay(currentValue);
        }
        pendingOp = op;
        startNewNumber = true;
    }

    private void doEquals() {
        BigDecimal x = parseDisplay();
        if (pendingOp != null) {
            try {
                currentValue = compute(currentValue, x, pendingOp);
                setDisplay(currentValue);
            } catch (ArithmeticException ex) {
                display.setText("Error");
            }
            pendingOp = null;
            startNewNumber = true;
        }
    }

    private BigDecimal compute(BigDecimal a, BigDecimal b, String op) {
        switch (op) {
            case "+": return a.add(b);
            case "-": return a.subtract(b);
            case "*": return a.multiply(b);
            case "/":
                if (b.compareTo(BigDecimal.ZERO) == 0) throw new ArithmeticException("divide by zero");
                return a.divide(b, 12, RoundingMode.HALF_UP);
            case "%":
                // modulus
                if (b.compareTo(BigDecimal.ZERO) == 0) throw new ArithmeticException("mod by zero");
                return new BigDecimal(a.remainder(b).toPlainString());
            case "^":
                // power using double (reasonable for normal calculator needs)
                double res = Math.pow(a.doubleValue(), b.doubleValue());
                return new BigDecimal(res).setScale(10, RoundingMode.HALF_UP);
            default: return b;
        }
    }

    // Memory functions
    private void memoryClear() { memory = BigDecimal.ZERO; }
    private void memoryRecall() { setDisplay(memory); startNewNumber = true; }
    private void memoryAdd() { memory = memory.add(parseDisplay()); }
    private void memorySubtract() { memory = memory.subtract(parseDisplay()); }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = ((JButton)e.getSource()).getText();
        switch (cmd) {
            case "AC": allClear(); break;
            case "C": clearEntry(); break;
            case "←": backspace(); break;
            case "MC": memoryClear(); break;
            case "MR": memoryRecall(); break;
            case "M+": memoryAdd(); break;
            case "M-": memorySubtract(); break;
            case "sqrt": applyUnary("sqrt"); break;
            case "%": applyUnary("%"); break;
            case "1/x": applyUnary("1/x"); break;
            case "+/-": applyUnary("+/-"); break;
            case "x^y":
                // treat as pending ^ operator
                applyOp("^");
                break;
            case "+": case "-": case "*": case "/":
                applyOp(cmd); break;
            case "=": doEquals(); break;
            case ".": appendDot(); break;
            default:
                // digits
                if (cmd.matches("\\d")) appendNumber(cmd);
                break;
        }
    }

    public static void main(String[] args) {
        // Run UI on EDT
        SwingUtilities.invokeLater(() -> new FullFunctionalCalculator());
    }
}
