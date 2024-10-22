package com.osslot.educorder.infrastructure;

import com.google.cloud.NoCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class FireStoreConfiguration {

  private static final FirestoreEmulatorContainer firestoreEmulatorContainer =
      new FirestoreEmulatorContainer(
          DockerImageName.parse(
              "gcr.io/google.com/cloudsdktool/google-cloud-cli:497.0.0-emulators"));

  @Bean
  public Firestore firestore() {
    firestoreEmulatorContainer.start();
    FirestoreOptions options =
        FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId(generateRandomString().toLowerCase())
            .setCredentials(NoCredentials.getInstance())
            .setHost(firestoreEmulatorContainer.getEmulatorEndpoint())
            .build();
    return options.getService();
  }

  private String generateRandomString() {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;
    Random random = new Random();

    return random
        .ints(leftLimit, rightLimit + 1)
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }
}
