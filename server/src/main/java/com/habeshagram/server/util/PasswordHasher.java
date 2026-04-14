package com.habeshagram.server.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    private static final int WORKLOAD = 12;
    
    public static String hash(String password) {
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(password, salt);
    }
    
    public static boolean verify(String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("$2a$")) {
            throw new IllegalArgumentException("Invalid hash format");
        }
        return BCrypt.checkpw(password, storedHash);
    }
}