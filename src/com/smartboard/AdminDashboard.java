package com.smartboard;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

public class AdminDashboard extends JPanel {

  // -------------------------
  // Modern Color Palette (Stratify-inspired)
  // -------------------------
  private static final Color BG = new Color(247, 248, 252); // Light purple-gray background
  private static final Color SIDEBAR_BG = new Color(255, 255, 255); // White sidebar
  private static final Color CARD_BG = new Color(255, 255, 255); // White cards
  private static final Color PRIMARY = new Color(99, 102, 241); // Indigo/purple primary
  private static final Color PRIMARY_LIGHT = new Color(224, 231, 255); // Light purple
  private static final Color TEXT_PRIMARY = new Color(30, 41, 59); // Slate dark
  private static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Slate gray
  private static final Color BORDER = new Color(226, 232, 240); // Light border

  private final Font customFont;
  private final JPanel cardsPanel;
  private final JTable table;
  private TableRowSorter<DefaultTableModel> sorter;
  private ModernTextField searchField;
  private String activeStatus = "All";

  public AdminDashboard(MainFrame frame) {
    setLayout(new BorderLayout());
    setBackground(BG);
    customFont = loadCustomFont();

    // -------------------------
    // Sidebar (Modern white sidebar with icons)
    // -------------------------
    JPanel sidebar = new JPanel();
    sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
    sidebar.setBackground(SIDEBAR_BG);
    sidebar.setPreferredSize(new Dimension(240, getHeight()));
    sidebar.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER),
        BorderFactory.createEmptyBorder(24, 16, 24, 16)
      )
    );

    // Logo/Brand
    JLabel brand = new JLabel("SmartBoard");
    brand.setFont(customFont.deriveFont(Font.BOLD, 22f));
    brand.setForeground(PRIMARY);
    brand.setBorder(BorderFactory.createEmptyBorder(0, 8, 24, 0));
    sidebar.add(brand);

    NavButton dashboardBtn = createSidebarButton("Dashboard");
    NavButton addNotice = createSidebarButton("Add Notice");
    NavButton manageNotices = createSidebarButton("Manage Notices");
    NavButton viewNotices = createSidebarButton("View Notices");
    dashboardBtn.setSelected(true);

    sidebar.add(dashboardBtn);
    sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
    sidebar.add(addNotice);
    sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
    sidebar.add(manageNotices);
    sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
    sidebar.add(viewNotices);
    sidebar.add(Box.createVerticalGlue());
    add(sidebar, BorderLayout.WEST);

    // -------------------------
    // Main Content
    // -------------------------
    JPanel mainContent = new JPanel(new BorderLayout());
    mainContent.setBackground(BG);
    mainContent.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    // -------------------------
    // Header (Top bar with title, search, user)
    // -------------------------
    JPanel header = new JPanel(new BorderLayout(16, 0));
    header.setOpaque(false);
    header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

    // Left: Title + subtitle
    JPanel headerLeft = new JPanel();
    headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
    headerLeft.setOpaque(false);
    JLabel pageTitle = new JLabel("Dashboard");
    pageTitle.setFont(customFont.deriveFont(Font.BOLD, 28f));
    pageTitle.setForeground(TEXT_PRIMARY);
    JLabel subtitle = new JLabel("Welcome back, Admin");
    subtitle.setFont(customFont.deriveFont(Font.PLAIN, 14f));
    subtitle.setForeground(TEXT_SECONDARY);
    headerLeft.add(pageTitle);
    headerLeft.add(Box.createRigidArea(new Dimension(0, 4)));
    headerLeft.add(subtitle);

    // Right: Search + user actions
    JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
    headerRight.setOpaque(false);
    
    
// --- REFRESH BUTTON ---
JButton refreshBtn = new JButton("Refresh");
refreshBtn.setFont(customFont.deriveFont(Font.BOLD, 13f));
refreshBtn.setForeground(TEXT_SECONDARY);
refreshBtn.setBackground(CARD_BG);
refreshBtn.setBorder(
    BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER, 1, true),
        BorderFactory.createEmptyBorder(8, 16, 8, 16)
    )
);
refreshBtn.setFocusPainted(false);
refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
refreshBtn.addActionListener(e -> {
    refreshTable();   // Reload table data
    refreshStats();   // Reload stats cards
});


    searchField = new ModernTextField("Search notices...");
    searchField.setPreferredSize(new Dimension(280, 40));

    JButton logout = new JButton("Logout");
    logout.setFont(customFont.deriveFont(Font.BOLD, 13f));
    logout.setForeground(TEXT_SECONDARY);
    logout.setBackground(CARD_BG);
    logout.setBorder(
      BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER, 1, true),
        BorderFactory.createEmptyBorder(8, 16, 8, 16)
      )
    );
    logout.setFocusPainted(false);
    logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    headerRight.add(searchField);
    headerRight.add(logout);

    header.add(headerLeft, BorderLayout.WEST);
    headerRight.add(refreshBtn); // <<< only add this
    header.add(headerRight, BorderLayout.EAST);
    mainContent.add(header, BorderLayout.NORTH);

    // -------------------------
    // Stats Cards
    // -------------------------
    cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
    cardsPanel.setOpaque(false);
    cardsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
    mainContent.add(cardsPanel, BorderLayout.NORTH);
    // Adjust layout: header at NORTH, cards also at NORTH - we'll use a wrapper
    JPanel topSection = new JPanel(new BorderLayout());
    topSection.setOpaque(false);
    topSection.add(header, BorderLayout.NORTH);
    topSection.add(cardsPanel, BorderLayout.CENTER);
    mainContent.add(topSection, BorderLayout.NORTH);
    addStatsCards();

    // -------------------------
    // Status filter tabs
    // -------------------------
    JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    filterBar.setOpaque(false);
    filterBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

    PillToggle allChip = new PillToggle("All");
    allChip.setSelected(true);
    PillToggle approvedChip = new PillToggle("Approved");
    PillToggle pendingChip = new PillToggle("Pending");
    ButtonGroup statusGroup = new ButtonGroup();
    statusGroup.add(allChip);
    statusGroup.add(approvedChip);
    statusGroup.add(pendingChip);
    java.awt.event.ActionListener statusListener = e -> {
      activeStatus = ((JToggleButton) e.getSource()).getText();
      applyFilters();
    };
    allChip.addActionListener(statusListener);
    approvedChip.addActionListener(statusListener);
    pendingChip.addActionListener(statusListener);

    filterBar.add(allChip);
    filterBar.add(approvedChip);
    filterBar.add(pendingChip);

    // -------------------------
    // Data Table
    // -------------------------
    table = createTable();
    JPanel tableCard = wrapInCard(table, "Recent Notices", filterBar);
    mainContent.add(tableCard, BorderLayout.CENTER);

    add(mainContent, BorderLayout.CENTER);

    // -------------------------
    // Actions
    // -------------------------
  addNotice.addActionListener(e -> {
  consume(e);
  selectSidebar(addNotice, dashboardBtn, manageNotices, viewNotices);

  // Reset the Add Notice form before showing
  frame.addNoticePage.resetForm();  
  frame.showPage("AddNotice");
});

    manageNotices.addActionListener(e -> {
      consume(e);
      selectSidebar(manageNotices, dashboardBtn, addNotice, viewNotices);
      frame.showPage("ManageNotices");
    });
    viewNotices.addActionListener(e -> {
      consume(e);
      selectSidebar(viewNotices, dashboardBtn, addNotice, manageNotices);
      frame.showPage("ViewNotices");
    });
    dashboardBtn.addActionListener(e -> {
      consume(e);
      selectSidebar(dashboardBtn, addNotice, manageNotices, viewNotices);
      frame.showPage("AdminDashboard");
    });
    logout.addActionListener(e -> {
      consume(e);
      int confirm = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to logout?",
        "Confirm Logout",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
      );
      if (confirm == JOptionPane.YES_OPTION) {
        frame.showPage("UserHome");
      }
    });
    searchField.addActionListener(e -> {
      consume(e);
      applyFilters();
    });
    searchField.addKeyListener(
      new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
          applyFilters();
        }
      }
    );
  }

  // -------------------------
  // Table
  // -------------------------
private JTable createTable() {
    String[] columns = { "ID", "Title", "Description", "Priority", "Status", "Date", "Published By", "Attachment" };

    // Make model non-editable
    DefaultTableModel model = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // <- makes the entire table non-editable
        }
    };

    try {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/smartboard", "root", "@JD0890");
        Statement stmt = con.createStatement();
        String sql = "SELECT id, title, description, priority, status, date, published_by, attachment_path FROM notices ORDER BY date DESC";
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("priority"),
                rs.getString("status"),
                rs.getDate("date").toString(),
                rs.getString("published_by"),
                rs.getString("attachment_path")
            };
            model.addRow(row);
        }

        rs.close();
        stmt.close();
        con.close();
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error fetching notices: " + e.getMessage());
    }

    JTable table = new JTable(model);

    // rest of your table styling, sorter, etc.
    sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);

    return table;
}

public boolean isCellEditable(int row, int column) {
    return false;
}



  // -------------------------
  // Stats Cards
  // -------------------------
  private void addStatsCards() {
    // Arrays for card labels and colors
    String[] metrics = { "Total Notices", "Pending", "Approved", "Total Users" };
    String[] values = new String[metrics.length];
    Color[] colors = {
        new Color(99, 102, 241), // Primary purple
        new Color(245, 158, 11), // Amber
        new Color(34, 197, 94),  // Green
        new Color(59, 130, 246), // Blue
    };

    try (Connection con = DriverManager.getConnection(
           "jdbc:mysql://localhost:3306/smartboard", "root", "@JD0890");
         Statement stmt = con.createStatement()) {

        // Total notices
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM notices");
        if (rs.next()) values[0] = String.valueOf(rs.getInt(1));
        rs.close();

        // Pending notices
        rs = stmt.executeQuery("SELECT COUNT(*) FROM notices WHERE status='Pending'");
        if (rs.next()) values[1] = String.valueOf(rs.getInt(1));
        rs.close();

        // Approved notices
        rs = stmt.executeQuery("SELECT COUNT(*) FROM notices WHERE status='Approved'");
        if (rs.next()) values[2] = String.valueOf(rs.getInt(1));
        rs.close();

        // Total users
        rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next()) values[3] = String.valueOf(rs.getInt(1));
        rs.close();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error fetching stats: " + e.getMessage());
        // fallback values
        values[0] = values[1] = values[2] = values[3] = "0";
    }

    // Remove old cards and add new ones
    cardsPanel.removeAll();
    for (int i = 0; i < metrics.length; i++) {
        cardsPanel.add(createStatCard(metrics[i], values[i], colors[i]));
    }
    cardsPanel.revalidate();
    cardsPanel.repaint();
}


  private JPanel createStatCard(String title, String value, Color color) {
    JPanel card = new RoundedPanel(12);
    card.setLayout(new BorderLayout(12, 0));
    card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    card.setBackground(CARD_BG);

    // Icon circle
    JPanel iconPanel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setColor(color);
        g2.fillOval(0, 0, 48, 48);
        g2.dispose();
      }
    };
    iconPanel.setOpaque(false);
    iconPanel.setPreferredSize(new Dimension(48, 48));

    // Text
    JPanel textPanel = new JPanel();
    textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
    textPanel.setOpaque(false);

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(customFont.deriveFont(Font.PLAIN, 13f));
    titleLabel.setForeground(TEXT_SECONDARY);

    JLabel valueLabel = new JLabel(value);
    valueLabel.setFont(customFont.deriveFont(Font.BOLD, 28f));
    valueLabel.setForeground(TEXT_PRIMARY);

    textPanel.add(titleLabel);
    textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    textPanel.add(valueLabel);

    card.add(iconPanel, BorderLayout.WEST);
    card.add(textPanel, BorderLayout.CENTER);
    return card;
  }

  // -------------------------
  // Sidebar Buttons
  // -------------------------
  private NavButton createSidebarButton(String text) {
    NavButton button = new NavButton(text);
    button.setFont(customFont.deriveFont(Font.PLAIN, 14f));
    button.setForeground(TEXT_SECONDARY);
    button.setFocusPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return button;
  }

  // -------------------------
  // Font
  // -------------------------
  private Font loadCustomFont() {
    try {
      Font poppins = Font.createFont(
        Font.TRUETYPE_FONT,
        Objects.requireNonNull(
          getClass().getResourceAsStream("/resource/Poppins-Regular.ttf")
        )
      ).deriveFont(16f);
      GraphicsEnvironment ge =
        GraphicsEnvironment.getLocalGraphicsEnvironment();
      ge.registerFont(poppins);
      return poppins;
    } catch (Exception e) {
      return new Font("Segoe UI", Font.PLAIN, 16);
    }
  }

  // -------------------------
  // Helpers & components
  // -------------------------
  private JPanel wrapInCard(JTable table, String title, JPanel filterBar) {
    RoundedPanel card = new RoundedPanel(12);
    card.setBackground(CARD_BG);
    card.setLayout(new BorderLayout());
    card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Header with title and filters
    JPanel head = new JPanel(new BorderLayout());
    head.setOpaque(false);
    head.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

    JLabel heading = new JLabel(title);
    heading.setFont(customFont.deriveFont(Font.BOLD, 18f));
    heading.setForeground(TEXT_PRIMARY);
    head.add(heading, BorderLayout.WEST);

    if (filterBar != null) {
      head.add(filterBar, BorderLayout.EAST);
    }

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(CARD_BG);

    card.add(head, BorderLayout.NORTH);
    card.add(scroll, BorderLayout.CENTER);
    return card;
  }

  private void applyFilters() {
    if (sorter == null) return;
    java.util.List<RowFilter<DefaultTableModel, Integer>> filters =
      new java.util.ArrayList<>();
    String text = searchField.getText();
    if (text != null && !text.isBlank()) {
      filters.add(
        RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text))
      );
    }
    if (!"All".equalsIgnoreCase(activeStatus)) {
      filters.add(
        RowFilter.regexFilter(
          "^" + java.util.regex.Pattern.quote(activeStatus) + "$",
          2
        )
      );
    }
    if (filters.isEmpty()) {
      sorter.setRowFilter(null);
    } else {
      sorter.setRowFilter(RowFilter.andFilter(filters));
    }
  }

  private static class RoundedPanel extends JPanel {

    private final int radius;

    public RoundedPanel(int radius) {
      this.radius = radius;
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
      );
      // subtle shadow
      g2.setColor(new Color(0, 0, 0, 25));
      g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 4, radius, radius);
      g2.setColor(getBackground());
      g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, radius, radius);
      g2.dispose();
      super.paintComponent(g);
    }

    @Override
    public boolean isOpaque() {
      return false;
    }
  }

  private void selectSidebar(NavButton selected, NavButton... others) {
    if (selected != null) selected.setSelected(true);
    if (others != null) for (NavButton b : others) if (b != null) b.setSelected(
      false
    );
    repaint();
  }

  // RoundedBorder removed; NavButton uses custom painting instead.

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
      g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
      );
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

  private class ModernTextField extends JTextField {

    private final String placeholder;

    public ModernTextField(String placeholder) {
      this.placeholder = placeholder;
      setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
      setBackground(CARD_BG);
      setOpaque(true);
      setFont(customFont.deriveFont(14f));
      setForeground(TEXT_PRIMARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
      );
      g2.setColor(getBackground());
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
      // outline
      g2.setColor(hasFocus() ? PRIMARY : BORDER);
      g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
      g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
      super.paintComponent(g);
      if (getText().isEmpty() && !isFocusOwner()) {
        g2.setColor(TEXT_SECONDARY);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(
          placeholder,
          14,
          (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1
        );
      }
      g2.dispose();
    }

    @Override
    public Insets getInsets() {
      return new Insets(10, 14, 10, 14);
    }

    @Override
    public boolean isOpaque() {
      return false;
    }
  }

  private static void consume(Object... ignored) {
    /* intentionally empty */
  }

  private class StatusPillRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
      JTable tbl,
      Object value,
      boolean isSelected,
      boolean hasFocus,
      int row,
      int column
    ) {
      String status = String.valueOf(value);
      Color bg = new Color(241, 245, 249);
      Color fg = TEXT_SECONDARY;

      if ("Approved".equalsIgnoreCase(status)) {
        bg = new Color(220, 252, 231);
        fg = new Color(22, 163, 74);
      } else if ("Pending".equalsIgnoreCase(status)) {
        bg = new Color(254, 243, 199);
        fg = new Color(202, 138, 4);
      }

      JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
      wrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
      if (!isSelected) {
        wrapper.setBackground(
          row % 2 == 0 ? CARD_BG : new Color(249, 250, 251)
        );
        wrapper.setOpaque(true);
      }

      PillLabel pill = new PillLabel(status, bg, fg);
      wrapper.add(pill);
      return wrapper;
    }
  }

  private class PillToggle extends JToggleButton {

    public PillToggle(String text) {
      super(text);
      setFocusPainted(false);
      setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
      setContentAreaFilled(false);
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      setFont(customFont.deriveFont(Font.BOLD, 13f));
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
      );
      Color bg = isSelected() ? PRIMARY : Color.WHITE;
      Color fg = isSelected() ? Color.WHITE : TEXT_SECONDARY;

      if (!isSelected() && getModel().isRollover()) {
        bg = PRIMARY_LIGHT;
      }

      g2.setColor(bg);
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

      if (!isSelected()) {
        g2.setColor(BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
      }

      g2.dispose();
      setForeground(fg);
      super.paintComponent(g);
    }

    @Override
    public boolean isOpaque() {
      return false;
    }
  }
  
  private void refreshTable() {
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    model.setRowCount(0); // clear existing data

    try (Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/smartboard", "root", "@JD0890");
         Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(
             "SELECT id, title, description, priority, status, date, published_by, attachment_path FROM notices ORDER BY date DESC")) {

        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("priority"),
                rs.getString("status"),
                rs.getDate("date").toString(),
                rs.getString("published_by"),
                rs.getString("attachment_path")
            };
            model.addRow(row);
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error fetching notices: " + e.getMessage());
    }

    applyFilters(); // keep filters active
}
  
  

private void refreshStats() {
    addStatsCards(); // reuse existing method to reload stats
}



  // Modern sidebar button with hover and selected states
  private class NavButton extends JButton {

    public NavButton(String text) {
      super(text);
      setContentAreaFilled(false);
      setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
      setHorizontalAlignment(SwingConstants.LEFT);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON
      );
      boolean hover = getModel().isRollover();
      boolean selected = isSelected();

      if (selected) {
        g2.setColor(PRIMARY_LIGHT);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        setForeground(PRIMARY);
      } else if (hover) {
        g2.setColor(new Color(248, 250, 252));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        setForeground(TEXT_PRIMARY);
      } else {
        setForeground(TEXT_SECONDARY);
      }

      // Left accent for selected
      if (selected) {
        g2.setColor(PRIMARY);
        g2.fillRoundRect(0, getHeight() / 4, 3, getHeight() / 2, 3, 3);
      }

      g2.dispose();
      super.paintComponent(g);
    }

    @Override
    public boolean isOpaque() {
      return false;
    }
    
    
  }
}