package com.smartboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent; // Added for ActionEvent
import java.io.File;               // Added for File operations
import java.io.IOException;          // Added for IO errors
import java.text.SimpleDateFormat; // Added for date formatting
import java.util.Objects;
// Import your entity and DAO
import com.smartboard.entity.Notice;
import com.smartboard.dao.NoticeDAO;

public class UserNoticeDetail extends JPanel {

    // Modern Color Palette
    private static final Color BG = new Color(247, 248, 252);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color SHADOW = new Color(0, 0, 0, 10);

    private final Font customFont;
    private final MainFrame mainFrame;
    private final NoticeDAO noticeDAO;

    // --- References to UI Components ---
    private JLabel titleLabel;
    private JPanel metaBar;
    private JLabel dateLabel;
    private JTextArea descText;
    private JPanel contentPanel;
    private JPanel attachmentBox; // Panel that holds the single attachment item
    private JLabel attachHeading;
    // Keep currentAttachmentPath for the 'Open' button in the attachment item
    private String currentAttachmentPath = null;


    public UserNoticeDetail(MainFrame frame) {
        this.mainFrame = frame;
        this.noticeDAO = new NoticeDAO();
        setLayout(new BorderLayout());
        setBackground(BG);
        customFont = loadCustomFont();

        // Main content area
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG);
        mainContent.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header section
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setOpaque(false);
        headerSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        // Left: Back button
        JButton back = createSecondaryButton("Back to Board");

        // Right: Empty panel (Download button removed)
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);

        headerSection.add(back, BorderLayout.WEST);
        headerSection.add(headerRight, BorderLayout.EAST);
        mainContent.add(headerSection, BorderLayout.NORTH);

        // Detail card
        RoundedPanel detailCard = new RoundedPanel(12);
        detailCard.setBackground(CARD_BG);
        detailCard.setLayout(new BorderLayout());
        detailCard.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        // Content panel - Store reference
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Title section - Store reference
        titleLabel = new JLabel("Loading Notice...");
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 28f));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Meta info bar - Store reference
        metaBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        metaBar.setOpaque(false);
        metaBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel priorityPillPlaceholder = new JLabel();
        JLabel statusPillPlaceholder = new JLabel();

        JLabel dateIcon = new JLabel("📅");
        dateIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        dateLabel = new JLabel("Published: ");
        dateLabel.setFont(customFont.deriveFont(Font.PLAIN, 13f));
        dateLabel.setForeground(TEXT_SECONDARY);

        metaBar.add(priorityPillPlaceholder);
        metaBar.add(statusPillPlaceholder);
        metaBar.add(Box.createHorizontalStrut(8));
        metaBar.add(dateIcon);
        metaBar.add(dateLabel);

        // Divider
        JSeparator divider = new JSeparator();
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setForeground(BORDER);

        // Description section
        JLabel descHeading = new JLabel("Description");
        descHeading.setFont(customFont.deriveFont(Font.BOLD, 16f));
        descHeading.setForeground(TEXT_PRIMARY);
        descHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        descText = new JTextArea("Loading description...");
        descText.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        descText.setForeground(TEXT_PRIMARY);
        descText.setLineWrap(true);
        descText.setWrapStyleWord(true);
        descText.setEditable(false);
        descText.setOpaque(false);
        descText.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        descText.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Attachment section
        attachHeading = new JLabel("Attachments");
        attachHeading.setFont(customFont.deriveFont(Font.BOLD, 16f));
        attachHeading.setForeground(TEXT_PRIMARY);
        attachHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        attachHeading.setVisible(false); // Hide initially

        // Initialize attachmentBox as an empty placeholder
        attachmentBox = new JPanel();
        attachmentBox.setOpaque(false);
        attachmentBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        attachmentBox.setVisible(false);

        // Add all components to contentPanel
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        contentPanel.add(metaBar);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 24)));
        contentPanel.add(divider);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 24)));
        contentPanel.add(descHeading);
        contentPanel.add(descText);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 32)));
        contentPanel.add(attachHeading);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        contentPanel.add(attachmentBox); // Add the placeholder

        detailCard.add(contentPanel, BorderLayout.CENTER);
        mainContent.add(detailCard, BorderLayout.CENTER);

        // Wrap main content in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Actions
        back.addActionListener(e -> {
            consume(e);
            frame.showPage("UserHome"); // Go back to the notice board
        });
    }

    // --- Method to load notice data ---
    public void loadNoticeDetails(int noticeId) {
        Notice notice = noticeDAO.getNoticeById(noticeId);

        currentAttachmentPath = null; // Reset path

        if (notice != null) {
            titleLabel.setText("<html>" + notice.getTitle() + "</html>");
            titleLabel.setToolTipText(notice.getTitle());

            String dateString = "N/A";
            if (notice.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy");
                dateString = sdf.format(notice.getDate());
            }
            dateLabel.setText("Published: " + dateString);

            // Update Pills
            metaBar.remove(0); metaBar.remove(0); // Remove placeholders
            JLabel newPriorityPill = createPill(
                notice.getPriority() != null ? notice.getPriority() : "N/A",
                getPriorityBGColor(notice.getPriority()),
                getPriorityFGColor(notice.getPriority())
            );
            metaBar.add(newPriorityPill, 0);
            JLabel newStatusPill = createPill(
                notice.getStatus() != null ? notice.getStatus() : "N/A",
                getStatusBGColor(notice.getStatus()),
                getStatusFGColor(notice.getStatus())
            );
            metaBar.add(newStatusPill, 1);
            metaBar.revalidate();
            metaBar.repaint();

            descText.setText(notice.getDescription() != null ? notice.getDescription() : "No description provided.");
            descText.setCaretPosition(0);

            // --- Update Attachment (Corrected Logic v2) ---
            String attachmentPath = notice.getAttachmentPath();
            JPanel newAttachmentDisplay = null; // Temporary panel to hold the new content
            boolean shouldShowAttachmentSection = false;

            if (attachmentPath != null && !attachmentPath.trim().isEmpty()) {
                File attachmentFile = new File(attachmentPath);
                currentAttachmentPath = attachmentPath; // Store path for the item's button

                if (attachmentFile.exists()) {
                    // Attachment Exists: Create the real item box
                    newAttachmentDisplay = createAttachmentItem(
                        attachmentFile.getName(),
                        "File • " + formatFileSize(attachmentFile.length())
                    );
                    newAttachmentDisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
                    shouldShowAttachmentSection = true;

                } else {
                    // Path Exists, File Not Found: Create 'not found' box
                    JLabel notFoundLabel = new JLabel("Attachment file not found at specified path.");
                    notFoundLabel.setFont(customFont.deriveFont(Font.ITALIC, 13f));
                    notFoundLabel.setForeground(TEXT_SECONDARY);
                    notFoundLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JPanel notFoundPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    notFoundPanel.setOpaque(false);
                    notFoundPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    notFoundPanel.add(notFoundLabel);
                    newAttachmentDisplay = notFoundPanel;
                    shouldShowAttachmentSection = true;
                }
            }

            // --- Replace the component in the contentPanel ---
            // Find the current component at the attachmentBox position (index 10)
            int attachmentIndex = 10; // Index after attachHeading and spacer
            Component currentComp = null;
            if (contentPanel.getComponentCount() > attachmentIndex) {
                 currentComp = contentPanel.getComponent(attachmentIndex);
            }

            // If a new display was created, replace the old one (or add if nothing was there)
            if (newAttachmentDisplay != null) {
                if (currentComp != null) {
                    contentPanel.remove(attachmentIndex); // Remove whatever was there
                }
                contentPanel.add(newAttachmentDisplay, attachmentIndex); // Add the new one at the index
                attachmentBox = newAttachmentDisplay; // Update instance variable reference
            }
            // If no new display was created (no path), remove the old one if it existed
            else {
                if (currentComp != null) {
                    contentPanel.remove(attachmentIndex);
                }
                // Optionally add an empty placeholder panel back if needed for layout stability
                // attachmentBox = new JPanel(); attachmentBox.setOpaque(false); attachmentBox.setVisible(false);
                // contentPanel.add(attachmentBox, attachmentIndex);
                attachmentBox = null; // Ensure reference is null if nothing is displayed
            }


            // Set visibility
            attachHeading.setVisible(shouldShowAttachmentSection);
            if (attachmentBox != null) { // Check if attachmentBox is null before setting visibility
                attachmentBox.setVisible(shouldShowAttachmentSection);
            }
            // --- End Update Attachment ---

        } else {
            // Notice not found
            titleLabel.setText("Notice Not Found");
            dateLabel.setText("Published: N/A");
            descText.setText("The requested notice (ID: " + noticeId + ") could not be found.");
            metaBar.remove(0); metaBar.remove(0);
            metaBar.add(new JLabel(), 0); metaBar.add(new JLabel(), 1);
            metaBar.revalidate(); metaBar.repaint();
            // Hide attachment section cleanly
            attachHeading.setVisible(false);
            // Remove the component currently referenced by attachmentBox
            if (attachmentBox != null && attachmentBox.getParent() == contentPanel) {
                contentPanel.remove(attachmentBox);
            }
            attachmentBox = null; // Clear reference
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    // Removed downloadAttachmentAction

    // Action for the "Open" button WITHIN the attachment box
    private void openAttachmentItemAction(ActionEvent e) {
         JButton button = (JButton) e.getSource();
         String path = (String) button.getClientProperty("attachmentPath");
         openFile(path);
    }

    // Helper method to open a file using Desktop API
    private void openFile(String path) {
         if (path != null && !path.isEmpty()) {
            try {
                File file = new File(path);
                if (file.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Cannot open the file.\nIt might not exist or your system doesn't support opening it directly.",
                        "File Open Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException | SecurityException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "An error occurred while trying to open the file:\n" + ex.getMessage(),
                    "File Open Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Creates the visual box for an attachment item
    private JPanel createAttachmentItem(String filename, String fileInfo) {
        RoundedPanel attachItemBox = new RoundedPanel(8);
        attachItemBox.setBackground(new Color(249, 250, 251));
        attachItemBox.setLayout(new BorderLayout(12, 0));
        attachItemBox.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        attachItemBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        String iconChar = "📄";
        if (filename != null) {
             String lowerFilename = filename.toLowerCase();
             if (lowerFilename.endsWith(".pdf")) iconChar = "📕";
             else if (lowerFilename.matches(".*\\.(jpg|jpeg|png|gif)$")) iconChar = "🖼️";
             else if (lowerFilename.matches(".*\\.(doc|docx)$")) iconChar = "📃";
        }
        JLabel icon = new JLabel(iconChar);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JPanel fileInfoPanel = new JPanel();
        fileInfoPanel.setLayout(new BoxLayout(fileInfoPanel, BoxLayout.Y_AXIS));
        fileInfoPanel.setOpaque(false);

        JLabel fileNameLabel = new JLabel(filename != null ? filename : "Unknown File");
        fileNameLabel.setFont(customFont.deriveFont(Font.BOLD, 14f));
        fileNameLabel.setForeground(TEXT_PRIMARY);

        JLabel fileMetaLabel = new JLabel(fileInfo != null ? fileInfo : "");
        fileMetaLabel.setFont(customFont.deriveFont(Font.PLAIN, 12f));
        fileMetaLabel.setForeground(TEXT_SECONDARY);

        fileInfoPanel.add(fileNameLabel);
        fileInfoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        fileInfoPanel.add(fileMetaLabel);

        // Open button
        JButton openBtn = new JButton("Open");
        openBtn.setFont(customFont.deriveFont(Font.BOLD, 12f));
        openBtn.setForeground(PRIMARY);
        openBtn.setBackground(CARD_BG);
        openBtn.setOpaque(true);
        openBtn.setFocusPainted(false);
        openBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        openBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        openBtn.putClientProperty("attachmentPath", currentAttachmentPath); // Use the stored path
        openBtn.addActionListener(this::openAttachmentItemAction);

        attachItemBox.add(icon, BorderLayout.WEST);
        attachItemBox.add(fileInfoPanel, BorderLayout.CENTER);
        attachItemBox.add(openBtn, BorderLayout.EAST);

        return attachItemBox;
    }

    // Helper to format file size
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        digitGroups = Math.max(0, Math.min(digitGroups, units.length - 1));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }


    // Creates styled pill labels
    private JLabel createPill(String text, Color bg, Color fg) {
        JLabel pill = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(customFont.deriveFont(Font.BOLD, 11f));
        pill.setForeground(fg);
        pill.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        pill.setOpaque(false);
        return pill;
    }

    // --- Color Helpers for Pills ---
    private Color getPriorityBGColor(String priority) {
         if ("High".equalsIgnoreCase(priority)) return new Color(254, 226, 226);
         if ("Medium".equalsIgnoreCase(priority)) return new Color(254, 243, 199);
         return new Color(219, 234, 254); // Default Low
    }
    private Color getPriorityFGColor(String priority) {
         if ("High".equalsIgnoreCase(priority)) return new Color(220, 38, 38);
         if ("Medium".equalsIgnoreCase(priority)) return new Color(202, 138, 4);
         return new Color(37, 99, 235); // Default Low
    }
     private Color getStatusBGColor(String status) {
         if ("Active".equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status)) return new Color(220, 252, 231);
         if ("Pending".equalsIgnoreCase(status) || "Draft".equalsIgnoreCase(status)) return new Color(241, 245, 249);
         return new Color(254, 226, 226); // Default Inactive/Other
    }
    private Color getStatusFGColor(String status) {
         if ("Active".equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status)) return new Color(22, 163, 74);
         if ("Pending".equalsIgnoreCase(status) || "Draft".equalsIgnoreCase(status)) return TEXT_SECONDARY;
         return new Color(220, 38, 38); // Default Inactive/Other
    }


    // --- Button Creation Methods ---
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 13f));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(PRIMARY.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e) { button.setBackground(PRIMARY); }
        });
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 13f));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(CARD_BG);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(9, 19, 9, 19)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(BG); }
            public void mouseExited(java.awt.event.MouseEvent e) { button.setBackground(CARD_BG); }
        });
        return button;
    }

    // --- Utility Methods ---
    private Font loadCustomFont() {
        try {
            Font poppins = Font.createFont(Font.TRUETYPE_FONT,
                Objects.requireNonNull(getClass().getResourceAsStream("/resource/Poppins-Regular.ttf")))
                .deriveFont(16f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(poppins);
            return poppins;
        } catch (Exception e) {
            System.err.println("Error loading font: " + e.getMessage());
            return new Font("Segoe UI", Font.PLAIN, 16);
        }
    }

    private static void consume(Object... ignored) { /* intentionally empty */ }

    // --- Custom Rounded Panel ---
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
            g2.setColor(SHADOW);
            g2.fillRoundRect(3, 5, getWidth() - 7, getHeight() - 8, radius + 3, radius + 3);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public boolean isOpaque() { return false; }
    }

} // End of UserNoticeDetail class