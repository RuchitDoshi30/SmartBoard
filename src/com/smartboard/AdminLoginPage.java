package com.smartboard;

import com.smartboard.dao.UserDAO;
import com.smartboard.entity.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class AdminLoginPage extends JPanel {

    // ----------------------
    // Color Palette
    // ----------------------
    private static final Color DEEP_BROWN = new Color(62, 39, 35);
    private static final Color RICH_BROWN = new Color(101, 67, 33);
    private static final Color WARM_BROWN = new Color(139, 90, 43);
    private static final Color GOLDEN = new Color(212, 175, 55);
    private static final Color LIGHT_GOLDEN = new Color(232, 204, 128);
    private static final Color CREAM_WHITE = new Color(250, 248, 245);
    private static final Color PURE_WHITE = new Color(255, 255, 255);
    private static final Color SOFT_BLACK = new Color(30, 30, 30);
    private static final Color TEXT_BLACK = new Color(40, 40, 40);
    private static final Color TEXT_GRAY = new Color(100, 100, 100);
    private static final Color BORDER_LIGHT = new Color(220, 215, 210);
    private static final Color INPUT_BG = new Color(248, 246, 243);

    private final Font customFont;

    // ----------------------
    // Constructor
    // ----------------------
    public AdminLoginPage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(CREAM_WHITE);
        customFont = loadCustomFont();

        // Main container with gradient background
        JPanel mainContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, CREAM_WHITE,
                        getWidth(), getHeight(), new Color(245, 240, 235)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainContainer.setLayout(new GridBagLayout());

        // Add login card
        JPanel cardPanel = createCardPanel(frame);
        mainContainer.add(cardPanel, new GridBagConstraints());

        add(mainContainer, BorderLayout.CENTER);
    }

    // ----------------------
    // Card Panel (Split Layout)
    // ----------------------
    private JPanel createCardPanel(MainFrame frame) {
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(1000, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Left panel (intro)
        JPanel leftPanel = createLeftPanel();

        // Right panel (form)
        JPanel rightPanel = createRightPanel(frame);

        // Left Panel constraints
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        card.add(leftPanel, gbc);

        // Right Panel constraints
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        card.add(rightPanel, gbc);

        return card;
    }


    // ----------------------
    // Left Panel
    // ----------------------
    private JPanel createLeftPanel() {
        JPanel panel = getJPanel();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setMaximumSize(new Dimension(350, 400));

        // Icon
        JLabel iconLabel = new JLabel("◆");
        iconLabel.setFont(new Font("Serif", Font.BOLD, 60));
        iconLabel.setForeground(GOLDEN);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Titles
        JLabel title = new JLabel("Welcome");
        title.setFont(customFont.deriveFont(Font.BOLD, 48f));
        title.setForeground(PURE_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("to SmartBoard");
        subtitle.setFont(customFont.deriveFont(Font.PLAIN, 22f));
        subtitle.setForeground(LIGHT_GOLDEN);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 30, 0));

        // Description
        JLabel description = new JLabel("<html><div style='text-align: center;'>Enter your personal details<br>and start your journey with us</div></html>");
        description.setFont(customFont.deriveFont(Font.PLAIN, 15f));
        description.setForeground(new Color(255, 255, 255, 200));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        description.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        // Sign up button


        content.add(iconLabel);
        content.add(title);
        content.add(subtitle);
        content.add(description);

        panel.add(content);
        return panel;
    }

    private JPanel getJPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, DEEP_BROWN,
                        getWidth(), getHeight(), RICH_BROWN
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Decorative elements
                g2d.setColor(new Color(212, 175, 55, 40));
                g2d.fillOval(-30, getHeight() / 2 - 100, 200, 200);
                g2d.fillOval(getWidth() - 100, 50, 150, 150);

                // Pattern
                g2d.setColor(new Color(212, 175, 55, 20));
                g2d.setStroke(new BasicStroke(2));
                for (int i = 0; i < 5; i++) {
                    int offset = i * 40;
                    g2d.drawLine(getWidth() - 50, offset, getWidth(), offset + 50);
                }
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    // ----------------------
    // Right Panel (Login Form)
    // ----------------------
    private JPanel createRightPanel(MainFrame frame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PURE_WHITE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(PURE_WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 40, 50, 40)); // padding for all sides

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Header
        JLabel welcomeBack = new JLabel("Welcome Back");
        welcomeBack.setFont(customFont.deriveFont(Font.BOLD, 32f));
        welcomeBack.setForeground(SOFT_BLACK);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 5, 0);
        formPanel.add(welcomeBack, gbc);

        JLabel subtext = new JLabel("Sign in to continue to your account");
        subtext.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        subtext.setForeground(TEXT_GRAY);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(subtext, gbc);

        // Email label and field (label directly above field, no extra space)
        JLabel emailLabel = createLabel("Email Address");
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(emailLabel, gbc);

        JTextField emailField = createTextField();
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(emailField, gbc);

        // Password label and field
        JLabel passwordLabel = createLabel("Password");
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = createPasswordField();
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(passwordField, gbc);

        // Options Panel (Remember Me + Forgot Password)
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBackground(PURE_WHITE);
        optionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        GridBagConstraints optGbc = new GridBagConstraints();
        optGbc.gridx = 0;
        optGbc.weightx = 0;
        optGbc.anchor = GridBagConstraints.WEST;

        JCheckBox rememberBox = createCheckbox();
        optionsPanel.add(rememberBox, optGbc);

        JLabel forgotLink = new JLabel("Forgot Password?");
        forgotLink.setFont(customFont.deriveFont(Font.BOLD, 13f));
        forgotLink.setForeground(WARM_BROWN);
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLink.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { forgotLink.setForeground(GOLDEN); }
            public void mouseExited(MouseEvent e) { forgotLink.setForeground(WARM_BROWN); }
        });

        optGbc.gridx = 1;
        optGbc.weightx = 1.0;
        optGbc.anchor = GridBagConstraints.EAST;
        optionsPanel.add(forgotLink, optGbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(10, 0, 25, 0);
        formPanel.add(optionsPanel, gbc);

        // Sign In Button
        JButton signInBtn = createPrimaryButton();
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(signInBtn, gbc);

 signInBtn.addActionListener(ev -> {
    String username = emailField.getText();
    String password = String.valueOf(passwordField.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(panel,
                "Please enter both username and password",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    UserDAO userDAO = new UserDAO();
    User admin = userDAO.loginAdmin(username, password);

    if (admin != null) {
        // ✅ Store logged-in admin in MainFrame
        frame.setLoggedInUsername(username);
        frame.setLoggedInUser(admin);

        signInBtn.setText("✓ Signing In...");
        signInBtn.setEnabled(false);

        Timer timer = new Timer(500, t -> frame.showPage("AdminDashboard"));
        timer.setRepeats(false);
        timer.start();
    } else {
        JOptionPane.showMessageDialog(panel,
                "Invalid credentials",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
    }

    Arrays.fill(password.toCharArray(), (char) 0);
});


        // Go Back to Home Button
        JButton backToHomeBtn = createSecondaryButton("Go Back to Home");
        backToHomeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(backToHomeBtn, gbc);

        backToHomeBtn.addActionListener(e -> frame.showPage("UserHome"));

    panel.add(formPanel);
        return panel;
    }


    // ----------------------
    // Helper Components
    // ----------------------
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(customFont.deriveFont(Font.BOLD, 13f));
        label.setForeground(TEXT_BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(hasFocus() ? GOLDEN : BORDER_LIGHT);
                g2d.setStroke(new BasicStroke(hasFocus() ? 2 : 1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            }
        };

        field.setOpaque(false);
        field.setBackground(INPUT_BG);
        field.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        field.setForeground(TEXT_GRAY);
        field.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setPreferredSize(new Dimension(350, 46));
        field.setText("yourname@example.com");

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals("yourname@example.com")) {
                    field.setText("");
                    field.setForeground(TEXT_BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText("yourname@example.com");
                    field.setForeground(TEXT_GRAY);
                }
            }
        });

        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(hasFocus() ? GOLDEN : BORDER_LIGHT);
                g2d.setStroke(new BasicStroke(hasFocus() ? 2 : 1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            }
        };

        field.setOpaque(false);
        field.setBackground(INPUT_BG);
        field.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        field.setForeground(TEXT_GRAY);
        field.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setPreferredSize(new Dimension(350, 46));
        field.setEchoChar((char) 0);
        field.setText("Enter your password");

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals("Enter your password")) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(TEXT_BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText("Enter your password");
                    field.setForeground(TEXT_GRAY);
                }
            }
        });

        return field;
    }

    private JButton createPrimaryButton() {
        JButton button = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) g2d.setColor(TEXT_GRAY);
                else if (getModel().isPressed()) g2d.setColor(RICH_BROWN);
                else if (getModel().isRollover()) {
                    g2d.setPaint(new GradientPaint(0, 0, WARM_BROWN, getWidth(), 0, GOLDEN));
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, DEEP_BROWN, getWidth(), 0, RICH_BROWN));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        button.setFont(customFont.deriveFont(Font.BOLD, 15f));
        button.setForeground(PURE_WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BORDER_LIGHT);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            }
        };
        button.setFont(customFont.deriveFont(Font.BOLD, 14f));
        button.setForeground(TEXT_BLACK);
        button.setBackground(PURE_WHITE);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(245, 240, 235));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(PURE_WHITE);
            }
        });
        
        return button;
    }

    private JCheckBox createCheckbox() {
        JCheckBox checkbox = new JCheckBox("Remember me");
        checkbox.setFont(customFont.deriveFont(Font.PLAIN, 13f));
        checkbox.setForeground(TEXT_GRAY);
        checkbox.setBackground(PURE_WHITE);
        checkbox.setFocusPainted(false);
        checkbox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return checkbox;
    }

    // ----------------------
    // Load Custom Font
    // ----------------------
    private Font loadCustomFont() {
        try {
            Font poppins = Font.createFont(
                    Font.TRUETYPE_FONT,
                    Objects.requireNonNull(getClass().getResourceAsStream("/resource/Poppins-Regular.ttf"))
            ).deriveFont(16f);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(poppins);
            return poppins;
        } catch (FontFormatException | IOException | NullPointerException e) {
            return new Font("Segoe UI", Font.PLAIN, 16);
        }
    }
}
