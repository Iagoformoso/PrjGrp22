/**
 * @name        Swing implementation of Calculator View interface
 * @package     calculator
 * @file        SwingView.java
 * @description 
 */

package calculator;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import static calculator.domain.BinaryOperatorModes.ADD;
import static calculator.domain.BinaryOperatorModes.DIVIDE;
import static calculator.domain.BinaryOperatorModes.MINUS;
import static calculator.domain.BinaryOperatorModes.MULTIPLY;
import static calculator.domain.BinaryOperatorModes.POWER;
import static calculator.domain.UnaryOperatorModes.ABS;
import static calculator.domain.UnaryOperatorModes.BIN;
import static calculator.domain.UnaryOperatorModes.COS;
import static calculator.domain.UnaryOperatorModes.INV;
import static calculator.domain.UnaryOperatorModes.LN;
import static calculator.domain.UnaryOperatorModes.LOG;
import static calculator.domain.UnaryOperatorModes.NEGATE;
import static calculator.domain.UnaryOperatorModes.PERCENT;
import static calculator.domain.UnaryOperatorModes.SIN;
import static calculator.domain.UnaryOperatorModes.SQRT;
import static calculator.domain.UnaryOperatorModes.SQUARE;
import static calculator.domain.UnaryOperatorModes.TAN;
import static calculator.domain.UnaryOperatorModes.ACOS;
import static calculator.domain.UnaryOperatorModes.ASIN;
import static calculator.domain.UnaryOperatorModes.ATAN;

// Añadido: imports para MS, MR y MC de memoria
import static calculator.domain.UnaryOperatorModes.MS;
import static calculator.domain.UnaryOperatorModes.MR;
import static calculator.domain.UnaryOperatorModes.MC;

public class SwingView implements View {

    private final JFrame frame;
    private final JPanel mainPanel;
    private final JPanel[] subPanels;
    private final JTextField text;

    private final JButton[] butNums;
    // Añadido: butMS, butMR, butMC para memoria
    private final JButton butAdd, butMinus, butMultiply, butDivide,
            butEqual, butCancel, butSqrt, butSquare, butInv, butCos,
            butSin, butTan, butAcos, butAsin, butAtan, butPower, butLog, butPercent, butAbs, butBin,
            butln, butNegate, butDecimal, butReturn, butMS, butMR, butMC;

    private EventHandler eventHandler;

    private final Font numberFont = ConfigManager.getFont("ui.font.family", Font.BOLD, 18, "Segoe UI");
    private final Font functionFont = ConfigManager.getFont("ui.font.family", Font.PLAIN, 18, "Segoe UI");
    private final Font textFont = ConfigManager.getFont("ui.font.family", Font.BOLD, 24, "Segoe UI");
    private final ImageIcon image;

    private final DecimalFormat decimalFormat;
    private boolean startNewInput = true;

    public enum ButtonType {
        NUMBER, FUNCTION
    }

    public SwingView() throws IOException {
        Locale.setDefault(Locale.US);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        decimalFormat = new DecimalFormat("0.###############", symbols);
        decimalFormat.setGroupingUsed(false);

        frame = new JFrame("Calculator");
        image = loadIcon();


        mainPanel = new JPanel();
        mainPanel.setBackground(ConfigManager.getColor("ui.color.panel.bg", "#F0F0F0"));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Modificado: tamaño 11 para incluir la nueva fila para botones de memoria
        subPanels = new JPanel[11];
        for (int i = 0; i < 11; i++) {
            subPanels[i] = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
            subPanels[i].setBackground(ConfigManager.getColor("ui.color.panel.bg", "#F0F0F0"));
        }

        // --- JTextField for display ---
        text = new JTextField();
        text.setFont(textFont);
        text.setEditable(false);
        text.setHorizontalAlignment(JTextField.RIGHT);
        text.setColumns(15);

        text.setBackground(ConfigManager.getColor("ui.color.display.bg", "#FFFFFF"));
        text.setForeground(ConfigManager.getColor("ui.color.display.text", "#000000"));
        text.setOpaque(true);
        text.setBorder(javax.swing.BorderFactory.createLineBorder(
                UIManager.getColor("Panel.background"), 5));

        // Number buttons
        butNums = new JButton[10];
        for (int i = 0; i < 10; i++) {
            butNums[i] = createButton(String.valueOf(i), ButtonType.NUMBER);
        }

        // Function buttons
        butAdd = createButton("+", ButtonType.FUNCTION);
        butMinus = createButton("-", ButtonType.FUNCTION);
        butMultiply = createButton("*", ButtonType.FUNCTION);
        butDivide = createButton("/", ButtonType.FUNCTION);
        butEqual = createButton("=", ButtonType.FUNCTION);
        butCancel = createButton("C", ButtonType.FUNCTION);
        butReturn = createButton("DEL", ButtonType.FUNCTION);
        butSqrt = createButton("sqrt", ButtonType.FUNCTION);
        butSquare = createButton("x^2", ButtonType.FUNCTION);
        butInv = createButton("1/x", ButtonType.FUNCTION);
        butCos = createButton("cos", ButtonType.FUNCTION);
        butSin = createButton("sin", ButtonType.FUNCTION);
        butTan = createButton("tan", ButtonType.FUNCTION);
        butAcos = createButton("acos", ButtonType.FUNCTION);
        butAsin = createButton("asin", ButtonType.FUNCTION);
        butAtan = createButton("atan", ButtonType.FUNCTION);
        butln = createButton("ln", ButtonType.FUNCTION);
        butPower = createButton("x^y", ButtonType.FUNCTION);
        butLog = createButton("log", ButtonType.FUNCTION);
        butPercent = createButton("%", ButtonType.FUNCTION);
        butAbs = createButton("abs", ButtonType.FUNCTION);
        butBin = createButton("bin", ButtonType.FUNCTION);
        butNegate = createButton("+/-", ButtonType.NUMBER);
        butDecimal = createButton(",", ButtonType.NUMBER);
        butMS = createButton("MS", ButtonType.FUNCTION);
        butMR = createButton("MR", ButtonType.FUNCTION);
        butMC = createButton("MC", ButtonType.FUNCTION);

        setupLayout();
    }

    private JButton createButton(String label, ButtonType type) {
        JButton b = new JButton(label);
        b.setFont(type == ButtonType.NUMBER ? numberFont : functionFont);
        b.setPreferredSize(new java.awt.Dimension(80, 40));

        Color bgColor = (type == ButtonType.NUMBER) 
            ? ConfigManager.getColor("ui.color.number.bg", "#FFFFFF") 
            : ConfigManager.getColor("ui.color.function.bg", "#DCFFFF");
            
        b.setBackground(bgColor);
        b.setFocusPainted(false);
        b.setBorderPainted(true);
        b.setOpaque(true);
        return b;
    }

    private void setupLayout() {
        // --- Display panel ---
        JPanel displayPanel = new JPanel(new java.awt.BorderLayout());
        displayPanel.add(text, java.awt.BorderLayout.CENTER);
        mainPanel.add(displayPanel);

        // --- Row 1 ---
        subPanels[1].add(butNums[1]);
        subPanels[1].add(butNums[2]);
        subPanels[1].add(butNums[3]);
        subPanels[1].add(Box.createHorizontalStrut(15));
        subPanels[1].add(butAdd);
        subPanels[1].add(butMinus);
        mainPanel.add(subPanels[1]);

        // --- Row 2 ---
        subPanels[2].add(butNums[4]);
        subPanels[2].add(butNums[5]);
        subPanels[2].add(butNums[6]);
        subPanels[2].add(Box.createHorizontalStrut(15));
        subPanels[2].add(butMultiply);
        subPanels[2].add(butDivide);
        mainPanel.add(subPanels[2]);

        // --- Row 3 ---
        subPanels[3].add(butNums[7]);
        subPanels[3].add(butNums[8]);
        subPanels[3].add(butNums[9]);
        subPanels[3].add(Box.createHorizontalStrut(15));
        subPanels[3].add(butEqual);
        subPanels[3].add(butCancel);
        mainPanel.add(subPanels[3]);

        // --- Row 4 ---
        subPanels[4].add(butNegate);
        subPanels[4].add(butNums[0]);
        subPanels[4].add(butDecimal);
        subPanels[4].add(Box.createHorizontalStrut(15));
        subPanels[4].add(butReturn);
        mainPanel.add(subPanels[4]);

        // --- Extra separation ---
        mainPanel.add(Box.createVerticalStrut(10));

        // --- Row 5 ---
        subPanels[5].add(butInv);
        subPanels[5].add(butln);
        subPanels[5].add(butLog);
        mainPanel.add(subPanels[5]);

        // --- Row 6 ---
        subPanels[6].add(butSquare);
        subPanels[6].add(butSqrt);
        subPanels[6].add(butPower);
        mainPanel.add(subPanels[6]);

        // --- Row 7 ---
        subPanels[7].add(butCos);
        subPanels[7].add(butSin);
        subPanels[7].add(butTan);
        mainPanel.add(subPanels[7]);

        // --- Row 8 ---
        subPanels[8].add(butAcos);
        subPanels[8].add(butAsin);
        subPanels[8].add(butAtan);
        mainPanel.add(subPanels[8]);

        // --- Row 9 ---
        subPanels[9].add(butPercent);
        subPanels[9].add(butAbs);
        subPanels[9].add(butBin);
        mainPanel.add(subPanels[9]);
      
        // --- Row 10 ---
        subPanels[10].add(butMS);
        subPanels[10].add(butMR);
        subPanels[10].add(butMC);
        subPanels[10].add(Box.createHorizontalStrut(15));
        mainPanel.add(subPanels[10]);
    }

    public void init() {
        frame.setSize(465, 510);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (image != null)
            frame.setIconImage(image.getImage());
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    @Override
    public void setActionListener(EventHandler handler) {
        this.eventHandler = handler;
        for (int i = 0; i < 10; i++) {
            final int index = i;
            butNums[i].addActionListener(e -> eventHandler.onNumberPressed(index));
        }

        // Binary operators
        butAdd.addActionListener(e -> eventHandler.onBinaryOperatorPressed(ADD));
        butMinus.addActionListener(e -> eventHandler.onBinaryOperatorPressed(MINUS));
        butMultiply.addActionListener(e -> eventHandler.onBinaryOperatorPressed(MULTIPLY));
        butDivide.addActionListener(e -> eventHandler.onBinaryOperatorPressed(DIVIDE));
        butPower.addActionListener(e -> eventHandler.onBinaryOperatorPressed(POWER));

        // Unary operators
        butSquare.addActionListener(e -> eventHandler.onUnaryOperatorPressed(SQUARE));
        butSqrt.addActionListener(e -> eventHandler.onUnaryOperatorPressed(SQRT));
        butInv.addActionListener(e -> eventHandler.onUnaryOperatorPressed(INV));
        butCos.addActionListener(e -> eventHandler.onUnaryOperatorPressed(COS));
        butSin.addActionListener(e -> eventHandler.onUnaryOperatorPressed(SIN));
        butTan.addActionListener(e -> eventHandler.onUnaryOperatorPressed(TAN));
        butAcos.addActionListener(e -> eventHandler.onUnaryOperatorPressed(ACOS));
        butAsin.addActionListener(e -> eventHandler.onUnaryOperatorPressed(ASIN));
        butAtan.addActionListener(e -> eventHandler.onUnaryOperatorPressed(ATAN));
        butLog.addActionListener(e -> eventHandler.onUnaryOperatorPressed(LOG));
        butln.addActionListener(e -> eventHandler.onUnaryOperatorPressed(LN));
        butPercent.addActionListener(e -> eventHandler.onUnaryOperatorPressed(PERCENT));
        butAbs.addActionListener(e -> eventHandler.onUnaryOperatorPressed(ABS));
        butBin.addActionListener(e -> eventHandler.onUnaryOperatorPressed(BIN));
        butNegate.addActionListener(e -> eventHandler.onUnaryOperatorPressed(NEGATE));

        // Other actions
        butDecimal.addActionListener(e -> eventHandler.onDecimalPressed());
        butEqual.addActionListener(e -> eventHandler.onEqualsPressed());
        butCancel.addActionListener(e -> eventHandler.onClearPressed());
        butReturn.addActionListener(e -> eventHandler.onReturnPressed());

        // Añadido: Acciones de memoria
        butMS.addActionListener(e -> eventHandler.onUnaryOperatorPressed(MS));
        butMR.addActionListener(e -> eventHandler.onUnaryOperatorPressed(MR));
        butMC.addActionListener(e -> eventHandler.onUnaryOperatorPressed(MC));
        
    }

    @Override
    public void displayResult(Double result) {
        if (result == null || Double.isNaN(result) || Double.isInfinite(result)) {
            text.setText("Error");
        } else {
            text.setText(decimalFormat.format(result));
        }
        startNewInput = true;
    }

    @Override
    public Double getDisplayValue() {
        String textValue = text.getText().trim();
        textValue = textValue.replace(',', '.');

        if (textValue.isEmpty()) {
            return 0.0;
        }

        // Detectar cadeas especiais
        switch (textValue) {
            case "NaN":
                return Double.NaN;
            case "Inf":
                return Double.POSITIVE_INFINITY;
            case "-Inf":
                return Double.NEGATIVE_INFINITY;
        }

        // Eliminar punto final sen díxitos
        if (textValue.endsWith(".")) {
            textValue = textValue.substring(0, textValue.length() - 1);
        }

        try {
            return Double.parseDouble(textValue);
        } catch (NumberFormatException e) {
            // Se por calquera motivo non é un número válido, devolve 0.0
            return 0.0;
        }
    }

    @Override
    public void appendToDisplay(String value) {
        if (startNewInput) {
            text.setText(value);
            startNewInput = false;
        } else {
            text.setText(text.getText() + value);
        }
    }

    @Override
    public void clearDisplay() {
        text.setText("");
        startNewInput = true;
    }

    @Override
    public void setDisplay(String displayText) {
        text.setText(displayText);
        startNewInput = true;
    }

    private ImageIcon loadIcon() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/icon/icon.png")) {
            if (is == null)
                return null;
            BufferedImage bufferedImage = ImageIO.read(is);
            return new ImageIcon(bufferedImage);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
            return null;
        }
    }
}