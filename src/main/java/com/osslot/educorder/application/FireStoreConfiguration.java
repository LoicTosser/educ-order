package com.osslot.educorder.application;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
public class FireStoreConfiguration {

  @Value("${firestore.credentials}")
  String firestoreCredentials;

  @Bean
  @Profile("prod")
  public Firestore firestore() {
    log.info("Using Firestore credentials from environment variable: {}", firestoreCredentials);
    try (InputStream serviceAccount = new ByteArrayInputStream(firestoreCredentials.getBytes())) {
      var options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      FirebaseApp.initializeApp(options);
      return FirestoreClient.getFirestore();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
