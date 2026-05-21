package com.group8.docprocessing.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.group8.docprocessing.service.DocumentProcessingService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import com.group8.docprocessing.service.DownstreamService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DocProcessingController.class)
@AutoConfigureMockMvc(addFilters = true) //needs to pass security filters now
public class DocProcessingControllerTest {

    /**
     * MockMvc is used to perform HTTP requests in the test environment.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mocked instance of DocumentProcessingService to simulate service behavior in tests.
     */
    @MockBean
    private DocumentProcessingService documentProcessingService;

    @MockBean
    private DownstreamService downstreamService;

    @Test
    public void testUploadDocumentSuccessImage() throws Exception {
        String expectedDocumentId = "PROC-test-123";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "dummy image content".getBytes()
        );
        
        when(documentProcessingService.processUpload(any(), any(), any()))
                .thenReturn(expectedDocumentId);

        mockMvc.perform(
                        multipart("/documents/upload")
                                .file(file)
                                .with(jwt().jwt(builder -> builder.subject("test-user")))
                )
                .andExpect(status().isAccepted());
    }
    
    @Test
    public void testUploadDocumentSuccessPdf() throws Exception {
        String expectedDocumentId = "PROC-test-123";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.pdf",
                "image/pdf",
                "dummy image content".getBytes()
        );
        
        when(documentProcessingService.processUpload(any(), any(), any()))
                .thenReturn(expectedDocumentId);

        mockMvc.perform(
                        multipart("/documents/upload")
                                .file(file)
                                .with(jwt().jwt(builder -> builder.subject("test-user")))
                )
                .andExpect(status().isAccepted());
    }
    
    @Test
    public void testUploadDocumentNoFile() throws Exception {
        mockMvc.perform(
                        multipart("/documents/upload")
                                .with(jwt().jwt(builder -> builder.subject("test-user")))
                )
                .andExpect(status().isBadRequest());
    }
}