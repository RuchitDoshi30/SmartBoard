// NoticeDAO.java
package com.smartboard.dao;

import com.smartboard.entity.Notice;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import com.smartboard.util.HibernateUtil;

public class NoticeDAO {

    public List<Notice> getAllNotices() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Notice", Notice.class).list();
        }
    }

    public boolean deleteNotice(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Notice notice = session.get(Notice.class, id);
            if (notice != null) {
                session.delete(notice);
                tx.commit();
                return true;
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
        return false;
    }
    
    public Notice getNoticeById(int id) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        return session.get(Notice.class, id);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

public void updateNotice(Notice notice) {
    Transaction tx = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        tx = session.beginTransaction();
        session.update(notice);
        tx.commit();
    } catch (Exception e) {
        if (tx != null) tx.rollback();
        e.printStackTrace();
    }
}

}
