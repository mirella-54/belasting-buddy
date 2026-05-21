package com.group8.documentstorage;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.net.URL;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import io.github.cdimascio.dotenv.Dotenv;




@RestController
@RequestMapping("/api/files-archive")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FileUploadController {
    /** declare firebaseStorage service. */
    @Autowired
    private FirebaseStorageService firebaseStorageService;
    /**
     * return 200 if there was no error.
     */
    private static final int OK = 200;
    /**
     * return error code 500 in case something went wrong.
     */
    private static final int ERROR = 500;
    /** If file is not found it has a specific status code. */
    private static final int FILE_NOT_FOUND = 404;
    /** Variable which loads the .env variables in. */
    static Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    /** Name of the project in Firebase. */
    private static final String projectName = dotenv.get("PROJECT_NAME");  
    /** Area where firebase db is. */                
    private static final String projectArea = dotenv.get("PROJECT_AREA");
    /** Name of the KMS generated in Google Key management System. */
    private static final String projectKMS = dotenv.get("PROJECT_KMS");
    /** Key of the project generated in Google Key management System. */
    private static final String projectKey = dotenv.get("PROJECT_KEY");

    /**
     * check if this file was compiled by Springboot
     * and notify that the microservice is active.
     */
    @PostConstruct
    public void init() {
        System.out.println("FileUploadController initialized!");
    }

    /**
     * Test api with a hello msg.
     * @return sends back hello world to the requester.
     */
    @GetMapping("/hello")
    public String hello() {
        System.out.println("Hello endpoint called");
        return "Hello World";
    }

    /**
     * function to handle /upload request.
     * @param file file to be uploaded.
     * @param content content of the file.
     * @param userID id of the user.
     * @return return a msg, 200 if it's ok or 500 if it went wrong.
     */
    @PostMapping(value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String>
        uploadFile(@RequestParam("file") final MultipartFile file, 
        @RequestParam("content") final String content, 
        @RequestParam("userID") final String userID) {
        try {
            System.out.println("Upload endpoint called!");
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            //todo: optinally encrypt the binary.
            String fileUrl = firebaseStorageService
                    .uploadFile(inputStream, fileName, contentType);
            if (fileUrl == null) {
                return ResponseEntity.status(ERROR).body("Upload failed");
            }

            // Parse content to extract amount and VAT (before encryption)
            String amount = null;
            String vat = null;
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode contentJson = mapper.readTree(content);
                if (contentJson.has("amount")) {
                    amount = contentJson.get("amount").asText();
                }
                if (contentJson.has("vat")) {
                    vat = contentJson.get("vat").asText();
                }
            } catch (Exception e) {
                System.err.println("Failed to parse content for amount/VAT: " + e.getMessage());
            }

            // Use try-with-resources to properly close the KMS client
            try (KeyManagementServiceClient kmsClient = KeyManagementServiceClient.create()) {
                CryptoKeyName kmsKeyName = CryptoKeyName.of(
                    projectName,
                    projectArea,
                    projectKMS,
                    projectKey
                );
                EncryptedContent enc = CryptoHelper.encryptContent(content, kmsClient, kmsKeyName);
                
                FileMetadata metadata = new FileMetadata(
                        fileName,
                        contentType,
                        fileUrl,
                        new Date(), 
                        enc.ciphertext, 
                        userID,
                        enc.wrappedDek,
                        enc.iv
                        );
                
                // Set the plaintext amount and VAT for easy display
                metadata.setAmount(amount);
                metadata.setVat(vat);
                
                int ret = firebaseStorageService
                        .saveFileMetadata(metadata);
                
                if (ret != 0) {
                    return ResponseEntity.status(ERROR)
                            .body("Error uploading file");
                }
                return ResponseEntity.status(OK).body("File sent successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(ERROR).body("Error uploading file");
        }
    }
    /**
     * This function is implemented for testing purposes. It allows to save a file locally to 'files' directory.
     * @param file the file binaries.
     * @param content content of the file.
     * @param userID userID the file belongs to.
     * @return returns status code and message which depends if error occurred or not.
     */
    @PostMapping(value = "/upload-local",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String>
        uploadLocal(@RequestParam("file") final MultipartFile file, 
                @RequestParam("content") final String content, 
                @RequestParam("userID") final String userID) {
        try {

            String filename = file.getOriginalFilename();
            if(filename == null) {
                return ResponseEntity.status(ERROR).body("Failed to extract filename");
            }
            System.out.println("Checking Firebase for file: " + filename + " userID: " + userID);
            FileMetadata existingMetadata = firebaseStorageService.getFile(userID, filename);
            if (existingMetadata != null) {
                // File exists — reject upload
                return ResponseEntity.status(ERROR).body("File with this name already exists.");
            }

            String filesDir = "files-upload-test/";

            // Safe binaries
            File directory = new File(filesDir);
            if(!directory.exists()){
                directory.mkdirs();
            }
            Path filePath = Paths.get(filesDir + filename);
            Files.write(filePath, file.getBytes());

            // Create JSON metadata file.
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode metadata = mapper.createObjectNode();
            metadata.put("filename", filename);
            metadata.put("userID", userID);
            metadata.put("content", content);
            metadata.put("uploadTime", new Date().toString());

            // Save metadata.
            String noExtensionName = filename.replace(".", "_");
            Path metadataPath = Paths.get(filesDir + "metadata_" + noExtensionName+ ".json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(metadataPath.toFile(), metadata);
            return ResponseEntity.ok().body("Successfully saved testing file locally.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(ERROR).body("Failed to save test file in directory.");
        }
    }

    /**
     * get metadata of the file.
     * @param fileName name of the file.
     * @param userID user ID of the user whose file it is.
     * @return returns status code plus message based on the response from firebaseStorageService.
     */
    @PostMapping("/getFile")
    public ResponseEntity<?> getFile(
            @RequestParam("fileName") String fileName,
            @RequestParam("userID") String userID) {

        try {
            FileMetadata metadata = firebaseStorageService.getFile(userID, fileName);
            System.out.println(metadata);
            
            if (metadata != null) {
                // Use try-with-resources to properly close the KMS client
                try (KeyManagementServiceClient kmsClient = KeyManagementServiceClient.create()) {
                    CryptoKeyName kmsKeyName = CryptoKeyName.of(
                        projectName,
                        projectArea,
                        projectKMS,
                        projectKey
                    );
                    EncryptedContent enc = new EncryptedContent(metadata.getFileContent(), 
                        metadata.getVI(), metadata.getWrappedDek());
                    String content = CryptoHelper.decryptContent(enc, kmsClient, kmsKeyName);
                    return ResponseEntity.status(OK).body(content);
                }
            } else {
                return ResponseEntity.status(FILE_NOT_FOUND)
                        .body("File not found for user " + userID + " with name " + fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(ERROR)
                    .body("Error occurred while retrieving file metadata");
        }
    }
    /**
     * Sends the URL to download the file.
     * @param fileName name of the file.
     * @param userID id of the user.
     * @return returns a Response Entity with the URL or error msg
     */
    @PostMapping("/download")
    public ResponseEntity<?> download(
            @RequestParam("fileName") String fileName,
            @RequestParam("userID") String userID) {
        try {
            //todo: authenticate user.
            //todo: optionally decrypt the binary.
            FileMetadata metadata = firebaseStorageService.getFile(userID, fileName);
            System.out.println(metadata);

            if (metadata != null) {
                URL signedUrl = firebaseStorageService.generateSignedURL(metadata.getFileName(), 2, TimeUnit.MINUTES);
                if (signedUrl == null) {
                    return ResponseEntity.status(ERROR).body("Error generating signed URL");
                }
                return ResponseEntity.status(OK).body(signedUrl.toString());
            } else {
                return ResponseEntity.status(FILE_NOT_FOUND)
                        .body("File not found for user " + userID + " with name " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(ERROR)
                .body("Error occurred while downloading the file");
        }

    }

    @PostMapping("/delete")
    public ResponseEntity<?> delete(
        @RequestParam("fileName") String fileName,
        @RequestParam("userID") String userID){
        try{
            if(fileName == "" || userID == "" || fileName == null || userID == null){
                return ResponseEntity.status(ERROR)
                .body("Error occurred when deleting the file.");
            }
            int deleted = firebaseStorageService.deleteFile(fileName, userID);

            if (deleted == 0){
                return ResponseEntity.status(OK).body("File deleted.");
            }
            return ResponseEntity.status(ERROR).body("File not deleted.");
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(ERROR)
                .body("Error occurred when deleting the file.");
        }
    }

    @PostMapping("/download-zip")
    public ResponseEntity<?> downloadZip(
        @RequestParam("userID") String userID,
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    ) {
        try{
            URL url = firebaseStorageService.downloadZip(userID, startDate, endDate);
            if(url == null){
                return ResponseEntity.status(ERROR).body("Something went wrong");
            }
            return ResponseEntity.status(OK).body(url.toString());
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(ERROR)
                .body("Error occured when downloading files.");
        }
    }

    /**
     * Get all documents for a specific user.
     * @param userID the user ID to fetch documents for.
     * @return list of file metadata for the user.
     */
    @GetMapping("/list")
    public ResponseEntity<?> listFiles(@RequestParam("userID") String userID) {
        try {
            List<FileMetadata> files = firebaseStorageService.getAllFilesForUser(userID);
            return ResponseEntity.status(OK).body(files);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(ERROR)
                .body("Error occurred while fetching files list.");
        }
    }

    /**
     * Delete all documents for a specific user.
     * @param userID the user ID to delete documents for.
     * @return confirmation message with number of files deleted.
     */
    @PostMapping("/delete-all")
    public ResponseEntity<?> deleteAllFiles(@RequestParam("userID") String userID) {
        System.out.println("DELETE-ALL endpoint called for user: " + userID);
        try {
            int deletedCount = firebaseStorageService.deleteAllFilesForUser(userID);
            System.out.println("Deleted count: " + deletedCount);
            
            if (deletedCount < 0) {
                System.err.println("Error: deletedCount was negative");
                return ResponseEntity.status(ERROR)
                    .body("Error occurred while deleting files.");
            }
            
            String message = "Successfully deleted " + deletedCount + " file(s).";
            System.out.println(message);
            return ResponseEntity.status(OK).body(message);
        } catch (Exception e) {
            System.err.println("Exception in deleteAllFiles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(ERROR)
                .body("Error occurred while deleting files: " + e.getMessage());
        }
    }

}
