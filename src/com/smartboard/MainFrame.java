package com.smartboard;

import javax.swing.*;
import java.awt.*;
import com.smartboard.entity.User; // Make sure this import exists

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel container;

    // -----------------------
    // Session variables
    // -----------------------
    private String loggedInUsername;
    private User loggedInUser;

    public MainFrame() {
        setTitle("SmartBoard - Digital Notice Board");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Optional: set a fixed size or use pack() at end
        setPreferredSize(new Dimension(1000, 600));

        // CardLayout container
        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        // Add all pages (JPanel subclasses)
        container.add(new AdminLoginPage(this), "AdminLoginPage");
        container.add(new AdminDashboard(this), "AdminDashboard");
        addNoticePage = new AddNoticePage(this);
        container.add(addNoticePage, "AddNotice");

        container.add(new ManageNoticesPage(this, addNoticePage), "ManageNotices");
        container.add(new ViewAllNoticesPage(this), "ViewNotices");

        container.add(new UserHomePage(this), "UserHome");
        container.add(new UserNoticeViewer(this), "UserNoticeViewer");
        container.add(new UserNoticeDetail(this), "UserNoticeDetail");

        // Put container in center
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(container, BorderLayout.CENTER);

        // Show initial page
        showPage("UserHome");

        pack();               // sizes JFrame to fit preferred sizes
        setLocationRelativeTo(null); // center on screen
        setVisible(true);
    }

    AddNoticePage addNoticePage;

    // -----------------------
    // Navigation
    // -----------------------
    public void showPage(String name) {
        cardLayout.show(container, name);
    }

    // -----------------------
    // Session getters/setters
    // -----------------------
    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }


    // -----------------------
    // Main
    // -----------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // ignore if setting L&F fails
            }
            new MainFrame();
        });
    }
    
    // -----------------------
    // Navigation
    // -----------------------
    // ✅ ADD THIS METHOD
// ✅ CORRECTED METHOD
    public void showNoticeDetails(int noticeId) {
        // Find the UserNoticeDetail panel within the container
        UserNoticeDetail detailPanel = null; // <-- Changed variable name and type
        for (Component comp : container.getComponents()) {
            if (comp instanceof UserNoticeDetail) { // <-- Check for UserNoticeDetail
                detailPanel = (UserNoticeDetail) comp;
                break;
            }
        }

        if (detailPanel != null) {
            // Tell the detail panel which notice to load
            detailPanel.loadNoticeDetails(noticeId); // <-- Assume UserNoticeDetail has this method

            // Switch to the detail panel
            cardLayout.show(container, "UserNoticeDetail"); // <-- Show the correct page name
        } else {
            // Handle error - UserNoticeDetail panel not found
            System.err.println("Error: UserNoticeDetail panel not found in container.");
            JOptionPane.showMessageDialog(this,
                "Error loading notice details page.", // Updated error message
                "Navigation Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    // -----------------------
}
