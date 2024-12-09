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

@Configuration
public class FireStoreConfiguration {

  //  private static final FirestoreEmulatorContainer firestoreEmulatorContainer =
  //      new FirestoreEmulatorContainer(
  //          DockerImageName.parse(
  //              "gcr.io/google.com/cloudsdktool/google-cloud-cli:497.0.0-emulators"));
  //
  //  @Bean
  //  public Firestore firestore() {
  //    firestoreEmulatorContainer.start();
  //    FirestoreOptions options =
  //        FirestoreOptions.getDefaultInstance().toBuilder()
  //            .setProjectId(generateRandomString().toLowerCase())
  //            .setCredentials(NoCredentials.getInstance())
  //            .setHost(firestoreEmulatorContainer.getEmulatorEndpoint())
  //            .build();
  //    return options.getService();
  //  }
  //
  //  private String generateRandomString() {
  //    int leftLimit = 97; // letter 'a'
  //    int rightLimit = 122; // letter 'z'
  //    int targetStringLength = 10;
  //    Random random = new Random();
  //
  //    return random
  //        .ints(leftLimit, rightLimit + 1)
  //        .limit(targetStringLength)
  //        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
  //        .toString();
  //  }

  private static final String CREDENTIALS_FILE_PATH =
      "src/main/resources/firestore-credentials.json";

  @Bean
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
