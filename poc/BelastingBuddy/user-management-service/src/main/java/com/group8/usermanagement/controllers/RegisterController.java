package com.group8.usermanagement.controllers;

import com.group8.usermanagement.models.RegisterRequest;
import com.group8.usermanagement.models.RegisterResponse;
import com.group8.usermanagement.services.DownstreamService;
import com.group8.usermanagement.services.KeycloakService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RegisterController {
    /**
     * Service for interacting with Keycloak.
     */
    private final KeycloakService kc;
    /**
     * Service for handling downstream service calls.
     */
    private final DownstreamService downstreamService;

    public RegisterController(KeycloakService kc, DownstreamService downstreamService) {
        this.kc = kc;
        this.downstreamService = downstreamService;
    }


    @PostMapping("public/register")
    public RegisterResponse register(@RequestBody @Valid RegisterRequest req) {
        String id = kc.createUser(
                req.username(),
                req.email(),
                req.password(),
                req.firstName(),
                req.lastName()
        );
        return new RegisterResponse(id, "CREATED");
    }

    @PostMapping("public/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_request",
                    "error_description", "Missing username or password"
            ));
        }
        return ResponseEntity.ok(kc.loginUser(username, password));
    }

    @PostMapping("/stubService")
    public ResponseEntity<String> callDownstream(@AuthenticationPrincipal Jwt jwt) {
        String result = downstreamService.callDocProcessing(jwt);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/stubServiceNO")
    public ResponseEntity<String> callDownstreamNO(@AuthenticationPrincipal Jwt jwt) {
        String result = downstreamService.callDocProcessing(jwt);
        return ResponseEntity.ok(result);
    }
}
