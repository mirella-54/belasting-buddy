package com.group8.usermanagement.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName
) {}
