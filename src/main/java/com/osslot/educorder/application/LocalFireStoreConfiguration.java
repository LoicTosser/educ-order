package com.osslot.educorder.application;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class LocalFireStoreConfiguration {

  private static final String CREDENTIALS_FILE_PATH =
      "src/main/resources/firestore-credentials.json";

  @Bean
  @Profile("!prod")
  public Firestore firestore() {
    try (InputStream serviceAccount = new FileInputStream(CREDENTIALS_FILE_PATH)) {
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
