package com.smartboard.dao;

import com.smartboard.entity.Role;
import com.smartboard.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class RoleDAO {

    public void saveRole(Role role) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(role);
            transaction.commit();
            System.out.println("Role saved: " + role.getName());
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // Optional: fetch by name
    public Role getRoleByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Role r WHERE r.name = :name", Role.class)
                          .setParameter("name", name)
                          .uniqueResult();
        }
    }
}
