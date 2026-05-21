package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentStorageClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DocumentStorageClient client;

    // Use ArgumentCaptor to capture the complex arguments passed to restTemplate.exchange()
    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, Object>>> requestEntityCaptor;

    private final String mockBaseUrl = "http://doc-storage:8080/api/files-archive";
    private final String testEndpoint = "/upload-local";
    private final String testFullUrl = mockBaseUrl + testEndpoint;
    
    private ExtractedData testEventData;
    private final byte[] testFileData = "file content bytes".getBytes();
    private final String testFilename = "invoice-123.pdf";

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to inject the @Value field
        ReflectionTestUtils.setField(client, "docStorageUrl", mockBaseUrl);

        // Setup mock ExtractedData
        testEventData = mock(ExtractedData.class);
        when(testEventData.getExtractedContent()).thenReturn("Extracted text: ABC-123");
    }

    // Custom ArgumentMatcher to verify the contents of the ByteArrayResourceWithFilename
    private static class ResourceMatcher implements ArgumentMatcher<Resource> {
        private final byte[] expectedBytes;
        private final String expectedFilename;

        public ResourceMatcher(byte[] expectedBytes, String expectedFilename) {
            this.expectedBytes = expectedBytes;
            this.expectedFilename = expectedFilename;
        }

        @Override
        public boolean matches(Resource resource) {
            if (!(resource instanceof DocumentStorageClient.ByteArrayResourceWithFilename)) {
                return false;
            }
            // Check filename and content length
            try {
                return expectedFilename.equals(resource.getFilename()) && 
                       expectedBytes.length == resource.contentLength();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
    }

    @Test
    void sendExtractedData_Success_ReturnsOkResponse() {
        // Arrange
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Archive success", HttpStatus.OK);

        // Mock RestTemplate to return a successful response
        when(restTemplate.exchange(
                eq(testFullUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> actualResponse = client.sendExtractedData(
                testEventData, 
                testFileData, 
                testFilename, 
                testEndpoint
        );

        // Assert
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals("Archive success", actualResponse.getBody());
    }
    
    @Test
    void sendExtractedData_VerifiesRequestStructure() {
        // Arrange
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Accepted", HttpStatus.ACCEPTED);
        
        // Mock RestTemplate to capture the request entity
        when(restTemplate.exchange(
                eq(testFullUrl),
                eq(HttpMethod.POST),
                requestEntityCaptor.capture(), // Capture the HttpEntity argument
                eq(String.class)
        )).thenReturn(expectedResponse);

        // Act
        client.sendExtractedData(testEventData, testFileData, testFilename, testEndpoint);

        // Assert

        // 1. Verify URL and Method
        verify(restTemplate, times(1)).exchange(
                eq(testFullUrl), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(String.class)
        );

        // 2. Capture and check the RequestEntity
        HttpEntity<MultiValueMap<String, Object>> capturedRequest = requestEntityCaptor.getValue();
        assertNotNull(capturedRequest);
        
        // 3. Check Headers
        HttpHeaders headers = capturedRequest.getHeaders();
        assertEquals(MediaType.MULTIPART_FORM_DATA, headers.getContentType(), "Header must be MULTIPART_FORM_DATA");

        // 4. Check Body (MultiValueMap)
        MultiValueMap<String, Object> body = capturedRequest.getBody();
        assertNotNull(body, "Request body must not be null");
        
        // a) Check 'content' part
        assertTrue(body.containsKey("content"));
        assertEquals(testEventData.getExtractedContent(), body.getFirst("content"), "Extracted content mismatch");
        
        // b) Check 'userID' part
        assertTrue(body.containsKey("userID"));
        assertEquals("system-processor", body.getFirst("userID"), "UserID mismatch");
        
        // c) Check 'file' part using the custom matcher
        assertTrue(body.containsKey("file"));
        Object filePart = body.getFirst("file");
        assertTrue(filePart instanceof Resource, "File part must be a Resource");
        assertTrue(new ResourceMatcher(testFileData, testFilename).matches((Resource) filePart), 
                   "File part resource name or content is incorrect");
    }
    
    @Test
    void sendExtractedData_UsesDifferentEndpoint() {
        // Arrange
        String productionEndpoint = "/upload";
        String expectedProdUrl = mockBaseUrl + productionEndpoint;
        
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("Uploaded", HttpStatus.CREATED);
        when(restTemplate.exchange(
                eq(expectedProdUrl), // Expect the production URL
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(expectedResponse);

        // Act
        ResponseEntity<String> actualResponse = client.sendExtractedData(
                testEventData, 
                testFileData, 
                testFilename, 
                productionEndpoint
        );

        // Assert
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        
        // Verify the RestTemplate was called with the production endpoint URL
        verify(restTemplate, times(1)).exchange(
                eq(expectedProdUrl), 
                eq(HttpMethod.POST), 
                any(HttpEntity.class), 
                eq(String.class)
        );
    }
}