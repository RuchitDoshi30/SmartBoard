package com.smartboard;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat; // Needed for date formatting
import java.util.ArrayList;       // Needed for List
import java.util.List;          // Needed for List
import java.util.Objects;
// Import your Notice entity and DAO
import com.smartboard.entity.Notice;
import com.smartboard.dao.NoticeDAO;

public class UserNoticeViewer extends JPanel {

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
    private final MainFrame mainFrame; // Store MainFrame reference
    private final NoticeDAO noticeDAO; // DAO for database access

    public UserNoticeViewer(MainFrame frame) {
        this.mainFrame = frame; // Store frame reference
        this.noticeDAO = new NoticeDAO(); // Initialize DAO
        setLayout(new BorderLayout());
        setBackground(BG);
        customFont = loadCustomFont();

        // Main content area with padding
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG);
        mainContent.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header section
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setOpaque(false);
        headerSection.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        // Left: Title and subtitle
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel pageTitle = new JLabel("Browse Notices");
        pageTitle.setFont(customFont.deriveFont(Font.BOLD, 28f));
        pageTitle.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("View all published notices and announcements");
        subtitle.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        subtitle.setForeground(TEXT_SECONDARY);

        headerLeft.add(pageTitle);
        headerLeft.add(Box.createRigidArea(new Dimension(0, 4)));
        headerLeft.add(subtitle);

        // Right: Search and Back button
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);

        searchField = new ModernTextField("Search notices...");
        searchField.setPreferredSize(new Dimension(280, 40));

        JButton back = createSecondaryButton("Back to Home");

        headerRight.add(searchField);
        headerRight.add(back);

        headerSection.add(headerLeft, BorderLayout.WEST);
        headerSection.add(headerRight, BorderLayout.EAST);
        mainContent.add(headerSection, BorderLayout.NORTH);

        // Table card - Pass frame to createTableCard
        JPanel tableCard = createTableCard(frame);
        mainContent.add(tableCard, BorderLayout.CENTER);

        // Wrap main content (which now includes header and table card) in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER); // Add the main scroll pane to the UserNoticeViewer panel

        // Actions
        back.addActionListener(e -> {
            consume(e);
            frame.showPage("UserHome");
        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });

        // Load initial data into the table
        loadTableData();
    }

    private JPanel createTableCard(MainFrame frame) { // Accept MainFrame
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table header with title and filter pills
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel tableTitle = new JLabel("Available Notices");
        tableTitle.setFont(customFont.deriveFont(Font.BOLD, 18f));
        tableTitle.setForeground(TEXT_PRIMARY);

        // Filter pills
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        PillToggle allPill = new PillToggle("All", true);
        PillToggle highPill = new PillToggle("High Priority", false);
        PillToggle mediumPill = new PillToggle("Medium Priority", false);
        // Assuming you might add a Low priority filter later
        // PillToggle lowPill = new PillToggle("Low Priority", false);

        allPill.addActionListener(e -> {
            currentFilter = "All";
            allPill.setSelected(true);
            highPill.setSelected(false);
            mediumPill.setSelected(false);
            // lowPill.setSelected(false);
            applyFilters();
        });

        highPill.addActionListener(e -> {
            currentFilter = "High";
            allPill.setSelected(false);
            highPill.setSelected(true);
            mediumPill.setSelected(false);
            // lowPill.setSelected(false);
            applyFilters();
        });

        mediumPill.addActionListener(e -> {
            currentFilter = "Medium";
            allPill.setSelected(false);
            highPill.setSelected(false);
            mediumPill.setSelected(true);
            // lowPill.setSelected(false);
            applyFilters();
        });

        // lowPill.addActionListener(e -> { ... });

        filterPanel.add(allPill);
        filterPanel.add(highPill);
        filterPanel.add(mediumPill);
        // filterPanel.add(lowPill);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(filterPanel, BorderLayout.EAST);

        // Create table structure
        table = createTableStructure(frame); // Pass frame to createTableStructure
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);

        card.add(tableHeader, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

     // Creates table structure, data loaded separately
    private JTable createTableStructure(MainFrame frame) { // Accept MainFrame
        String[] columns = {"ID", "Title", "Priority", "Published Date", "View"};

        // Start with an empty model
        DefaultTableModel model = new DefaultTableModel(new Object[0][columns.length], columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only View column is clickable
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(56);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD_BG);
        table.setFont(customFont.deriveFont(Font.PLAIN, 14f));
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(TEXT_SECONDARY);
        header.setFont(customFont.deriveFont(Font.BOLD, 12f));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        header.setReorderingAllowed(false);

        // Column renderers
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
                    c.setForeground(TEXT_PRIMARY);
                }
                 // Add tooltip for Title column (index 1)
                if (col == 1 && value != null) {
                    setToolTipText(value.toString());
                } else {
                    setToolTipText(null);
                }
                return c;
            }
        };
        cellRenderer.setHorizontalAlignment(JLabel.LEFT);

        // Apply default renderer to non-custom columns
        table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer); // ID
        table.getColumnModel().getColumn(1).setCellRenderer(cellRenderer); // Title
        table.getColumnModel().getColumn(3).setCellRenderer(cellRenderer); // Date

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(60);       // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(400); // Title (give more space)
        table.getColumnModel().getColumn(2).setMaxWidth(130);      // Priority
        table.getColumnModel().getColumn(3).setMaxWidth(140);      // Date
        table.getColumnModel().getColumn(4).setMinWidth(120);      // View
        table.getColumnModel().getColumn(4).setMaxWidth(120);

        // Custom renderers and editor
        table.getColumnModel().getColumn(2).setCellRenderer(new PriorityPillRenderer()); // Priority
        table.getColumnModel().getColumn(4).setCellRenderer(new ViewButtonRenderer());   // View Button Appearance
        table.getColumnModel().getColumn(4).setCellEditor(new ViewButtonEditor(table, frame)); // View Button Action

        // Add sorter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        return table;
    }

    // --- Data Loading ---
    private void loadTableData() {
        List<Notice> noticeList = noticeDAO.getAllNotices(); // Fetch data
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear existing table data

        if (noticeList != null && !noticeList.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Notice notice : noticeList) {
                 // Only add Approved/Active notices
                if ("Approved".equalsIgnoreCase(notice.getStatus()) || "Active".equalsIgnoreCase(notice.getStatus())) {
                    model.addRow(new Object[]{
                        notice.getId(),
                        notice.getTitle(),
                        notice.getPriority(),
                        (notice.getDate() != null) ? sdf.format(notice.getDate()) : "", // Format date
                        "" // Placeholder for the view button column
                    });
                }
            }
        } else {
             System.out.println("No notices found in database or DAO returned null/empty.");
             // Optionally add a row indicating no data, or leave table empty
        }
        // Apply initial filters if needed (e.g., if default filter isn't "All")
        applyFilters();
    }


    private void applyFilters() {
        String searchText = searchField.getText();
        RowFilter<Object, Object> combinedFilter = null;
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Add search filter (searches Title - column 1)
        if (!searchText.trim().isEmpty()) {
             // (?i) for case-insensitive search on Title column (index 1)
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 1));
        }

        // 2. Add priority filter (searches Priority - column 2)
        if (!"All".equals(currentFilter)) {
             // Exact match filter for priority column (index 2)
            filters.add(RowFilter.regexFilter("^" + currentFilter + "$", 2));
        }

        // Combine filters using AND logic
        if (!filters.isEmpty()) {
            combinedFilter = RowFilter.andFilter(filters);
        }

        sorter.setRowFilter(combinedFilter); // Apply the combined filter (or null if no filters)
    }

    // --- Button Creation Methods ---
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
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(249, 250, 251)); // Lighter hover
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(CARD_BG);
            }
        });

        return button;
    }

    private JButton createViewButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 12f));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Add hover effect for view button
         button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY);
            }
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

    // --- Custom Components --- (RoundedPanel, ModernTextField, PillToggle, PillLabel)

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
            // Simple background fill, no shadow needed here if it's just a container
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g); // Paint children
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
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setBackground(CARD_BG);
            setOpaque(false); // Let paintComponent handle drawing
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
            super.paintComponent(g); // Paint text etc.
            if (getText().isEmpty() && !hasFocus()) { // Paint placeholder
                g2.setColor(TEXT_SECONDARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                Insets insets = getInsets();
                g2.drawString(placeholder, insets.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
        @Override
        public boolean isOpaque() { return false; } // Handled by paintComponent
    }


    private class PillToggle extends JButton {
        private boolean selected;
        public PillToggle(String text, boolean selected) {
            super(text);
            this.selected = selected;
            setFont(customFont.deriveFont(Font.BOLD, 12f));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            updateStyle();
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e){ if(!selected) setForeground(PRIMARY); repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e) { updateStyle(); repaint(); }
            });
        }
        @Override public void setSelected(boolean selected) { if (this.selected != selected) { this.selected = selected; updateStyle(); repaint(); } }
        @Override public boolean isSelected() { return selected; }
        private void updateStyle() { setForeground(selected ? Color.WHITE : TEXT_SECONDARY); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            } else {
                 Point mousePos = getMousePosition();
                 boolean hovered = (mousePos != null);
                 g2.setColor(hovered ? new Color(235, 237, 255) : CARD_BG);
                 g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                 g2.setColor(hovered ? PRIMARY : BORDER);
                 g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public Dimension getPreferredSize() { Dimension size = super.getPreferredSize(); return new Dimension(size.width + 24, 32); }
        @Override public boolean isOpaque() { return false; }
    }


    private class PillLabel extends JLabel {
        private Color pillBackground; // Renamed from bg to avoid conflict
        public PillLabel(String text, Color bg, Color fg) {
            super(text);
            this.pillBackground = bg;
            setFont(customFont.deriveFont(Font.BOLD, 11f));
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            setForeground(fg);
            setHorizontalAlignment(CENTER);
            setOpaque(false);
        }
        // Add a setter if needed, though PriorityPillRenderer directly sets pillBackground
        public void setPillBackground(Color bg) {
            this.pillBackground = bg;
            repaint(); // Repaint when background color changes
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(pillBackground); // Use the member variable
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight()); // Use height for round caps
            g2.dispose();
            super.paintComponent(g);
        }
         @Override public boolean isOpaque() { return false; }
    }


    // Priority Renderer
    private class PriorityPillRenderer extends DefaultTableCellRenderer {
        private final JPanel wrapper;
        private final PillLabel pill;
        public PriorityPillRenderer() {
            wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // Padding around pill
            pill = new PillLabel("", Color.GRAY, Color.BLACK); // Initial placeholder
            wrapper.add(pill);
            setOpaque(false); // Renderer itself is transparent
        }
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String priority = String.valueOf(value);
            Color bg, fg;
            if ("High".equalsIgnoreCase(priority)) {
                bg = new Color(254, 226, 226); fg = new Color(220, 38, 38);
            } else if ("Medium".equalsIgnoreCase(priority)) {
                bg = new Color(254, 243, 199); fg = new Color(202, 138, 4);
            } else { // Low or other
                bg = new Color(219, 234, 254); fg = new Color(37, 99, 235);
                 priority = (value != null) ? priority : "Low"; // Handle null priority
            }

            pill.setText(priority);
            pill.setForeground(fg);
            pill.setPillBackground(bg); // Set the background color for the pill to draw

            // Set wrapper background based on selection/striping
            if (isSelected) {
                wrapper.setBackground(tbl.getSelectionBackground());
            } else {
                wrapper.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
            }
            wrapper.setOpaque(true); // Wrapper needs to be opaque to show its background
            return wrapper;
        }
    }


    // View Button Renderer (Shows the button)
    private class ViewButtonRenderer extends DefaultTableCellRenderer {
        private final JPanel panel;
        private final JButton viewButton;
        public ViewButtonRenderer() {
             panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
             panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); // Padding for cell
             viewButton = createViewButton("👁 View");
             panel.add(viewButton);
             setOpaque(false); // Renderer is transparent
        }
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
             // Set panel background based on selection/striping
            if (isSelected) {
                panel.setBackground(tbl.getSelectionBackground());
            } else {
                panel.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
            }
            panel.setOpaque(true); // Panel needs to be opaque
            return panel;
        }
    }


 // View Button Editor (Handles clicks)
    private class ViewButtonEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton viewButton;
        private final MainFrame frame;
        private int currentNoticeId; // Store ID when editing starts

        public ViewButtonEditor(JTable table, MainFrame frame) {
            super(new JCheckBox()); // Required dummy component
            this.frame = frame;

            // Create the panel that will BE the editor
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            viewButton = createViewButton("👁 View"); // Assign the button directly
            panel.add(viewButton);
            panel.setOpaque(true);

            setClickCountToStart(1); // Start editing on single click

            // Action listener for the actual button inside the editor component
            viewButton.addActionListener(e -> {
                // Use the stored notice ID
                if (currentNoticeId > 0) { // Check if ID is valid
                    // Call the correct method in MainFrame
                    frame.showNoticeDetails(currentNoticeId);
                }
                fireEditingStopped(); // Important: Stop editing after action
            });
        }

        // Removed the unused createEditorComponent method

        // Called when editing starts - Store the notice ID from the model
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Get notice ID from the model (column 0) for the current editing row
            int modelRow = table.convertRowIndexToModel(row);
            Object idObject = table.getModel().getValueAt(modelRow, 0);
            // Safely convert ID to int
            currentNoticeId = (idObject instanceof Number) ? ((Number) idObject).intValue() : -1;

            // Set background of the editor panel based on selection
            panel.setBackground(table.getSelectionBackground()); // Set background on the existing panel
            return panel; // <-- Return the panel instance variable
        }

        // Return value after editing (not really used here)
        @Override
        public Object getCellEditorValue() {
            return "";
        }

        // Ensures the editor stops immediately after button click
        @Override
        public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }

} // End of UserNoticeViewer class