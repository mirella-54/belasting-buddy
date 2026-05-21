package com.group8.docprocessing.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

import com.group8.docprocessing.model.FileMetadata;

@Service
public class GridFsStorageService {

    /**
     * The GridFsTemplate used for interacting with MongoDB GridFS.
     */
    private final GridFsTemplate gridFsTemplate;

    public GridFsStorageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    /**
     * Saves the uploaded file to MongoDB GridFS.
     * @param file The file to save.
     * @param fileId The unique ID to associate with the file.
     * @return The ID used to retrieve the file (the MongoDB object ID).
     */
    public String storeFile(MultipartFile file, String fileId) throws IOException {
        FileMetadata metadata = new FileMetadata(fileId); 
    
        return gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                metadata
        ).toHexString(); 
    }

    /**
     * Retrieves a file from GridFS. (Used by the OCR pipeline, e.g.)
     * @param fileId The unique ID associated with the file.
     * @return The GridFSFile object, or null if not found.
     */
    public GridFSFile getFile(String fileId) {
        return gridFsTemplate.findOne(new Query(Criteria.where("metadata.fileId").is(fileId)));
    }

    /**
     * Deletes a file from GridFS using the unique fileId.
     * @param fileId The unique ID associated with the file to delete.
     */
    public void deleteFile(String fileId) {
        gridFsTemplate.delete(new Query(Criteria.where("metadata.fileId").is(fileId)));
    }

    public InputStream getFileAsStream(String fileId) throws IOException {
        GridFSFile file = getFile(fileId);
        if (file == null) {
            return null;
        }
        return gridFsTemplate.getResource(file).getInputStream();
    }

    public String getFileName(String fileId) {
        return gridFsTemplate.findOne(new Query(Criteria.where("metadata.fileId").is(fileId))).getFilename();
    }
}