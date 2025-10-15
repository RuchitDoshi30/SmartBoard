package com.smartboard;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class ManageNoticesPage extends JPanel {
    
    // Modern Color Palette (matching AdminDashboard)
    private static final Color BG = new Color(247, 248, 252);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color DANGER = new Color(239, 68, 68);
    
    // Instance variables passed from MainFrame
    private final MainFrame frame;        
    private final AddNoticePage addNoticePage; 
    
    private final Font customFont;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private ModernTextField searchField;

    public ManageNoticesPage(MainFrame frame, AddNoticePage addNoticePage) {
        setLayout(new BorderLayout());
        setBackground(BG);
        customFont = loadCustomFont();

        this.frame = frame;
        this.addNoticePage = addNoticePage; 
        
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
        
        JLabel pageTitle = new JLabel("Manage Notices");
        pageTitle.setFont(customFont.deriveFont(Font.BOLD, 28f));
        pageTitle.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Edit, delete or update notice status");
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
        
        JButton back = createSecondaryButton("Back");
        
        headerRight.add(searchField);
        headerRight.add(back);

        headerSection.add(headerLeft, BorderLayout.WEST);
        headerSection.add(headerRight, BorderLayout.EAST);
        mainContent.add(headerSection, BorderLayout.NORTH);

        // Table card
        JPanel tableCard = createTableCard(frame);
        mainContent.add(tableCard, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // Actions
        back.addActionListener(e -> {
            consume(e);
            frame.showPage("AdminDashboard");
        });
        
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applySearchFilter();
            }
        });
    }
    
    // This method seems to be a duplicate/unused, but I've left it
    private void loadNoticesFromDB() {
        Object[][] data = loadNoticeData();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // clear table
        for (Object[] row : data) {
            model.addRow(row);
        }
    }

    private JPanel createTableCard(MainFrame frame) {
        RoundedPanel card = new RoundedPanel(12);
        card.setBackground(CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table header with title and add button
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        
        JLabel tableTitle = new JLabel("All Notices");
        tableTitle.setFont(customFont.deriveFont(Font.BOLD, 18f));
        tableTitle.setForeground(TEXT_PRIMARY);
        
        JButton addNew = createPrimaryButton("+ Add New");
        JButton refresh = createSecondaryButton("Refresh");
        
        refresh.addActionListener(e -> {
            consume(e);
            refreshTable();
        });

        addNew.addActionListener(e -> {
            consume(e);
            addNoticePage.resetForm(); // Reset form before showing
            frame.showPage("AddNotice");
        });

        // Your code had two right-button panels, I kept the second one.
        // You can delete the one you don't need.
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);
        rightButtons.add(refresh);
        rightButtons.add(addNew);

        tableHeader.add(tableTitle, BorderLayout.WEST);
        tableHeader.add(rightButtons, BorderLayout.EAST);

        // Create table
        table = createTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BG);

        card.add(tableHeader, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        
        return card;
    }
    
    private void refreshTable() {
        com.smartboard.dao.NoticeDAO dao = new com.smartboard.dao.NoticeDAO();
        List<com.smartboard.entity.Notice> noticeList = dao.getAllNotices();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // clear table

        if (noticeList == null) return; // Guard against null list

        for (com.smartboard.entity.Notice n : noticeList) {
            model.addRow(new Object[]{
                n.getId(),
                n.getTitle(),
                n.getPriority(),
                n.getStatus(),
                new java.text.SimpleDateFormat("yyyy-MM-dd").format(n.getDate()),
                "" // Placeholder for actions column
            });
        }
    }

    private JTable createTable() {
        String[] columns = {"ID", "Title", "Priority", "Status", "Date", "Actions"};
        
        // Load data using the helper method
        Object[][] data = loadNoticeData();
        
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only actions column is "editable" (clickable)
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
        table.setFillsViewportHeight(true); // Ensures table fills the scroll pane

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(TEXT_SECONDARY);
        header.setFont(customFont.deriveFont(Font.BOLD, 12f));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        header.setReorderingAllowed(false); // Disable column reordering

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
                return c;
            }
        };
        cellRenderer.setHorizontalAlignment(JLabel.LEFT);
        
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
        
        // Column widths
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setMaxWidth(60);   // ID
        columnModel.getColumn(1).setPreferredWidth(250); // Title (give it more space)
        columnModel.getColumn(2).setMaxWidth(100);  // Priority
        columnModel.getColumn(3).setMaxWidth(120);  // Status
        columnModel.getColumn(4).setMaxWidth(120);  // Date
        columnModel.getColumn(5).setMinWidth(180);  // Actions
        columnModel.getColumn(5).setMaxWidth(180);
        
        // Custom renderers
        columnModel.getColumn(2).setCellRenderer(new PriorityPillRenderer());
        columnModel.getColumn(3).setCellRenderer(new StatusPillRenderer());
        columnModel.getColumn(5).setCellRenderer(new ActionButtonsRenderer());
        columnModel.getColumn(5).setCellEditor(new ActionButtonsEditor(table));

        // Add sorter for search
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        return table;
    }

    private void applySearchFilter() {
        String text = searchField.getText();
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // (?i) makes the search case-insensitive
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 13f));
        button.setForeground(Color.BLACK); // White text on primary color
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(79, 70, 229)); // Darker shade
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY); // Back to original
            }
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
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(249, 250, 251)); // Slight hover
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(CARD_BG);
            }
        });
        
        return button;
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(customFont.deriveFont(Font.BOLD, 11f));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    // Helper method to load initial data
    private Object[][] loadNoticeData() {
        com.smartboard.dao.NoticeDAO noticeDAO = new com.smartboard.dao.NoticeDAO();
        List<com.smartboard.entity.Notice> noticeList = noticeDAO.getAllNotices();

        if (noticeList != null && !noticeList.isEmpty()) {
            Object[][] data = new Object[noticeList.size()][6];
            for (int i = 0; i < noticeList.size(); i++) {
                com.smartboard.entity.Notice n = noticeList.get(i);
                data[i][0] = n.getId();
                data[i][1] = n.getTitle();
                data[i][2] = n.getPriority();
                data[i][3] = n.getStatus();
                data[i][4] = new java.text.SimpleDateFormat("yyyy-MM-dd").format(n.getDate());
                data[i][5] = ""; // Placeholder for actions
            }
            return data;
        } else {
            return new Object[0][6]; // Empty data
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
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
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

    private class PillLabel extends JLabel {
        private final Color bg;
        public PillLabel(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            setFont(customFont.deriveFont(Font.BOLD, 11f));
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            setForeground(fg);
            setHorizontalAlignment(CENTER);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    // Priority Renderer
    private class PriorityPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String priority = String.valueOf(value);
            Color bg, fg;
            
            if ("High".equalsIgnoreCase(priority)) {
                bg = new Color(254, 226, 226);
                fg = new Color(220, 38, 38);
            } else if ("Medium".equalsIgnoreCase(priority)) {
                bg = new Color(254, 243, 199);
                fg = new Color(202, 138, 4);
            } else {
                bg = new Color(219, 234, 254);
                fg = new Color(37, 99, 235);
            }
            
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            if (isSelected) {
                wrapper.setBackground(tbl.getSelectionBackground());
            } else {
                wrapper.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
            }
            
            PillLabel pill = new PillLabel(priority, bg, fg);
            wrapper.add(pill);
            return wrapper;
        }
    }

    // Status Renderer
    private class StatusPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String status = String.valueOf(value);
            Color bg, fg;
            
            if ("Active".equalsIgnoreCase(status) || "Approved".equalsIgnoreCase(status)) {
                bg = new Color(220, 252, 231);
                fg = new Color(22, 163, 74);
            } else if ("Draft".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
                bg = new Color(241, 245, 249);
                fg = TEXT_SECONDARY;
            } else { // Expired, Rejected, etc.
                bg = new Color(254, 226, 226);
                fg = new Color(220, 38, 38);
            }
            
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            if (isSelected) {
                wrapper.setBackground(tbl.getSelectionBackground());
            } else {
                wrapper.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
            }
            
            PillLabel pill = new PillLabel(status, bg, fg);
            wrapper.add(pill);
            return wrapper;
        }
    }

    // Action Buttons Renderer
    private class ActionButtonsRenderer extends DefaultTableCellRenderer {
        private final JPanel panel;
        private final JButton editButton;
        private final JButton deleteButton;

        public ActionButtonsRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            
            editButton = createActionButton("Edit", new Color(224, 231, 255), PRIMARY);
            deleteButton = createActionButton("Delete", new Color(254, 226, 226), DANGER);
            
            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if (isSelected) {
                panel.setBackground(tbl.getSelectionBackground());
            } else {
                panel.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
            }
            return panel;
        }
    }

    // Action Buttons Editor (for click events)
    private class ActionButtonsEditor extends DefaultCellEditor {
        private final JPanel panel;
        private final JButton editButton;
        private final JButton deleteButton;
        
        public ActionButtonsEditor(JTable table) {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            
            editButton = createActionButton("✏ Edit", new Color(224, 231, 255), PRIMARY);
            deleteButton = createActionButton("🗑 Delete", new Color(254, 226, 226), DANGER);
            
            editButton.addActionListener(e -> {
                int row = table.convertRowIndexToModel(table.getEditingRow());
                if (row >= 0) {
                    int id = (int) table.getModel().getValueAt(row, 0);
                    String title = table.getModel().getValueAt(row, 1).toString();

                    int confirm = JOptionPane.showConfirmDialog(panel,
                        "Edit notice: " + title + "?",
                        "Confirm Edit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        // This is the correct, fixed call
                        addNoticePage.loadNoticeForEdit(id); // load existing data
                        frame.showPage("AddNotice");
                    }
                }
                fireEditingStopped();
            });
            
            deleteButton.addActionListener(e -> {
                int row = table.convertRowIndexToModel(table.getEditingRow());
                if (row >= 0) {
                    int id = (int) table.getModel().getValueAt(row, 0);
                    String title = table.getModel().getValueAt(row, 1).toString();

                    int confirm = JOptionPane.showConfirmDialog(panel,
                        "Are you sure you want to delete '" + title + "'?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        com.smartboard.dao.NoticeDAO dao = new com.smartboard.dao.NoticeDAO();
                        boolean deleted = dao.deleteNotice(id);

                        if (deleted) {
                            ((DefaultTableModel) table.getModel()).removeRow(row);
                            JOptionPane.showMessageDialog(panel, "Notice deleted successfully!");
                        } else {
                            JOptionPane.showMessageDialog(panel, "Failed to delete notice!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                fireEditingStopped();
            });
            
            panel.add(editButton);
            panel.add(deleteButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}