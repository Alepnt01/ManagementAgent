package com.managementagent.server.model;

/**
 * Response returned upon successful login.
 */
public record LoginResponse(String username, String token) {
}
