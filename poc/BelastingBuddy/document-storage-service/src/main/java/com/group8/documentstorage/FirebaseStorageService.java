package com.group8.documentstorage;

import com.google.api.core.ApiFuture;
// import com.google.cloud.firestore.Blob; // Remove this line
import com.google.cloud.storage.Blob; // Add this line
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import com.google.cloud.storage.HttpMethod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.io.File;
import java.nio.file.Files;
import java.io.FileOutputStream;

@Service
public class FirebaseStorageService {
    /** firestore class initialisation. */
    private final Firestore firestore;
    /** buffer size for the bytes array. */
    private static final int BUFFER_SIZE = 16384;
    /**
     * firestore constructor.
     * @param storage variable for firestore class.
     */
    @Autowired
    public FirebaseStorageService(final Firestore storage) {
        this.firestore = storage;
    }

    /**
     * upload file to firebase storage.
     * @param fileStream  file.
     * @param fileName    file name.
     * @param contentType file extension.
     * @return returns the firebase link if worked otherwise nothing.
     */

    public String uploadFile(final InputStream fileStream,
            final String fileName, final String contentType) {
        try {
            Storage storage = StorageClient.getInstance()
                    .bucket().getStorage();
            String bucketName = StorageClient.getInstance()
                    .bucket().getName();

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                    .setContentType(contentType)
                    .build();

            byte[] bytes = toByteArray(fileStream);
            
            storage.create(blobInfo, bytes);

            return "https://storage.googleapis.com/"
                + bucketName + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * saves file metadata to the firestore database.
     * @param metadata file metadata constructed as in the model.
     * @return returns 0 if worked fine or -1 on error.
     */
    public int saveFileMetadata(final FileMetadata metadata) {
        try {
            CollectionReference files = firestore.collection("files");
            files.add(metadata);
            System.out.println("Metadata saved.");
            return 0;
        } catch (Exception e) {
            System.err.println("Error saving metadata: " + e.getMessage());
            e.printStackTrace();
            return -1;

        }
    }
    /**
     *  function to get file content and metadata.
     * @param userID id of the user file belongs to.
     * @param fileName name of the file to retrieve.
     * @return return metadata or null in case it didn't work.
     */
    public FileMetadata getFile( final String userID, final String fileName) {
        try{
            CollectionReference files = firestore.collection("files");
            ApiFuture<QuerySnapshot> query = files
                .whereEqualTo("userID", userID)
                .whereEqualTo("fileName", fileName)
                .get();
            
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            System.out.println(documents);
            if (documents.isEmpty()) {
                return null;
            }
            QueryDocumentSnapshot doc = documents.get(0);
            FileMetadata metadata = doc.toObject(FileMetadata.class);
            return metadata;
        } catch (Exception e){
            System.err.println("Error getting file metadata: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }
    /**
     * This function generates a signed URL for download.
     * @param fileName name of the file.
     * @param duration time URL needs to be valid for.
     * @param unit minutes/seconds the URL needs to be valid for.
     * @return returns the URL. 
     */
    public URL generateSignedURL (final String fileName, final long duration, final TimeUnit unit ){
        Storage storage = StorageClient.getInstance()
                                .bucket().getStorage();
        String bucketName = StorageClient.getInstance()
                                .bucket().getName();
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();

        URL signedUrl = storage.signUrl(
                blobInfo, 
                duration, 
                unit,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                Storage.SignUrlOption.withV4Signature() 
        );

        return signedUrl;
    }
    
    /**
     *  function to delete a file from Firestore.
     * @param fileName name of the file.
     * @param userID userID.
     * @return returns 0 on success or -1 on failure.
     */
    public int deleteFile(final String fileName, final String userID) {
        try {
            CollectionReference files = firestore.collection("files");
            ApiFuture<QuerySnapshot> query = files
                .whereEqualTo("userID", userID)
                .whereEqualTo("fileName", fileName)
                .get();
                
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            if (documents.isEmpty()) {
                return -1;
            }
            Storage storage = StorageClient.getInstance()
                    .bucket().getStorage();
            String bucketName = StorageClient.getInstance()
                    .bucket().getName();
            boolean deleted = storage.delete(BlobId.of(bucketName, fileName));
            if(deleted){
                for (QueryDocumentSnapshot doc : documents) { // Remove metadata.
                    doc.getReference().delete();
                
                }
                return 0;
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Get all files for a specific user.
     * @param userID the user ID to fetch files for.
     * @return list of FileMetadata objects for the user.
     */
    public List<FileMetadata> getAllFilesForUser(final String userID) {
        try {
            CollectionReference files = firestore.collection("files");
            ApiFuture<QuerySnapshot> query = files
                .whereEqualTo("userID", userID)
                .get();
            
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            
            if (documents.isEmpty()) {
                return List.of();
            }
            
            return documents.stream()
                .map(doc -> doc.toObject(FileMetadata.class))
                .toList();
        } catch (Exception e) {
            System.err.println("Error getting files for user: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Delete all files for a specific user.
     * @param userID the user ID to delete files for.
     * @return number of files deleted, or -1 on error.
     */
    public int deleteAllFilesForUser(final String userID) {
        try {
            System.out.println("deleteAllFilesForUser called for userID: " + userID);
            CollectionReference files = firestore.collection("files");
            ApiFuture<QuerySnapshot> query = files
                .whereEqualTo("userID", userID)
                .get();
            
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            System.out.println("Found " + documents.size() + " documents to delete");
            
            if (documents.isEmpty()) {
                return 0;
            }

            Storage storage = StorageClient.getInstance()
                    .bucket().getStorage();
            String bucketName = StorageClient.getInstance()
                    .bucket().getName();

            int deletedCount = 0;
            int failedCount = 0;
            
            for (QueryDocumentSnapshot doc : documents) {
                FileMetadata metadata = doc.toObject(FileMetadata.class);
                String fileName = metadata.getFileName();
                System.out.println("Attempting to delete file: " + fileName);
                
                try {
                    // Try to delete file from Firebase Storage (may not exist if duplicate filenames)
                    boolean storageDeleted = storage.delete(BlobId.of(bucketName, fileName));
                    System.out.println("Storage delete result for " + fileName + ": " + storageDeleted);
                    
                    // Always delete metadata from Firestore, even if storage file doesn't exist
                    // (This handles cases where multiple metadata entries point to same file)
                    doc.getReference().delete();
                    deletedCount++;
                    System.out.println("Deleted metadata for: " + fileName 
                        + " (storage existed: " + storageDeleted + ")");
                } catch (Exception e) {
                    failedCount++;
                    System.err.println("Exception deleting file " + fileName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Deletion complete. Deleted: " + deletedCount + ", Failed: " + failedCount);
            return deletedCount;
        } catch (Exception e) {
            System.err.println("Error deleting files for user: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public URL downloadZip(final String userID, final String startDateStr, final String endDateStr){
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("d-MMM-yyyy", Locale.ENGLISH);
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = startCal.getTime();

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            Date endOfDay = endCal.getTime();

            // Convert to com.google.cloud.Timestamp
            com.google.cloud.Timestamp startTs = com.google.cloud.Timestamp.of(startOfDay);
            com.google.cloud.Timestamp endTs   = com.google.cloud.Timestamp.of(endOfDay);

            System.out.println(startTs);
            System.out.println(endTs);

            CollectionReference files = firestore.collection("files");
            ApiFuture<QuerySnapshot> query = files
                    .whereEqualTo("userID", userID)
                    .whereGreaterThanOrEqualTo("uploadTimestamp", startTs)
                    .whereLessThanOrEqualTo("uploadTimestamp", endTs)
                    .get();

            List<QueryDocumentSnapshot> documents = query.get().getDocuments();

            if (documents.isEmpty()) {
                System.out.println("No files found for this user in the given date range.");
                return null;
            }
            System.out.println(documents);
            Storage storage = StorageClient.getInstance()
                    .bucket().getStorage();
            String bucketName = StorageClient.getInstance()
                    .bucket().getName();

            File tempZip = File.createTempFile("files_", ".zip");
            
            try (FileOutputStream fos = new FileOutputStream(tempZip);
                ZipOutputStream zos = new ZipOutputStream(fos)) {

                for (QueryDocumentSnapshot doc : documents) {
                    FileMetadata metadata = doc.toObject(FileMetadata.class);
                    String fileName = metadata.getFileName();

                    Blob blob = storage.get(bucketName, fileName);
                    if (blob == null) {
                        System.out.println("File not found in bucket: " + fileName);
                        continue;
                    }

                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);
                    blob.downloadTo(zos);
                    zos.closeEntry();
                }
            }

            String zipFileName = "archives/" + UUID.randomUUID() + ".zip";
            BlobInfo zipBlob = BlobInfo.newBuilder(bucketName, zipFileName)
                    .setContentType("application/zip")
                    .build();

            storage.create(zipBlob, Files.readAllBytes(tempZip.toPath()));

            // Generate 1-hour signed URL
            URL signedUrl = storage.signUrl(
                    zipBlob,
                    2,
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                    Storage.SignUrlOption.withV4Signature()
            );

            // Clean up
            tempZip.delete();
            System.out.println(signedUrl);
            return signedUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
    * transfer bytes to array.
    *
    * @param input file usually.
    * @return returns the array of bytes.
    * @throws IOException throws exception in case of error.
    */
    private byte[] toByteArray(final InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[BUFFER_SIZE];

        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

}
