package com.smartboard;
import java.util.ArrayList; // ✅ Add this import
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage; // Needed for image loading & PDF rendering
import javax.imageio.ImageIO;      // Needed for image loading
import java.io.File;               // Needed for attachment path parsing and image loading
import java.io.IOException;          // Needed for image/PDF loading errors
import java.text.SimpleDateFormat; // Needed for date formatting
import java.util.List;             // Needed for List<Notice>
import java.util.Objects;
import java.util.Date;             // Needed for Timestamp
import java.awt.event.ActionListener; // Needed for Timer
import java.awt.event.ActionEvent;    // Needed for Timer
// Import your Notice entity and DAO
import com.smartboard.entity.Notice;
import com.smartboard.dao.NoticeDAO;

// --- PDFBox Imports ---
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
// --- End PDFBox Imports ---

public class UserHomePage extends JPanel {

    // Modern Color Palette
    private static final Color BG = new Color(247, 248, 252);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color SHADOW = new Color(0, 0, 0, 15);
    private static final Color PREVIEW_BG = new Color(241, 245, 249);

    private final Font customFont;
    private JPanel noticeGridPanel;
    private final MainFrame mainFrame;
    private final NoticeDAO noticeDAO;
    private Timer refreshTimer; // Timer for automatic refresh
    private JLabel lastRefreshedLabel; // Label for timestamp
    private SimpleDateFormat timestampFormat; // Formatter for the timestamp
    private JTextField searchField; // Search field in header

    public UserHomePage(MainFrame frame) {
        this.mainFrame = frame;
        this.noticeDAO = new NoticeDAO();
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        customFont = loadCustomFont();
        timestampFormat = new SimpleDateFormat("hh:mm:ss a"); // Format for "Last Refreshed: 01:45:30 PM"

        // 1. Header Panel (Now includes search, refresh, timestamp)
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 2. Notice Grid Panel (inside a Scroll Pane)
        noticeGridPanel = new JPanel(new GridLayout(0, 3, 20, 20)); // Use GridLayout
        noticeGridPanel.setBackground(BG);
        noticeGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(noticeGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // 3. Initial Load & Start Timer
        refreshNotices(); // Load data initially and set timestamp
        startAutoRefresh(); // Start the 30-second timer
    }

    // Creates the header with Title, Search, Refresh, Timestamp, Login
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0)); // Add horizontal gap
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), // Bottom border
            BorderFactory.createEmptyBorder(10, 24, 10, 24)      // Padding
        ));
        headerPanel.setPreferredSize(new Dimension(0, 70)); // Keep fixed height

        // --- Left Side: Title ---
        JLabel titleLabel = new JLabel("Notice Board");
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(TEXT_PRIMARY);

        // --- Center: Search Bar ---
        searchField = new ModernTextField("Search notices..."); // Use custom text field
        // Add listener for search (optional - currently just UI)
        searchField.addActionListener(e -> performSearch()); // Trigger search on Enter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
        });


        // --- Right Side: Timestamp, Refresh, Login ---
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); // Align right with gaps
        rightPanel.setOpaque(false);

        lastRefreshedLabel = new JLabel("Last Refreshed: --:--:-- --");
        lastRefreshedLabel.setFont(customFont.deriveFont(Font.PLAIN, 12f));
        lastRefreshedLabel.setForeground(TEXT_SECONDARY);

        JButton refreshButton = createSecondaryButton("Refresh"); // Refresh icon
        refreshButton.addActionListener(e -> refreshNotices()); // Manual refresh

        JButton adminLoginButton = createSecondaryButton("Admin Login"); // Key icon
        adminLoginButton.addActionListener(e -> mainFrame.showPage("AdminLoginPage"));

        rightPanel.add(lastRefreshedLabel);
        rightPanel.add(refreshButton);
        rightPanel.add(adminLoginButton);

        // --- Add components to header ---
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(searchField, BorderLayout.CENTER);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    // --- Refresh Logic ---
    private void refreshNotices() {
        System.out.println("Refreshing notices..."); // Log refresh action
        loadNoticesFromDB(); // Reload data from DB
        // Update timestamp label
        lastRefreshedLabel.setText("Last Refreshed: " + timestampFormat.format(new Date()));
        // Stop and restart timer to reset the 30s interval after manual refresh
        stopAutoRefresh();
        startAutoRefresh();
    }

    // Starts the automatic refresh timer
    private void startAutoRefresh() {
        if (refreshTimer == null) {
            ActionListener refreshAction = e -> {
                System.out.println("Auto-refresh triggered..."); // Log auto-refresh
                loadNoticesFromDB(); // Reload data
                lastRefreshedLabel.setText("Last Refreshed: " + timestampFormat.format(new Date())); // Update time
            };
            // Timer triggers every 30000 ms (30 seconds)
            refreshTimer = new Timer(30000, refreshAction);
            refreshTimer.setInitialDelay(30000); // Wait 30s before first auto-refresh
            refreshTimer.setRepeats(true);       // Keep repeating
        }
        if (!refreshTimer.isRunning()) {
            refreshTimer.start();
            System.out.println("Auto-refresh timer started.");
        }
    }

    // Stops the automatic refresh timer (e.g., if navigating away or during manual refresh)
    private void stopAutoRefresh() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
            System.out.println("Auto-refresh timer stopped.");
        }
    }

    // --- Search Logic Placeholder ---
    private void performSearch() {
        String searchText = searchField.getText();
        System.out.println("Search triggered for: " + searchText);
        // TODO: Implement filtering logic here.
        // You would likely need to:
        // 1. Get all notices from DAO again (or keep a master list).
        // 2. Filter the list based on searchText matching title/description.
        // 3. Clear the noticeGridPanel.
        // 4. Add cards ONLY for the filtered notices.
        // 5. Revalidate/repaint the noticeGridPanel.
        // For now, it just prints the search text.
        // We might want to debounce this slightly so it doesn't fire on every keystroke.
        loadNoticesFromDB(); // TEMP: Reload all for now until filter is built
    }


    // Loads notices from DB and creates cards
    private void loadNoticesFromDB() {
        List<Notice> noticeList = noticeDAO.getAllNotices();

        noticeGridPanel.removeAll(); // Clear previous cards
        // Ensure GridLayout is reapplied if message was shown previously
        if (!(noticeGridPanel.getLayout() instanceof GridLayout)) {
             noticeGridPanel.setLayout(new GridLayout(0, 3, 20, 20));
        }

        // --- Filter based on search text (Basic Title Filter Example) ---
        String filterText = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        List<Notice> filteredList = new ArrayList<>(); // Create a new list for filtered items
        if (noticeList != null) {
            if (filterText.isEmpty()) {
                filteredList.addAll(noticeList); // No filter, add all
            } else {
                for (Notice notice : noticeList) {
                    // Check if title contains the filter text (case-insensitive)
                    if (notice.getTitle() != null && notice.getTitle().toLowerCase().contains(filterText)) {
                        filteredList.add(notice);
                    }
                     // Optionally search description too:
                     // else if (notice.getDescription() != null && notice.getDescription().toLowerCase().contains(filterText)) {
                     //    filteredList.add(notice);
                     // }
                }
            }
        }
        // --- End Filtering ---


        if (!filteredList.isEmpty()) { // Use the filtered list now
            boolean noticesAdded = false;
            for (Notice notice : filteredList) { // Iterate over filtered list
                // You can uncomment this if you only want to show specific statuses
                // if ("Approved".equalsIgnoreCase(notice.getStatus()) || "Active".equalsIgnoreCase(notice.getStatus())) {
                    noticeGridPanel.add(createNoticeCard(notice));
                    noticesAdded = true;
                // }
            }
            if (!noticesAdded) {
                // This case might occur if filtering removed all items
                 displayNoNoticesMessage(filterText.isEmpty() ? "No notices found matching status." : "No notices found matching search/status.");
            }
        } else {
            // Display message if filtered list is empty
             displayNoNoticesMessage(filterText.isEmpty() ? "No notices available." : "No notices found matching your search.");
        }

        noticeGridPanel.revalidate();
        noticeGridPanel.repaint();
    }

    // Helper to display a message when no notices are shown
    private void displayNoNoticesMessage(String message) {
        JLabel noNoticesLabel = new JLabel(message);
        noNoticesLabel.setFont(customFont.deriveFont(Font.PLAIN, 16f));
        noNoticesLabel.setForeground(TEXT_SECONDARY);
        noNoticesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noticeGridPanel.setLayout(new BorderLayout()); // Change layout to center message
        noticeGridPanel.add(noNoticesLabel, BorderLayout.CENTER);
    }

    // Creates a single notice card JPanel using the Notice entity
    private JPanel createNoticeCard(Notice notice) {
        // --- ( Code for createNoticeCard remains the same as previous version ) ---
        // --- ( It includes the PDFBox rendering logic ) ---
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SHADOW);
                g2.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 8, 16, 16);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        card.setPreferredSize(new Dimension(300, 350));

        // 1. Preview Area
        JPanel previewPanel = new JPanel(new GridBagLayout());
        previewPanel.setBackground(PREVIEW_BG);
        previewPanel.setPreferredSize(new Dimension(0, 180));
        previewPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel previewLabel = new JLabel();
        previewLabel.setFont(customFont.deriveFont(Font.ITALIC, 14f));
        previewLabel.setForeground(TEXT_SECONDARY);
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);

        String attachmentPath = notice.getAttachmentPath();
        String attachmentType = "none";
        if (attachmentPath != null && !attachmentPath.trim().isEmpty()) {
            String lowerPath = attachmentPath.toLowerCase();
            if (lowerPath.endsWith(".pdf")) attachmentType = "pdf";
            else if (lowerPath.matches(".*\\.(jpg|jpeg|png|gif)$")) attachmentType = "image";
            else attachmentType = "other";
        }

        previewLabel.setIcon(null); previewLabel.setText("");

        switch (attachmentType) {
            case "image":
                try {
                    File imgFile = new File(attachmentPath);
                    if (imgFile.exists()) {
                        BufferedImage originalImage = ImageIO.read(imgFile);
                        if (originalImage != null) {
                            ImageIcon imageIcon = createScaledIcon(originalImage, 280, 160);
                            previewLabel.setIcon(imageIcon);
                        } else { previewLabel.setText("⚠️ Error Reading Image"); }
                    } else { previewLabel.setText("🖼️ Image Not Found"); }
                } catch (IOException e) {
                    System.err.println("Error loading image: " + attachmentPath + " - " + e.getMessage());
                    previewLabel.setText("⚠️ Error Loading Image");
                }
                break;
            case "pdf":
                PDDocument document = null;
                try {
                    File pdfFile = new File(attachmentPath);
                    if (pdfFile.exists()) {
                        document = Loader.loadPDF(pdfFile);
                        if (document.getNumberOfPages() > 0) {
                             PDFRenderer pdfRenderer = new PDFRenderer(document);
                             BufferedImage image = pdfRenderer.renderImageWithDPI(0, 72);
                             ImageIcon imageIcon = createScaledIcon(image, 280, 160);
                             previewLabel.setIcon(imageIcon);
                        } else { previewLabel.setText("📄 Empty PDF"); }
                    } else { previewLabel.setText("📄 PDF Not Found"); }
                } catch (IOException e) {
                    System.err.println("Error loading/rendering PDF: " + attachmentPath + " - " + e.getMessage());
                    previewLabel.setText("⚠️ Error Loading PDF");
                } finally { if (document != null) { try { document.close(); } catch (IOException e) { /* ignore */ } } }
                break;
           case "other":
                // --- START: Added Word Doc Check ---
                String iconChar = "📎"; // Default generic file icon
                String fileText = "File Attached";
                if (attachmentPath != null) { // Check if path is not null before checking extension
                    String lowerPath = attachmentPath.toLowerCase();
                    if (lowerPath.endsWith(".doc") || lowerPath.endsWith(".docx")) {
                        iconChar = "📃"; // Use a document icon for Word files
                        fileText = "Word Doc Attached";
                    }
                    // Add more checks here for other specific types if needed
                    // else if (lowerPath.endsWith(".xls") || lowerPath.endsWith(".xlsx")) { iconChar = "📊"; fileText = "Excel File Attached";}
                    // else if (lowerPath.endsWith(".ppt") || lowerPath.endsWith(".pptx")) { iconChar = "🖥️"; fileText = "PPT File Attached";}

                }
                previewLabel.setIcon(null); // Ensure no image icon is showing
                previewLabel.setText(iconChar + " " + fileText);
                // --- END: Added Word Doc Check ---
                break; // Keep the break
            default: previewLabel.setText(" (No Attachment) "); break;
        }
        previewPanel.add(previewLabel);

        // 2. Details Area
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(notice.getTitle());
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setToolTipText(notice.getTitle());

        JPanel metaPanel = new JPanel(new BorderLayout());
        metaPanel.setOpaque(false);
        metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        String dateString = (notice.getDate() != null) ? new SimpleDateFormat("yyyy-MM-dd").format(notice.getDate()) : "N/A";
        JLabel dateLabel = new JLabel("📅 " + dateString);
        dateLabel.setFont(customFont.deriveFont(Font.PLAIN, 12f));
        dateLabel.setForeground(TEXT_SECONDARY);

        JLabel priorityLabel = createPriorityLabel(notice.getPriority());
        priorityLabel.setFont(customFont.deriveFont(Font.BOLD, 11f));

        metaPanel.add(dateLabel, BorderLayout.WEST);
        metaPanel.add(priorityLabel, BorderLayout.EAST);

        JButton viewButton = createPrimaryButton("View Details");
        viewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        viewButton.addActionListener(e -> mainFrame.showNoticeDetails(notice.getId()));

        detailsPanel.add(titleLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        detailsPanel.add(metaPanel);
        detailsPanel.add(Box.createVerticalGlue());
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        detailsPanel.add(viewButton);

        card.add(previewPanel, BorderLayout.NORTH);
        card.add(detailsPanel, BorderLayout.CENTER);

        return card;
        // --- ( End of createNoticeCard code ) ---
    }

    // --- Helper Method for Image Scaling ---
    private ImageIcon createScaledIcon(BufferedImage originalImage, int targetWidth, int targetHeight) {
         if (originalImage == null) return null;
         int originalWidth = originalImage.getWidth();
         int originalHeight = originalImage.getHeight();
         int newWidth = originalWidth; int newHeight = originalHeight;
         if (originalWidth > targetWidth) { newWidth = targetWidth; newHeight = (newWidth * originalHeight) / originalWidth; }
         if (newHeight > targetHeight) { newHeight = targetHeight; newWidth = (newHeight * originalWidth) / originalHeight; }
         Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
         return new ImageIcon(scaledImage);
    }

    // Helper to create styled priority labels
    private JLabel createPriorityLabel(String priority) {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        Color bgColor, fgColor;
        if ("High".equalsIgnoreCase(priority)) { label.setText("High"); bgColor = new Color(254, 226, 226); fgColor = new Color(220, 38, 38);
        } else if ("Medium".equalsIgnoreCase(priority)) { label.setText("Medium"); bgColor = new Color(254, 243, 199); fgColor = new Color(202, 138, 4);
        } else { label.setText(priority != null ? priority : "Low"); bgColor = new Color(219, 234, 254); fgColor = new Color(37, 99, 235); }
        label.setBackground(bgColor); label.setForeground(fgColor);
        return label;
    }

    // --- Button Creation Methods ---
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 13f));
        button.setForeground(Color.BLACK); button.setBackground(PRIMARY);
        button.setFocusPainted(false); button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(PRIMARY.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e) { button.setBackground(PRIMARY); }
        }); return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 13f));
        button.setForeground(TEXT_PRIMARY); button.setBackground(CARD_BG);
        button.setOpaque(true); button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createLineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(9, 19, 9, 19)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(BG); }
            public void mouseExited(java.awt.event.MouseEvent e) { button.setBackground(CARD_BG); }
        }); return button;
    }

     // --- Modern Text Field (Inner Class) ---
    // (Copied from UserNoticeViewer for convenience, can be refactored to a separate file)
    private class ModernTextField extends JTextField {
        private final String placeholder;
        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(customFont.deriveFont(Font.PLAIN, 14f));
            setForeground(TEXT_PRIMARY);
            // Adjust padding
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setBackground(BG); // Use main background for search field
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(hasFocus() ? PRIMARY : BORDER);
            g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus()) {
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                Insets insets = getInsets();
                g2.drawString(placeholder, insets.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
        @Override
        public boolean isOpaque() { return false; }
    }


    // --- Utility Methods ---
    private Font loadCustomFont() {
        try {
            Font poppins = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResourceAsStream("/resource/Poppins-Regular.ttf"))).deriveFont(16f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(poppins);
            return poppins;
        } catch (Exception e) { System.err.println("Error loading font: " + e.getMessage()); return new Font("Segoe UI", Font.PLAIN, 16); }
    }

    private static void consume(Object... ignored) { /* intentionally empty */ }

    // --- Custom Rounded Panel ---
    private static class RoundedPanel extends JPanel {
        private final int radius;
        public RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, radius, radius));
            g2.dispose(); super.paintComponent(g);
        }
        @Override public boolean isOpaque() { return false; }
    }

} // End of UserHomePage class