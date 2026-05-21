package com.group8.documentstorage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile("!test")
public class FirebaseConfig {
    /**
     * Function to initiate firebase storage for the documents.
     */
    @PostConstruct
    public void initFirebase() {
        try { // configure the storage database
            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(
                            "belastingbuddy-32403.firebasestorage.app")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);

            }
            System.out.println("Success setting up Firebase");
        } catch (Exception e) {
            System.err.println("Firebase file database initialization failed: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * this function sets up the firestore storage for metadata of each file.
     * @return returns function set up.
     * @throws IOException in case something fails it throws an exception.
     */
    @Bean
    public Firestore firestore() throws IOException {

        InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream("firebase-service-account.json");

        FirestoreOptions options = FirestoreOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        return options.getService();
    }

}
