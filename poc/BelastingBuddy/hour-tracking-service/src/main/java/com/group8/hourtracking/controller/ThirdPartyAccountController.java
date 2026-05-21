package com.group8.hourtracking.controller;

import com.group8.hourtracking.model.ThirdPartyAccount;
import com.group8.hourtracking.repository.ThirdPartyAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * controller for managing third-party account connections (api keys).
 */
@RestController
@RequestMapping("/third-party-accounts")
public class ThirdPartyAccountController {
    /** variable for third party account repo. */
    private final ThirdPartyAccountRepository accountRepository;
    /**
     * init thirdparty account controller.
     * @param accountRepository
     */
    public ThirdPartyAccountController(ThirdPartyAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Get all third-party accounts for a user.
     * @param userId
     * @return return successfully saved account.
     */
    @GetMapping
    public ResponseEntity<List<ThirdPartyAccount>> getUserAccounts(@RequestParam UUID userId) {
        List<ThirdPartyAccount> accounts = accountRepository.findByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Add a new third-party account connection.
     * 
     * POST /third-party-accounts
     * Body: { "userId": "uuid", "provider": "clickup", "authToken": "pk_xxx:teamId"
     * }
     * @param account 
     * @return succesfully saved account.
     */
    @PostMapping
    public ResponseEntity<ThirdPartyAccount> addAccount(@RequestBody ThirdPartyAccount account) {
        account.setId(UUID.randomUUID());
        account.setConnectedDate(LocalDateTime.now());
        account.setActive(true);
        ThirdPartyAccount saved = accountRepository.save(account);
        return ResponseEntity.ok(saved);
    }

    /**
     * Update an existing third-party account (for example update API key).
     * @param accountId
     * @param account
     * @return response.
     */
    @PutMapping("/{accountId}")
    public ResponseEntity<ThirdPartyAccount> updateAccount(
        @PathVariable UUID accountId,
        @RequestBody ThirdPartyAccount account) {
        return accountRepository.findById(accountId)
            .map(existing -> {
                if (account.getAuthToken() != null) {
                    existing.setAuthToken(account.getAuthToken());
                }
                if (account.getProvider() != null) {
                    existing.setProvider(account.getProvider());
                }
                existing.setActive(account.isActive());
                return ResponseEntity.ok(accountRepository.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a third-party account connection.
     * @param accountId
     * @return send no acc found.
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID accountId) {
        accountRepository.deleteById(accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get a specific account by user and provider.
     * @param userId
     * @param provider
     * @return send response.
     */
    @GetMapping("/by-provider")
    public ResponseEntity<ThirdPartyAccount> getAccountByProvider(
        @RequestParam UUID userId,
        @RequestParam String provider) {
        return accountRepository.findByUserIdAndProvider(userId, provider)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
