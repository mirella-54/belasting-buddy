# Doc Processing Service

## Overview
The **Doc Processing Service** is a Spring Boot microservice responsible for processing and managing user documents.  
It provides endpoints to start document processing, interact with downstream services, and manage extracted data asynchronously via Kafka.

## Endpoints

### `POST /documents/upload`

#### **Description**
Uploads a document (PDF or image) and triggers the asynchronous document extraction pipeline.  
Once uploaded, the service validates the file, attaches any provided metadata, and publishes a message to Kafka for downstream processing.

#### **Example Request (curl)**
```bash
curl --location 'http://localhost:8082/documents/upload' \
--header 'Authorization: Bearer <JWT Token>' \
--form 'file=@"/path/to/file.pdf"'
```

## Logging

To verify successful processing, look for the following logs:

   * `Successfully initiated Kafka send for ID: <document_id>`
   * `Successfully sent processed event for Document ID: <document_id> to topic document_processed`
   * `Successfully deleted temporary file with Processing ID: <document_id>`

