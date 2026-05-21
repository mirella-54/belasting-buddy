package com.group8.docprocessing.service;

import com.group8.docprocessing.model.ExtractedData;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class OcrService {

    /**
     * Logger instance for logging messages in the OcrService class.
     */
    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    /**
     * Service for interacting with GridFS storage.
     */
    private final GridFsStorageService gridFsStorageService;

    /**
     * Path to the Tesseract data files, used for OCR processing.
     * This value is injected from the application properties.
     */
    @Value("${tesseract.datapath:/usr/share/tesseract-ocr/5/tessdata}")
    private String tessDataPath;

    public OcrService(GridFsStorageService gridFsStorageService) {
        this.gridFsStorageService = gridFsStorageService;
    }

    public ExtractedData performOcrAndExtraction(String processingId, String gridFsId, String userId) {
        log.info("Starting OCR for Processing ID: {} (GridFS ID: {}) User ID: {}", processingId, gridFsId, userId);

        File tempFile = null;

        try {
            InputStream inputStream = gridFsStorageService.getFileAsStream(processingId);

            if (inputStream == null) {
                log.error("File not found in GridFS using ID: {}", processingId);
                return null;
            }

            String originalFilename = gridFsStorageService.getFileName(processingId);
            
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
                extension = originalFilename.substring(dotIndex);
            }

            tempFile = File.createTempFile("ocr-", extension); 
            
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                inputStream.transferTo(out);
            }
            
            ImageIO.scanForPlugins();

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");

            String extractedText = tesseract.doOCR(tempFile);
            
            log.info("OCR extraction completed for document {}: {} chars extracted",
                    gridFsId, extractedText.length());

            // Parse the extracted text to find amount and VAT
            String amount = extractAmountFromText(extractedText);
            String vat = extractVatFromText(extractedText);
            
            // If VAT not found but amount is found, calculate VAT from total (21% included)
            if (vat == null && amount != null) {
                try {
                    // Remove currency symbols and parse the amount
                    String cleanAmount = amount.replaceAll("[^0-9.]", "");
                    double totalAmount = Double.parseDouble(cleanAmount);
                    
                    // Calculate VAT (assuming 21% VAT included in total)
                    // VAT = Total × (21 / 121)
                    double vatAmount = totalAmount * (21.0 / 121.0);
                    vat = String.format("$%.2f", vatAmount);
                    log.info("VAT not found, calculated from total: {} → {}", amount, vat);
                } catch (NumberFormatException e) {
                    vat = "21%";
                    log.warn("Could not calculate VAT from amount: {}", amount);
                }
            } else if (vat == null) {
                vat = "21%";
                log.info("No amount found, defaulting VAT to 21%");
            }
            
            // Create structured JSON content
            String structuredContent = String.format(
                "{\"rawText\": %s, \"amount\": %s, \"vat\": %s}",
                escapeJson(extractedText),
                amount != null ? "\"" + amount + "\"" : "null",
                vat != null ? "\"" + vat + "\"" : "null"
            );

            ExtractedData extractedData = new ExtractedData();
            extractedData.setId(processingId);
            extractedData.setDocumentId(gridFsId);
            extractedData.setExtractedContent(structuredContent);
            extractedData.setUserId(userId);

            log.info("Structured data created - Amount: {}, VAT: {}", amount, vat);

            return extractedData;
            
        } catch (TesseractException e) {
            log.error("OCR failed: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during OCR processing", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }

        return null;
    }

    /**
     * Extracts the total amount from invoice text.
     * Looks for patterns like "TOTAL AMOUNT $1,600.00" or "Total: €123.45"
     * @param text the text to extract the amount from
     * @return the extracted amount as a string, or null if not found
     */
    private String extractAmountFromText(String text) {
        try {
            // Look for total amount patterns
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?i)(?:total\\s*amount|total|amount)[:\\s]*[€$£]?\\s*([0-9,]+\\.?[0-9]*)",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher matcher = pattern.matcher(text);
            
            if (matcher.find()) {
                String amount = matcher.group(1).replace(",", "");
                return "$" + amount;
            }
        } catch (Exception e) {
            log.warn("Failed to extract amount: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extracts VAT from invoice text.
     * Looks for patterns like "VAT: 21%" or "Tax: $50.00"
     * @param text the text to extract the VAT from
     * @return the extracted VAT as a string, or null if not found
     */
    private String extractVatFromText(String text) {
        try {
            // Look for VAT/Tax patterns
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?i)(?:vat|tax|btw)[:\\s]*[€$£]?\\s*([0-9,]+\\.?[0-9]*%?)",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher matcher = pattern.matcher(text);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("Failed to extract VAT: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Escapes a string for JSON format.
     * @param text the text to escape
     * @return the escaped text suitable for JSON
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "null";
        }
        return "\"" + text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            + "\"";
    }
}
