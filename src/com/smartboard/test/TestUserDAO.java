package com.smartboard.test;

import com.smartboard.dao.UserDAO;
import com.smartboard.entity.User;

public class TestUserDAO {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        // CREATE
        User user = new User();
        user.setUsername("alice");
        user.setPassword("password123");
        user.setRole("user");
        userDAO.saveUser(user);

        // READ
        User retrieved = userDAO.getUser(user.getId());
        System.out.println("Retrieved: " + retrieved);

        // UPDATE
        retrieved.setRole("admin");
        userDAO.updateUser(retrieved);

        // DELETE
        // userDAO.deleteUser(retrieved);
    }
}
