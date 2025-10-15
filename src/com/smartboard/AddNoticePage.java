package com.smartboard;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class AddNoticePage extends JPanel {
    
    // Modern Color Palette (matching AdminDashboard)
    private static final Color BG = new Color(247, 248, 252);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private final MainFrame mainFrame;
    private String attachmentPath = ""; // Stores the selected file path
    
    // Form fields as instance variables
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<String> priorityCombo;
    private JComboBox<String> statusCombo;
    private JLabel uploadText;
    private JButton submitButton; // Button is now an instance variable

    private boolean isEditMode = false;
    private int editNoticeId = -1;

    private final Font customFont;
    
    public AddNoticePage(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());
        setBackground(BG);
        customFont = loadCustomFont();

        // Main content area with padding
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG);
        mainContent.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header section (fixed at top)
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setOpaque(false);
        headerSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        // Left: Title and subtitle
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        
        JLabel pageTitle = new JLabel("Create New Notice");
        pageTitle.setFont(customFont.deriveFont(Font.BOLD, 28f));
        pageTitle.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Fill in the details to add a new notice");
        subtitle.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        subtitle.setForeground(TEXT_SECONDARY);
        
        headerLeft.add(pageTitle);
        headerLeft.add(Box.createRigidArea(new Dimension(0, 4)));
        headerLeft.add(subtitle);

        // Right: Back button
        JButton back = createSecondaryButton("Back to Dashboard");
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        headerRight.setOpaque(false);
        headerRight.add(back);

        headerSection.add(headerLeft, BorderLayout.WEST);
        headerSection.add(headerRight, BorderLayout.EAST);
        mainContent.add(headerSection, BorderLayout.NORTH);

        // Form card wrapped in scroll pane
        JPanel formCard = createFormCard(frame);
        
        // Create scroll pane for the form
        JScrollPane scrollPane = new JScrollPane(formCard);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(BG);
        
        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // Actions
        back.addActionListener(e -> {
            consume(e);
            resetForm(); // Reset form when going back
            frame.showPage("AdminDashboard");
        });
    }

    // Called when editing a notice
    // FIXED: No longer needs JButton, uses instance variable `submitButton`
    public void loadNoticeForEdit(int id) {
        com.smartboard.dao.NoticeDAO dao = new com.smartboard.dao.NoticeDAO();
        com.smartboard.entity.Notice notice = dao.getNoticeById(id);

        if (notice != null) {
            // Use instance variables to set data
            if (titleField != null) titleField.setText(notice.getTitle());
            if (descriptionArea != null) descriptionArea.setText(notice.getDescription()); // <-- FIXED
            if (priorityCombo != null) priorityCombo.setSelectedItem(notice.getPriority());
         if (statusCombo != null) statusCombo.setSelectedItem(notice.getStatus());
            
            // ✅ START: ADD/REPLACE THIS BLOCK
            
            // 1. Get the path from the notice object
            String path = notice.getAttachmentPath();

            // 2. Store it in the class variable so "Update" will use it
            //    If the path is null, set it to "" to avoid errors
            this.attachmentPath = (path != null) ? path : ""; 

            // 3. Update the UI text label
            if (this.uploadText != null && !this.attachmentPath.isEmpty()) {
                // Use File to get just the file name from the full path
                this.uploadText.setText("Selected: " + new File(this.attachmentPath).getName());
            } else if (this.uploadText != null) {
                // If there's no attachment, reset the text
                this.uploadText.setText("Click to upload or drag and drop");
            }
            // ✅ END: ADD/REPLACE THIS BLOCK

            isEditMode = true;
            editNoticeId = id;
            
            // Use instance variable `submitButton`
            if (this.submitButton != null) this.submitButton.setText("Update Notice");
        }
    }

    // DELETED the duplicate resetForm(JButton submit) method

    private JPanel createFormCard(MainFrame frame) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        card.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);

        // Title field
        // This call will assign `this.titleField` via createFormField
        formPanel.add(createFormField("Notice Title", "Enter a clear and concise title", true));
        formPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Description field
        JPanel descField = new JPanel();
        descField.setLayout(new BoxLayout(descField, BoxLayout.Y_AXIS));
        descField.setOpaque(false);
        descField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(customFont.deriveFont(Font.BOLD, 14f));
        descLabel.setForeground(TEXT_PRIMARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // FIXED: Assign instance variable `this.descriptionArea`
        this.descriptionArea = new ModernTextArea();
        this.descriptionArea.setRows(6);
        this.descriptionArea.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        JScrollPane descScroll = new JScrollPane(this.descriptionArea); // Use it
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        
        descField.add(descLabel);
        descField.add(Box.createRigidArea(new Dimension(0, 8)));
        descField.add(descScroll);
        
        formPanel.add(descField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Priority and Status in a row
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // These calls will assign `this.priorityCombo` and `this.statusCombo`
        row.add(createSelectField("Priority", new String[]{"High", "Medium", "Low"}));
        row.add(createSelectField("Status", new String[]{"Pending", "Approved", "Draft"}));
        
        formPanel.add(row);
        formPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Attachment section
        // This call will assign `this.uploadText`
        JPanel attachmentSection = createAttachmentSection();
        formPanel.add(attachmentSection);
        formPanel.add(Box.createRigidArea(new Dimension(0, 32)));

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JButton cancel = createSecondaryButton("Cancel");
        
        // FIXED: Assign instance variable `this.submitButton`
        this.submitButton = createPrimaryButton("Create Notice");
        
        // Button actions
        cancel.addActionListener(e -> {
            consume(e);
            resetForm(); // Reset form when cancelling
            frame.showPage("AdminDashboard");
        });
        
        // FIXED: Use instance variable `this.submitButton`
        this.submitButton.addActionListener(e -> {
            consume(e);

            // Fetch values
            // FIXED: Use instance variables directly, no more getComponent()
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            String priority = this.priorityCombo.getSelectedItem().toString();
            String status = this.statusCombo.getSelectedItem().toString();

            // Validate
            if(title.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/smartboard", "root", "@JD0890")) {

                if (isEditMode) {
                    // Update existing notice
                    String sql = "UPDATE notices SET title=?, description=?, priority=?, status=?, date=NOW(), attachment_path=? WHERE id=?";
                    try (PreparedStatement pst = con.prepareStatement(sql)) {
                        pst.setString(1, title);
                        pst.setString(2, description);
                        pst.setString(3, priority);
                        pst.setString(4, status);
                        pst.setString(5, attachmentPath);
                        pst.setInt(6, editNoticeId);

                        pst.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Notice updated successfully!");
                    }
                } else {
                    // Add new notice
                    String sql = "INSERT INTO notices(title, description, priority, status, date, published_by, attachment_path) " +
                                 "VALUES (?, ?, ?, ?, NOW(), ?, ?)";
                    try (PreparedStatement pst = con.prepareStatement(sql)) {
                        pst.setString(1, title);
                        pst.setString(2, description);
                        pst.setString(3, priority);
                        pst.setString(4, status);
                        pst.setString(5, "admin@example.com"); // TODO: Pass real username
                        pst.setString(6, attachmentPath);

                        pst.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Notice created successfully!");
                    }
                }

                resetForm(); // This now works correctly
                frame.showPage("AdminDashboard"); // TODO: Maybe refresh ManageNoticesPage?

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving notice: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(cancel);
        actionPanel.add(this.submitButton); // Add instance variable
        
        formPanel.add(actionPanel);

        card.add(formPanel, BorderLayout.NORTH);
        
        // Center the card
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, new GridBagConstraints());
        
        return wrapper;
    }

    
    
    private JPanel createFormField(String label, String placeholder, boolean required) {
        JPanel field = new JPanel();
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));
        field.setOpaque(false);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.setOpaque(false);
        labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(customFont.deriveFont(Font.BOLD, 14f));
        fieldLabel.setForeground(TEXT_PRIMARY);
        
        if (required) {
            JLabel asterisk = new JLabel(" *");
            asterisk.setFont(customFont.deriveFont(Font.BOLD, 14f));
            asterisk.setForeground(new Color(239, 68, 68));
            labelPanel.add(fieldLabel);
            labelPanel.add(asterisk);
        } else {
            labelPanel.add(fieldLabel);
        }
        
        ModernTextField textField = new ModernTextField(placeholder);
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        
        // FIXED: Assign instance variable `this.titleField`
        if (label.equals("Notice Title")) {
            this.titleField = textField;
        }

        field.add(labelPanel);
        field.add(Box.createRigidArea(new Dimension(0, 8)));
        field.add(textField);
        
        return field;
    }

    private JPanel createSelectField(String label, String[] options) {
        JPanel field = new JPanel();
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));
        field.setOpaque(false);
        
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(customFont.deriveFont(Font.BOLD, 14f));
        fieldLabel.setForeground(TEXT_PRIMARY);
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        ModernComboBox combo = new ModernComboBox(options);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        
        // FIXED: Assign instance variables
        if (label.equals("Priority")) {
            this.priorityCombo = combo;
        } else if (label.equals("Status")) {
            this.statusCombo = combo;
        }

        field.add(fieldLabel);
        field.add(Box.createRigidArea(new Dimension(0, 8)));
        field.add(combo);
        
        return field;
    }

    private JPanel createAttachmentSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section label
        JLabel label = new JLabel("Attachment (Optional)");
        label.setFont(customFont.deriveFont(Font.BOLD, 14f));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Upload box
        JPanel uploadBox = new JPanel(new BorderLayout());
        uploadBox.setBackground(new Color(249, 250, 251));
        uploadBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(BORDER, 2, 4, 4, true),
            BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));
        uploadBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        uploadBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        uploadBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Content inside upload box
        JPanel uploadContent = new JPanel();
        uploadContent.setLayout(new BoxLayout(uploadContent, BoxLayout.Y_AXIS));
        uploadContent.setOpaque(false);

        JLabel uploadIcon = new JLabel("📎");
        uploadIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        uploadIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FIXED: Assign instance variable `this.uploadText`
        this.uploadText = new JLabel("Click to upload or drag and drop");
        this.uploadText.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        this.uploadText.setForeground(TEXT_SECONDARY);
        this.uploadText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel uploadHint = new JLabel("PDF, DOC, DOCX (MAX. 10MB)");
        uploadHint.setFont(customFont.deriveFont(Font.PLAIN, 12f));
        uploadHint.setForeground(new Color(156, 163, 175));
        uploadHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        uploadContent.add(uploadIcon);
        uploadContent.add(Box.createRigidArea(new Dimension(0, 8)));
        uploadContent.add(this.uploadText); // Add the instance variable
        uploadContent.add(Box.createRigidArea(new Dimension(0, 4)));
        uploadContent.add(uploadHint);

        uploadBox.add(uploadContent, BorderLayout.CENTER);
        
        // Mouse click listener for file selection
        uploadBox.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Attachment");

                // Only allow specific file types
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.addChoosableFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                        "Documents (PDF, DOC, DOCX)", "pdf", "doc", "docx"
                    )
                );

                int result = fileChooser.showOpenDialog(AddNoticePage.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    // Store file path in class-level variable
                    attachmentPath = selectedFile.getAbsolutePath();

                    // FIXED: Use instance variable `this.uploadText`
                    if (uploadText != null) {
                        uploadText.setText("Selected: " + selectedFile.getName());
                    }
                }
            }
        });

        // Add components to section
        section.add(label);
        section.add(Box.createRigidArea(new Dimension(0, 8)));
        section.add(uploadBox);

        return section;
    }

    // FIXED: This is now the only resetForm method and it handles all state
    public void resetForm() {
        if (titleField != null) titleField.setText("");
        if (descriptionArea != null) descriptionArea.setText("");
        if (priorityCombo != null) priorityCombo.setSelectedIndex(0);
        if (statusCombo != null) statusCombo.setSelectedIndex(0);
        
        attachmentPath = "";
        if (uploadText != null) uploadText.setText("Click to upload or drag and drop");

        // Reset edit state
        isEditMode = false;
        editNoticeId = -1;
        if (submitButton != null) submitButton.setText("Create Notice");
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 14f));
        button.setForeground(Color.BLACK);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(79, 70, 229));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Color.BLACK); // <-- Note: You had Color.BLACK here, changed from PRIMARY
            }
        });
        
        return button;
    }
    
    
    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 14f));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(CARD_BG);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(11, 23, 11, 23)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(249, 250, 251));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(CARD_BG);
            }
        });
        
        return button;
    }

    private Font loadCustomFont() {
        try {
            Font poppins = Font.createFont(Font.TRUETYPE_FONT,
                Objects.requireNonNull(getClass().getResourceAsStream("/resource/Poppins-Regular.ttf")))
                .deriveFont(16f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(poppins);
            return poppins;
        } catch (Exception e) {
            return new Font("Segoe UI", Font.PLAIN, 16);
        }
    }

    private static void consume(Object... ignored) { /* intentionally empty */ }

    // Custom Components
    private static class RoundedPanel extends JPanel {
        private final int radius;
        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 6, radius, radius);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 8, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    private class ModernTextField extends JTextField {
        private final String placeholder;
        
        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(customFont.deriveFont(Font.PLAIN, 14f));
            setForeground(TEXT_PRIMARY);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            setBackground(CARD_BG);
            setOpaque(true);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            
            g2.setColor(hasFocus() ? PRIMARY : BORDER);
            g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            
            super.paintComponent(g);
            
            if (getText().isEmpty() && !hasFocus()) {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            
            g2.dispose();
        }
        
        @Override
        public boolean isOpaque() {
            return false;
        }
    }
    
    private class ModernTextArea extends JTextArea {
        public ModernTextArea() {
            setFont(customFont.deriveFont(Font.PLAIN, 14f));
            setForeground(TEXT_PRIMARY);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            setBackground(CARD_BG);
            setOpaque(true);
            setLineWrap(true);
            setWrapStyleWord(true);
        }
        
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            
            g2.setColor(hasFocus() ? PRIMARY : BORDER);
            g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            
            super.paintComponent(g);
            g2.dispose();
        }
        
        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    private class ModernComboBox extends JComboBox<String> {
        public ModernComboBox(String[] items) {
            super(items);
            setFont(customFont.deriveFont(Font.PLAIN, 14f));
            setForeground(TEXT_PRIMARY);
            setBackground(CARD_BG);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            setFocusable(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // This custom renderer makes the dropdown list look consistent
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (isSelected) {
                        setBackground(PRIMARY);
                        setForeground(Color.WHITE);
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(TEXT_PRIMARY);
                    }
                    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    return this;
                }
            });
        }
    }
}