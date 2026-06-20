package com.uade.arquitectura.auth.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    // Simple in-memory credentials for demo
    private static final String[] VALID_USERS = {"user1", "user2", "admin"};
    private static final String[] VALID_PASSWORDS = {"pass1", "pass2", "admin123"};

    public boolean authenticate(String username, String password) {
        for (int i = 0; i < VALID_USERS.length; i++) {
            if (VALID_USERS[i].equals(username) && VALID_PASSWORDS[i].equals(password)) {
                return true;
            }
        }
        return false;
    }
}
