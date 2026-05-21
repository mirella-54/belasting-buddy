package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcrServiceTest {

    /**
     * Mocked service for interacting with GridFS storage.
     * Used to simulate file retrieval and metadata operations during tests.
     */
    @Mock
    private GridFsStorageService gridFsStorageService;

    /**
     * The service under test, with dependencies injected as mocks.
     * This is the main class being tested in this test suite.
     */
    @InjectMocks
    private OcrService ocrService;

    /**
     * A mock processing ID used for testing purposes.
     */
    private static final String PROC_ID = "PROC-123";

    /**
     * A mock GridFS ID representing a file in the storage system.
     */
    private static final String GRID_FS_ID = "gridfs-abc";
    /**
     * Path to the Tesseract data files used for OCR processing.
     */
    private static final String TESS_DATA_PATH = "/test/tessdata";

    /**
     * Mocked OCR text to simulate the output of Tesseract during tests.
     */
    private static final String FAKE_OCR_TEXT = "This is the extracted text.";

    /**
     * Mocked construction of the Tesseract object to simulate OCR behavior during tests.
     */
    private MockedConstruction<Tesseract> tesseractMockedConstruction;
    
    /**
     * Captured mock instance of ITesseract constructed during setUp.
     */
    private ITesseract tesseractMock; 

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ocrService, "tessDataPath", TESS_DATA_PATH);
        tesseractMockedConstruction = Mockito.mockConstruction(Tesseract.class, (mock, context) -> {
            tesseractMock = (ITesseract) mock;
            when(tesseractMock.doOCR(any(java.io.File.class))).thenReturn(FAKE_OCR_TEXT);
        });
    }

    @AfterEach
    void tearDown() {
        if (tesseractMockedConstruction != null) {
            tesseractMockedConstruction.close();
        }
    }

    @Test
    void performOcrAndExtractionSuccessForPng() throws Exception { 
        InputStream mockInputStream = new ByteArrayInputStream("image data".getBytes());
        when(gridFsStorageService.getFileAsStream(PROC_ID)).thenReturn(mockInputStream);
        when(gridFsStorageService.getFileName(PROC_ID)).thenReturn("test_document.png");


        ExtractedData result = ocrService.performOcrAndExtraction(PROC_ID, GRID_FS_ID, "test-user-id");

        assertNotNull(result, "The extracted data should not be null.");
        assertEquals(GRID_FS_ID, result.getDocumentId(), "The document ID should match the input GridFS ID.");
        // Content is now JSON structure with rawText, amount, and vat
        assertTrue(
            result.getExtractedContent().contains(FAKE_OCR_TEXT),
            "The content should contain the mocked Tesseract output in rawText field"
        );
        assertTrue(
            result.getExtractedContent().contains("\"vat\""),
            "The content should contain VAT field"
        );

        verify(gridFsStorageService).getFileAsStream(PROC_ID);
        verify(gridFsStorageService).getFileName(PROC_ID);
        
        verify(tesseractMock).setDatapath(TESS_DATA_PATH);
        verify(tesseractMock).setLanguage("eng");
        verify(tesseractMock).doOCR(any(java.io.File.class)); 
    }
    
    @Test
    void performOcrAndExtractionSuccessForTiff() throws Exception { 
        InputStream mockInputStream = new ByteArrayInputStream("tiff data".getBytes());
        when(gridFsStorageService.getFileAsStream(PROC_ID)).thenReturn(mockInputStream);
        when(gridFsStorageService.getFileName(PROC_ID)).thenReturn("scan.tiff");

        ExtractedData result = ocrService.performOcrAndExtraction(PROC_ID, GRID_FS_ID, "test-user-id");

        assertNotNull(result, "The extracted data should not be null.");
        // Content is now JSON structure with rawText, amount, and vat
        assertTrue(
            result.getExtractedContent().contains(FAKE_OCR_TEXT),
            "The content should contain the mocked Tesseract output in rawText field"
        );
        assertTrue(
            result.getExtractedContent().contains("\"vat\""),
            "The content should contain VAT field"
        );
        
        verify(tesseractMock).doOCR(any(java.io.File.class));
    }



    @Test
    void performOcrAndExtractionReturnsNullWhenInputStreamIsNull() throws Exception { 
        when(gridFsStorageService.getFileAsStream(PROC_ID)).thenReturn(null); 

        ExtractedData result = ocrService.performOcrAndExtraction(PROC_ID, GRID_FS_ID, "test-user-id");

        assertNull(result, "Should return null when the input stream is null (file not found).");
        verify(gridFsStorageService, never()).getFileName(anyString());
        assertEquals(0, tesseractMockedConstruction.constructed().size(), 
                     "Tesseract should not be constructed if file retrieval fails.");
    }
}