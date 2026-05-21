# BelastingBuddy - Proof of Concept

BelastingBuddy is our (microservices based) tax management app for Dutch freelancers. It handles document storage, hour tracking, and eventually tax report generation.

## 1. PoC Goal

The goal of this PoC is to validate the core functionalities and quality attributes of the proposed system. Specifically, it aims to implement and showcase the most important features through:

- Secure user authentication.
- Encryption of data.
- Integration with third-party productivity tools (ClickUp and Clockify).
- An OCR pipeline to extract essential financial data from uploaded invoices and bills.
- A workable User Interface (UI) with connected endpoints for presentation.
- A scalable and reliable system via Kafka. 

## 2. PoC Architecture

The system uses 4 microservices, each with its own database:

- User Management (port 8080) - JWT authentication, user accounts
- Document Processing (port 8081) - Document upload and data extraction
- Hour Tracking (port 8082) - Manual and synced hour logging
- Document Storage (port 8083) - Firebase-based long-term archiving

## 3. Explanation of Key Design Choices

The core architectural decision is the Microservice Architecture. This was selected over a Monolithic architecture (due to lack of flexibility/reliability) and a Micro-kernel architecture (due to management overhead and complex communication).

| Component           | Design Choice                            | Justification |
|--------------------|-----------------------------------------|---------------|
| Backend Framework   | Spring Boot and Java for the microservices | Chosen for its vast ecosystem, maturity, strong support for modular microservices, and community support |
| User Authentication | JWT-based Authentication  | Implemented to balance security and scalability, providing stateless authentication/authorization and secure communication between the microservices |
| Document Pipeline   | Kafka as a Message Queue   | Used to handle document uploads asynchronously, acting as a reliable buffer to prevent service overload during peak times |
| Frontend UI         | React and Typescript      | Chosen for implementing the web application's user interface  |


## 4. Requirements and Local Run Instructions

### Prerequisites

- Java 17
- Maven
- Docker (for MongoDB and the gateway)

### Start Backend

```bash
cd poc/Belastingbuddy
docker compose up -d
```

### Start Frontend

```bash
cd poc/BelastingBuddy/front-end
npm install
npm run dev
```

## 5. List of Implemented Features

- Users can register an account, log in, and log out securely.
- Users can upload documents, which are processed through an optical character recognition pipeline and stored securely in the document-storage service.
- Users can delete uploaded documents.
- Users can manually track their hours worked.
- Users can add API keys for ClickUp and Clockify, and synchronise hours from these platforms to BelastingBuddy.
- Users can download a .zip file containing all uploaded documents within a specified timeframe via a secure link.

## 6. List of Conducted Experiments

We conducted experiments to evaluate the reliability, scalability, and interoperability of the application.

To assess reliability, we simulated a failure by shutting down the doc-processing service during document processing. We then verified that processing resumed successfully once the service was restored, thanks to Kafka maintaining backups of in-process documents.

For interoperability, we confirmed that API keys for both ClickUp and Clockify could be added, manual hours could be entered, and all hour-tracking services functioned correctly in parallel.

To evaluate scalability, we used JMeter to upload up to 10,000 documents simultaneously and verified that all documents were processed and stored correctly.

Detailed descriptions of these experiments are provided in the report.
