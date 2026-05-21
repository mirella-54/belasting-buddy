package com.group8.docprocessing.controller;

import com.group8.docprocessing.service.DownstreamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import com.group8.docprocessing.service.DocumentProcessingService;

@RestController
@RequestMapping("/documents")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DocProcessingController {

    /**
     * Service to handle document processing logic.
     */
    private final DocumentProcessingService documentProcessingService;
    /**
     * Service to handle downstream service calls.
     */
    private final DownstreamService downstreamService;


    public DocProcessingController(DocumentProcessingService documentProcessingService,
                                   DownstreamService downstreamService) {
        this.documentProcessingService = documentProcessingService;
        this.downstreamService = downstreamService;
    }

    /**
     * Endpoint to upload a document file.
     * Initiates the asynchronous document extraction pipeline by publishing to Kafka.
     *
     * @param file the uploaded file
     * @param metadata optional metadata string
     * @param jwt the authenticated user's JWT token
     * @return 202 Accepted status with the unique processing ID.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "metadata", required = false) String metadata,
        @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject(); //throws 401 by autowired

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
            !(contentType.startsWith("image/") || contentType.equals("application/pdf"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type");
        }

        String finalMetadata = (metadata != null) ? metadata : "";

        String processingId = documentProcessingService.processUpload(file, finalMetadata, userId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body("Document accepted for processing. ID: " + processingId);
    }

    @PostMapping("/stubUser")
    public ResponseEntity<String> stubEndpoint(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok("Stub endpoint is working." + " User ID: " + userId);
    }

    @PostMapping("/internal/stub")
    @PreAuthorize("hasAuthority('SVC_user-management')")
    public ResponseEntity<String> internalStub(@AuthenticationPrincipal Jwt jwt) {
        String caller = jwt.getClaimAsString("azp");
        System.out.println("Internal stub called by service: " + caller);
        return ResponseEntity.ok("Authenticated as service: " + caller + "   " + jwt.getSubject());
    }

    @PostMapping("/internal/stubNO")
    @PreAuthorize("hasAuthority('SVC_no-such-service')")
    public ResponseEntity<String> internalStubNO(@AuthenticationPrincipal Jwt jwt) {
        String caller = jwt.getClaimAsString("azp");
        System.out.println("Internal stub called by service: " + caller);
        return ResponseEntity.ok("Authenticated as service: " + caller + "   " + jwt.getSubject());
    }

    @PostMapping("/internal/hourcheck")
    @PreAuthorize("hasAuthority('SVC_hour-tracking')")
    public ResponseEntity<String> internalStubHours(@AuthenticationPrincipal Jwt jwt) {
        String caller = jwt.getClaimAsString("azp");
        System.out.println("Internal stub called by service: " + caller);
        return ResponseEntity.ok("Authenticated as service: " + caller + "   " + jwt.getSubject());
    }

    @PostMapping("/hourresponse")
    public ResponseEntity<String> hourResponse(@AuthenticationPrincipal Jwt jwt) {
        String response = downstreamService.callHourService(jwt);
        return ResponseEntity.ok(response);
    }

}