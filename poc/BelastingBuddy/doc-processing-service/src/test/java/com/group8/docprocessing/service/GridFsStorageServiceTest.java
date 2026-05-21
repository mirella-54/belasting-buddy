package com.group8.docprocessing.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GridFsStorageServiceTest {

    /**
     * A mock representation of the GridFsTemplate used for testing purposes.
     */
    @Mock
    private GridFsTemplate gridFsTemplate;

    /**
     * A mock representation of a multipart file used for testing purposes.
     */
    @Mock
    private MultipartFile mockFile;

    /**
     * The service under test, which interacts with the GridFsTemplate.
     */
    @InjectMocks
    private GridFsStorageService gridFsStorageService;

    /**
     * A constant representing the test file ID used in the test cases.
     */
    private static final String TEST_FILE_ID = "PROC-12345";  

    @Test
    void getFileShouldCallGridFsTemplateFindOneWithCorrectQueryWhenFileExists() {
        GridFSFile mockGridFsFile = mock(GridFSFile.class);

        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(mockGridFsFile);

        GridFSFile result = gridFsStorageService.getFile(TEST_FILE_ID);

        verify(gridFsTemplate, times(1)).findOne(any(Query.class));
        assertEquals(mockGridFsFile, result);
    }

    @Test
    void getFileShouldReturnNullWhenFileDoesNotExist() {
        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(null);

        GridFSFile result = gridFsStorageService.getFile(TEST_FILE_ID);

        verify(gridFsTemplate, times(1)).findOne(any(Query.class));
        assertNull(result);
    }

    @Test
    void deleteFileShouldCallGridFsTemplateDeleteWithCorrectQuery() {
        gridFsStorageService.deleteFile(TEST_FILE_ID);

        verify(gridFsTemplate, times(1)).delete(any(Query.class));
    }
}