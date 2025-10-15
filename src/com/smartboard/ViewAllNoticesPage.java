package com.smartboard;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent; // Added for Timer
import java.awt.event.ActionListener; // Added for Timer
import java.text.SimpleDateFormat; // Import SimpleDateFormat
import java.util.ArrayList; // Import ArrayList
import java.util.Date; // Added for Timestamp
import java.util.List;      // Import List
import java.util.Objects;
// Import your Notice entity and DAO
import com.smartboard.entity.Notice;
import com.smartboard.dao.NoticeDAO;


public class ViewAllNoticesPage extends JPanel {

    // Modern Color Palette
    private static final Color BG = new Color(247, 248, 252);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    private final Font customFont;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private ModernTextField searchField;
    private String currentFilter = "All";
    private final NoticeDAO noticeDAO;
    private final MainFrame mainFrame; // Added MainFrame reference

    // --- Added for Refresh ---
    private Timer refreshTimer;
    private JLabel lastRefreshedLabel;
    private SimpleDateFormat timestampFormat;
    // --- End Added ---


    public ViewAllNoticesPage(MainFrame frame) { // Accept MainFrame
        this.mainFrame = frame; // Store MainFrame reference
        setLayout(new BorderLayout());
        setBackground(BG);
        customFont = loadCustomFont();
        this.noticeDAO = new NoticeDAO();
        timestampFormat = new SimpleDateFormat("hh:mm:ss a"); // Format like "01:55:30 PM"

        // Main content area with padding
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG);
        mainContent.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // --- Header section ---
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setOpaque(false);
        headerSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        // Left: Title and subtitle
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel pageTitle = new JLabel("View All Notices");
        pageTitle.setFont(customFont.deriveFont(Font.BOLD, 28f));
        pageTitle.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Browse all published notices in the system");
        subtitle.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        subtitle.setForeground(TEXT_SECONDARY);

        headerLeft.add(pageTitle);
        headerLeft.add(Box.createRigidArea(new Dimension(0, 4)));
        headerLeft.add(subtitle);

        // --- Right: Search, Timestamp, Refresh, Back ---
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);

        searchField = new ModernTextField("Search notices...");
        searchField.setPreferredSize(new Dimension(280, 40));

        // Timestamp Label
        lastRefreshedLabel = new JLabel("Last Refreshed: --:--:-- --");
        lastRefreshedLabel.setFont(customFont.deriveFont(Font.PLAIN, 12f));
        lastRefreshedLabel.setForeground(TEXT_SECONDARY);

        // Refresh Button
        JButton refreshButton = createSecondaryButton("🔄 Refresh");
        refreshButton.addActionListener(e -> refreshTableData()); // Call refresh method on click

        // Back Button
        JButton back = createSecondaryButton("Back"); // Keep 'Back' as Admin might use this

        headerRight.add(searchField);
        headerRight.add(lastRefreshedLabel); // Add timestamp
        headerRight.add(refreshButton);      // Add refresh button
        headerRight.add(back);               // Add back button

        headerSection.add(headerLeft, BorderLayout.WEST);
        headerSection.add(headerRight, BorderLayout.EAST);
        mainContent.add(headerSection, BorderLayout.NORTH);
        // --- End Header ---


        // Table card
        JPanel tableCard = createTableCard();
        mainContent.add(tableCard, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // Actions
        back.addActionListener(e -> {
            consume(e);
            stopAutoRefresh(); // Stop timer when leaving page
            frame.showPage("AdminDashboard"); // Go back to Admin Dashboard
        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });

        // Initial data load & start timer
        refreshTableData(); // Initial load and timestamp set
        startAutoRefresh(); // Start the 30-second timer
    }

     // --- Refresh Logic ---
    private void refreshTableData() {
        System.out.println("Refreshing ViewAllNotices table..."); // Log refresh
        loadTableData(); // Reload data from DB into the table model
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
                System.out.println("Auto-refresh triggered (ViewAllNotices)..."); // Log auto-refresh
                loadTableData(); // Reload data
                lastRefreshedLabel.setText("Last Refreshed: " + timestampFormat.format(new Date())); // Update time
            };
            // Timer triggers every 30000 ms (30 seconds)
            refreshTimer = new Timer(30000, refreshAction);
            refreshTimer.setInitialDelay(30000); // Wait 30s before first auto-refresh
            refreshTimer.setRepeats(true);       // Keep repeating
        }
        if (!refreshTimer.isRunning()) {
            refreshTimer.start();
            System.out.println("Auto-refresh timer started (ViewAllNotices).");
        }
    }

    // Stops the automatic refresh timer
    // Call this when navigating away from this page
    public void stopAutoRefresh() { // Make public if called from MainFrame
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
            System.out.println("Auto-refresh timer stopped (ViewAllNotices).");
        }
    }
    // --- End Refresh Logic ---


    private JPanel createTableCard() {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table header with title and filter pills
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel tableTitle = new JLabel("All Active Notices");
        tableTitle.setFont(customFont.deriveFont(Font.BOLD, 18f));
        tableTitle.setForeground(TEXT_PRIMARY);

        // Filter pills
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        PillToggle allPill = new PillToggle("All", true);
        PillToggle highPill = new PillToggle("High Priority", false);
        PillToggle mediumPill = new PillToggle("Medium Priority", false);
        PillToggle lowPill = new PillToggle("Low Priority", false);

        allPill.addActionListener(e -> {
            currentFilter = "All"; allPill.setSelected(true); highPill.setSelected(false); mediumPill.setSelected(false); lowPill.setSelected(false); applyFilters();
        });
        highPill.addActionListener(e -> {
            currentFilter = "High"; allPill.setSelected(false); highPill.setSelected(true); mediumPill.setSelected(false); lowPill.setSelected(false); applyFilters();
        });
        mediumPill.addActionListener(e -> {
            currentFilter = "Medium"; allPill.setSelected(false); highPill.setSelected(false); mediumPill.setSelected(true); lowPill.setSelected(false); applyFilters();
        });
        lowPill.addActionListener(e -> {
            currentFilter = "Low"; allPill.setSelected(false); highPill.setSelected(false); mediumPill.setSelected(false); lowPill.setSelected(true); applyFilters();
        });

        filterPanel.add(allPill); filterPanel.add(highPill); filterPanel.add(mediumPill); filterPanel.add(lowPill);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(filterPanel, BorderLayout.EAST);

        // Create table structure
        table = createTableStructure();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);

        card.add(tableHeader, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    // Creates the table structure but doesn't load data initially
    private JTable createTableStructure() {
        String[] columns = {"ID", "Title", "Priority", "Status", "Published Date", "Description"};

        DefaultTableModel model = new DefaultTableModel(new Object[0][6], columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(52); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD_BG); table.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        table.setSelectionBackground(new Color(224, 231, 255)); table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(249, 250, 251)); header.setForeground(TEXT_SECONDARY);
        header.setFont(customFont.deriveFont(Font.BOLD, 12f)); header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        header.setReorderingAllowed(false);

        // Column renderers
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) { c.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251)); c.setForeground(TEXT_PRIMARY); }
                if (col == 5 && value != null) { setToolTipText(value.toString()); } else { setToolTipText(null); } // Tooltip for Description
                 if (col == 1 && value != null) { setToolTipText(value.toString()); } // Tooltip for Title
                return c;
            }
        };
        cellRenderer.setHorizontalAlignment(JLabel.LEFT);

        for (int i = 0; i < table.getColumnCount(); i++) { if (i != 2 && i != 3) { table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer); } }

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(60); table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setMaxWidth(120); table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(140); table.getColumnModel().getColumn(5).setPreferredWidth(300);

        // Custom renderers
        table.getColumnModel().getColumn(2).setCellRenderer(new PriorityPillRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusPillRenderer());

        sorter = new TableRowSorter<>(model); table.setRowSorter(sorter);
        return table;
    }

    // Renamed from refreshTable to avoid confusion with the action method
    private void loadTableData() {
        List<com.smartboard.entity.Notice> noticeList = noticeDAO.getAllNotices();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        if (noticeList != null && !noticeList.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (com.smartboard.entity.Notice n : noticeList) {
                // Add ALL notices here, filters will handle visibility
                model.addRow(new Object[]{
                    n.getId(), n.getTitle(), n.getPriority(), n.getStatus(),
                    (n.getDate() != null) ? sdf.format(n.getDate()) : "",
                    n.getDescription()
                });
            }
        }
        applyFilters(); // Apply filters after loading all data
    }


    private void applyFilters() {
        String searchText = searchField.getText();
        RowFilter<Object, Object> combinedFilter = null;
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Search filter (searches Title - column 1, and Description - column 5)
        if (!searchText.trim().isEmpty()) {
            String regex = "(?i)" + searchText; // Case-insensitive
             List<RowFilter<Object, Object>> searchFilters = new ArrayList<>();
             searchFilters.add(RowFilter.regexFilter(regex, 1)); // Title
             searchFilters.add(RowFilter.regexFilter(regex, 5)); // Description
            filters.add(RowFilter.orFilter(searchFilters)); // Match if EITHER Title OR Description contains text
        }

        // Priority filter (column 2)
        if (!"All".equals(currentFilter)) {
            filters.add(RowFilter.regexFilter("^" + currentFilter + "$", 2));
        }

        // Combine filters using AND
        if (!filters.isEmpty()) {
            combinedFilter = RowFilter.andFilter(filters);
        }

        sorter.setRowFilter(combinedFilter);
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 13f));
        button.setForeground(TEXT_PRIMARY); button.setBackground(CARD_BG);
        button.setOpaque(true); button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createLineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(9, 19, 9, 19)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(new Color(249, 250, 251)); }
            public void mouseExited(java.awt.event.MouseEvent e) { button.setBackground(CARD_BG); }
        }); return button;
    }

    private Font loadCustomFont() {
        try {
            Font poppins = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(getClass().getResourceAsStream("/resource/Poppins-Regular.ttf"))).deriveFont(16f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(poppins);
            return poppins;
        } catch (Exception e) { e.printStackTrace(); return new Font("Segoe UI", Font.PLAIN, 16); }
    }

    private static void consume(Object... ignored) { /* intentionally empty */ }

    // --- Custom Components --- (RoundedPanel, ModernTextField, PillToggle, PillLabel, Renderers)

    private static class RoundedPanel extends JPanel { /* ... Same as before ... */
        private final int radius;
        public RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 10)); g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 6, radius, radius);
            g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius); g2.dispose();
            super.paintComponent(g);
        }
        @Override public boolean isOpaque() { return false; }
    }

    private class ModernTextField extends JTextField { /* ... Same as before ... */
        private final String placeholder;
        public ModernTextField(String placeholder) {
            this.placeholder = placeholder; setFont(customFont.deriveFont(Font.PLAIN, 14f));
            setForeground(TEXT_PRIMARY); setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setBackground(CARD_BG); setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(hasFocus() ? PRIMARY : BORDER); g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus()) { g2.setColor(TEXT_SECONDARY); g2.setFont(getFont()); FontMetrics fm = g2.getFontMetrics(); Insets insets = getInsets(); g2.drawString(placeholder, insets.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent()); }
            g2.dispose();
        }
        @Override public boolean isOpaque() { return false; }
    }

    private class PillToggle extends JButton { /* ... Same as before ... */
        private boolean selected;
        public PillToggle(String text, boolean selected) {
            super(text); this.selected = selected; setFont(customFont.deriveFont(Font.BOLD, 12f));
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); updateStyle();
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e){ if(!selected) setForeground(PRIMARY); repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e) { updateStyle(); repaint(); }
            });
        }
        @Override public void setSelected(boolean selected) { if (this.selected != selected) { this.selected = selected; updateStyle(); repaint(); } }
        @Override public boolean isSelected() { return selected; }
        private void updateStyle() { setForeground(selected ? Color.WHITE : TEXT_SECONDARY); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) { g2.setColor(PRIMARY); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); }
            else { Point mousePos = getMousePosition(); boolean hovered = (mousePos != null); g2.setColor(hovered ? new Color(235, 237, 255) : CARD_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); g2.setColor(hovered ? PRIMARY : BORDER); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20); }
            g2.dispose(); super.paintComponent(g);
        }
        @Override public Dimension getPreferredSize() { Dimension size = super.getPreferredSize(); return new Dimension(size.width + 24, 32); }
        @Override public boolean isOpaque() { return false; }
    }

    private class PillLabel extends JLabel { /* ... Same as before ... */
        private Color pillBackground;
        public PillLabel(String text, Color bg, Color fg) {
            super(text); this.pillBackground = bg; setFont(customFont.deriveFont(Font.BOLD, 11f));
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12)); setForeground(fg);
            setHorizontalAlignment(CENTER); setOpaque(false);
        }
        public void setPillBackground(Color bg) { this.pillBackground = bg; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(pillBackground); g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); g2.dispose();
            super.paintComponent(g);
        }
        @Override public boolean isOpaque() { return false; }
    }

    private class PriorityPillRenderer extends DefaultTableCellRenderer { /* ... Same as before ... */
        private final JPanel wrapper; private final PillLabel pill;
        public PriorityPillRenderer() { wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); wrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); pill = new PillLabel("", Color.GRAY, Color.BLACK); wrapper.add(pill); setOpaque(false); }
        @Override public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String priority = String.valueOf(value); Color bg, fg;
            if ("High".equalsIgnoreCase(priority)) { bg = new Color(254, 226, 226); fg = new Color(220, 38, 38); }
            else if ("Medium".equalsIgnoreCase(priority)) { bg = new Color(254, 243, 199); fg = new Color(202, 138, 4); }
            else { bg = new Color(219, 234, 254); fg = new Color(37, 99, 235); priority = (value != null) ? priority : "Low"; }
            pill.setText(priority); pill.setForeground(fg); pill.setPillBackground(bg);
            if (isSelected) { wrapper.setBackground(tbl.getSelectionBackground()); } else { wrapper.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251)); }
            wrapper.setOpaque(true); return wrapper;
        }
    }

    private class StatusPillRenderer extends DefaultTableCellRenderer { /* ... Same as before ... */
        private final JPanel wrapper; private final PillLabel pill;
        public StatusPillRenderer() { wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); wrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); pill = new PillLabel("", Color.WHITE, Color.BLACK); wrapper.add(pill); setOpaque(false); }
        @Override public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String status = String.valueOf(value); Color bg, fg;
            if ("Active".equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status)) { bg = new Color(220, 252, 231); fg = new Color(22, 163, 74); }
            else if ("Pending".equalsIgnoreCase(status) || "Draft".equalsIgnoreCase(status)) { bg = new Color(241, 245, 249); fg = TEXT_SECONDARY; }
            else { bg = new Color(254, 226, 226); fg = new Color(220, 38, 38); status = (value != null) ? status : "N/A"; } // Handle null/other
            pill.setText(status); pill.setForeground(fg); pill.setPillBackground(bg);
            if (isSelected) { wrapper.setBackground(tbl.getSelectionBackground()); } else { wrapper.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251)); }
            wrapper.setOpaque(true); return wrapper;
        }
    }

} // End of ViewAllNoticesPage class