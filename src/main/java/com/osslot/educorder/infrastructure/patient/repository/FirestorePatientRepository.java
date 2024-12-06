package com.osslot.educorder.infrastructure.patient.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.model.Patient.PatientId;
import com.osslot.educorder.domain.patient.repository.PatientRepository;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.infrastructure.patient.repository.entity.PatientEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class FirestorePatientRepository implements PatientRepository {

  private static final int CHUNK_SIZE = 10;

  private final Map<String, Map<String, Patient>> patientsByIdsCache = new ConcurrentHashMap<>();

  private final Firestore firestore;
  private final GoogleSheetPatientRepository googleSheetPatientRepository;

  @PostConstruct
  public void persistInDB() {
    var patients = googleSheetPatientRepository.getAllPatients();
    patients.forEach(
        patient -> {
          var patientEntity = PatientEntity.fromDomain(patient);
          firestore.collection(PatientEntity.PATH).document().set(patientEntity);
        });
  }

  @Override
  public List<Patient> findAllByUserId(UserId userId) {
    return patientsByIdsCache.computeIfAbsent(userId.id(), this::findAllByUserId).values().stream().toList();
  }

  @Override
  public Map<PatientId, Patient> findAllByIds(UserId userId, Set<PatientId> ids) {
    updateCacheWithMissingPatients(userId, ids);
    Map<String, Patient> usersPatientsCache =
        patientsByIdsCache.getOrDefault(userId.id(), Map.of());
    return ids.stream()
        .map(id -> usersPatientsCache.getOrDefault(id.id(), null))
        .filter(Objects::nonNull)
        .filter(patient -> patient.userId().equals(userId))
        .collect(Collectors.toMap(Patient::id, patient -> patient));
  }

  @Override
  public Optional<Patient> findById(UserId userId, PatientId patientId) {
    Map<String, Patient> usersPatientsCache =
        patientsByIdsCache.getOrDefault(userId.id(), new HashMap<>());
    Patient patient = usersPatientsCache.computeIfAbsent(patientId.id(), this::findById);
    patientsByIdsCache.put(userId.id(), usersPatientsCache);
    return Optional.ofNullable(patient);
  }

  private Map<String, Patient> findAllByUserId(String userId) {
    try {
      var patientEntities =
              firestore
                      .collection(PatientEntity.PATH)
                      .whereEqualTo("userId", userId)
                      .get()
                      .get()
                      .toObjects(PatientEntity.class);
      return patientEntities.stream().map(PatientEntity::toDomain).collect(Collectors.toMap(patient -> patient.id().id(), patient -> patient));
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching patients for user {}", userId, e);
      return new HashMap<>();
    }

  }

  private void updateCacheWithMissingPatients(UserId userId, Set<PatientId> ids) {
    Map<String, Patient> usersPatientsCache =
        patientsByIdsCache.getOrDefault(userId.id(), new HashMap<>());
    var patientIdsNotInCache =
        ids.stream()
            .map(PatientId::id)
            .filter(id -> !usersPatientsCache.containsKey(id))
            .collect(Collectors.toSet());
    var fetchedPatients = findAllByIdsByChunks(patientIdsNotInCache);
    usersPatientsCache.putAll(fetchedPatients);
    patientsByIdsCache.put(userId.id(), usersPatientsCache);
  }

  private Map<String, Patient> findAllByIdsByChunks(Set<String> patientIdsNotInCache) {
    AtomicInteger counter = new AtomicInteger();
    Map<Integer, List<String>> mapOfChunks =
        patientIdsNotInCache.stream()
            .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / CHUNK_SIZE));
    // Create a list containing the lists of chunks
    List<List<String>> patientIdsChunks = new ArrayList<>(mapOfChunks.values());
    return patientIdsChunks.stream()
        .flatMap(
            patientIdsChunk -> {
              var activitiesCollection = firestore.collection(PatientEntity.PATH);
              Query query = activitiesCollection.whereIn("id", patientIdsChunk);
              try {
                return query.get().get().getDocuments().stream();
              } catch (InterruptedException | ExecutionException e) {
                log.error("Error fetching patients by Ids", e);
              }
              return Stream.empty();
            })
        .map(document -> document.toObject(PatientEntity.class).toDomain())
        .collect(Collectors.toMap(patient -> patient.id().id(), patient -> patient));
  }

  private @Nullable Patient findById(String aPatientId) {
    try {
      var patientEntities =
          firestore
              .collection(PatientEntity.PATH)
              .whereEqualTo("id", aPatientId)
              .get()
              .get()
              .toObjects(PatientEntity.class);
      if (patientEntities.isEmpty()) {
        return null;
      }
      return patientEntities.getFirst().toDomain();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching patient {}", aPatientId, e);
      return null;
    }
  }
}
