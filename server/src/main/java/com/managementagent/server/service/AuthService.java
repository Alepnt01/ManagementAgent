package com.managementagent.server.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple authentication service backed by a static credential map and in-memory tokens.
 */
public class AuthService {

    private final Map<String, String> credentials;
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public Optional<String> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        String expected = credentials.get(username);
        if (expected != null && expected.equals(password)) {
            String token = generateToken();
            activeTokens.put(token, username);
            return Optional.of(token);
        }
        return Optional.empty();
    }

    public boolean isTokenValid(String token) {
        return token != null && activeTokens.containsKey(token);
    }

    public boolean isAuthenticationEnabled() {
        return !credentials.isEmpty();
    }

    public void invalidate(String token) {
        if (token != null) {
            activeTokens.remove(token);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
