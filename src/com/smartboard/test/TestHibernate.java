package com.smartboard.test;

import com.smartboard.entity.User;
import com.smartboard.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestHibernate {
    public static void main(String[] args) {
        // Get session from HibernateUtil
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            // Create new User
            User user = new User("john_doe", "password123", "admin");

            // Save User
            session.save(user);

            tx.commit();
            System.out.println("User saved: " + user);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
