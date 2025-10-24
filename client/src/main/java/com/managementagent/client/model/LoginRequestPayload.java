package com.managementagent.client.model;

/**
 * Payload sent to authenticate the desktop user against the REST API.
 */
public record LoginRequestPayload(String username, String password) {
}
