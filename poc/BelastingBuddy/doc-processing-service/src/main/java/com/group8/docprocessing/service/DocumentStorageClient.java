package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class DocumentStorageClient {

    /**
     * RestTemplate for making HTTP requests to the doc-storage service.
     */
    private final RestTemplate restTemplate;

    /**
     * The base URL of the doc-storage service, injected from application properties.
     */
    @Value("${doc-storage.service.url}")
    private String docStorageUrl;

    /**
     * Constructor for DocumentStorageClient.
     * @param restTemplate The RestTemplate used for making HTTP requests.
     */
    public DocumentStorageClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends the extracted data and the original binary file to the doc-storage service.
     * @param eventData The extracted document data.
     * @param fileData The actual binary content of the original file.
     * @param originalFilename The original name of the file.
     * @param endpoint The doc-storage endpoint to call (e.g., "/upload" or "/upload-local").
     * @return The response from the doc-storage service.
     */
    public ResponseEntity<String> sendExtractedData(
            ExtractedData eventData, 
            byte[] fileData, 
            String originalFilename,
            String endpoint) {
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", new ByteArrayResourceWithFilename(fileData, originalFilename));
        body.add("content", eventData.getExtractedContent());
        body.add("userID", "system-processor"); 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String url = docStorageUrl + endpoint;
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

    }
    
    /**
     * Custom ByteArrayResource that includes a filename.
     */
    static class ByteArrayResourceWithFilename extends org.springframework.core.io.ByteArrayResource {
        
        /**
         * The filename to be used in the multipart request.
         */
        private final String filename;

        public ByteArrayResourceWithFilename(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}