package com.group8.usermanagement.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * Represents a user in the user management system.
 */
@Getter
@Setter
@Document(collection = "users")
public class User {
    /**
     * The unique identifier for the user.
     */
    @Id
    private UUID id;
    /**
     * The username of the user.
     */
    private String username;
    /**
     * The email address of the user.
     */
    private String email;
    /**
     * The Keycloak ID associated with the user.
     */
    private String keycloakId;
    /**
     * The full name of the user.
     */
    private String fullName;
    /**
     * The locale preference of the user.
     */
    private String locale;
}
