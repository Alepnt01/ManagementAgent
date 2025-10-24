package com.managementagent.client.model;

/**
 * Response returned by the server upon successful login.
 */
public record LoginResponsePayload(String username, String token) {
}
