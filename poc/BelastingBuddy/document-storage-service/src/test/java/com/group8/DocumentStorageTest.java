package com.group8;

import com.group8.documentstorage.*;

import io.github.cdimascio.dotenv.Dotenv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.google.cloud.kms.v1.KeyManagementServiceClient;

import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;



@SpringBootTest(classes = DocumentStorageServiceApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
public class DocumentStorageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeyManagementServiceClient kmsClient;

    @MockBean
    private FirebaseStorageService firebaseStorageService;


    private final String filesDir = "files-upload-test/";

    private RequestPostProcessor mockJwt() {
        return jwt().jwt(jwt -> jwt
                .subject("test-user")
                .claim("iss", "http://localhost/realms/users"));
    }

    /**
     * create a directory for local file upload if it doesn't exist.
     */
    @BeforeEach
    void cleanUp() throws Exception {
        File directory = new File(filesDir);
        if (!directory.exists()) directory.mkdirs();
        for (File file : directory.listFiles()) {
            file.delete();
        }
    }
    /**
     * Test local upload.
     */
    @Test
    void shouldReturnHelloWorld() throws Exception {
        System.out.println("TEST RUNNING");
        mockMvc.perform(get("/api/files-archive/hello")
                        .with(mockJwt())
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    /**
     * check if local upload works correctly.
     */
    @Test
    void shouldUploadFileAndCreateMetadata() throws Exception {
        // Create a mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/files-archive/upload-local")
                                .file(file)
                                .param("content", "Sample content")
                                .param("userID", "user123")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(mockJwt())
                )
                .andExpect(status().isOk());

        // Verify the file was saved and metadata created correctly.
        Path uploadedFile = Path.of(filesDir, "test.txt");
        assertThat(Files.exists(uploadedFile)).isTrue();
        Path metadataFile = Path.of(filesDir, "metadata_test_txt.json");
        assertThat(Files.exists(metadataFile)).isTrue();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metadata = mapper.readTree(Files.readString(metadataFile));
        assertThat(metadata.get("filename").asText()).isEqualTo("test.txt");
        assertThat(metadata.get("userID").asText()).isEqualTo("user123");
        assertThat(metadata.get("content").asText()).isEqualTo("Sample content");
        assertThat(metadata.has("uploadTime")).isTrue();
    }

    @Test
    void shouldUploadFileSuccessfully() throws Exception {
        // Mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        // Mock firebase service to return a URL
        Mockito.when(firebaseStorageService.uploadFile(
                Mockito.any(InputStream.class),
                Mockito.anyString(),
                Mockito.anyString()
        )).thenReturn("https://fakefirebase.com/test.txt");
        // Mock saving metadata to firebase.
        Mockito.when(firebaseStorageService.saveFileMetadata(Mockito.any())).thenReturn(0);
        
        EncryptedContent enc = new EncryptedContent( "ciphertext", "wrappedDek", "iv" );
        
        try (
                MockedStatic<KeyManagementServiceClient> mockedKmsClientFactory = Mockito.mockStatic(KeyManagementServiceClient.class);
                MockedStatic<CryptoHelper> mockedCryptoHelper = Mockito.mockStatic(CryptoHelper.class)
        ) {
                KeyManagementServiceClient mockKmsClient = Mockito.mock(KeyManagementServiceClient.class);
                
                mockedKmsClientFactory.when(KeyManagementServiceClient::create).thenReturn(mockKmsClient);
                mockedCryptoHelper.when(() -> CryptoHelper.encryptContent(Mockito.anyString(), Mockito.any(), Mockito.any()))
                                .thenReturn(enc);

                mockMvc.perform(multipart("/api/files-archive/upload")
                        .file(file)
                        .param("content", "Sample content")
                        .param("userID", "user123")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(mockJwt()))
                        .andExpect(status().isOk())
                        .andExpect(content().string("File sent successfully"));

        }
    }

    @Test
    void shouldReturnOkWhenFileDeleted() throws Exception {
        String userID = "user123";
        String fileName = "test.txt";

        // Mock deleteFile to return 0 → success
        Mockito.when(firebaseStorageService.deleteFile(fileName, userID)).thenReturn(0);

        mockMvc.perform(multipart("/api/files-archive/delete")
                        .param("userID", userID)
                        .param("fileName", fileName)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(mockJwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("File deleted."));
    }

    @Test
    void shouldReturnErrorWhenFileDeleteFailed() throws Exception {
        String userID = null;
        String fileName = "test.txt";

        // Mock failed delete returns error 400 as userID is null.
        Mockito.when(firebaseStorageService.deleteFile(fileName, userID)).thenReturn(0);

        mockMvc.perform(multipart("/api/files-archive/delete")
                        .param("userID", userID)
                        .param("fileName", fileName)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(mockJwt()))
                .andExpect(status().is(400));
    }


}
