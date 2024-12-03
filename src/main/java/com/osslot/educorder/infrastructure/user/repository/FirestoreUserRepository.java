package com.osslot.educorder.infrastructure.user.repository;

import com.google.cloud.firestore.Firestore;
import com.osslot.educorder.domain.user.model.User;
import com.osslot.educorder.domain.user.repository.UserRepository;
import com.osslot.educorder.infrastructure.user.repository.entity.UserEntity;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FirestoreUserRepository implements UserRepository {

  private final Firestore firestore;
  private static final Map<String, User> usersCache = new ConcurrentHashMap<>();

  @Override
  public Optional<User> findByUserId(User.UserId userId) {
    return Optional.ofNullable(
        usersCache.computeIfAbsent(
            userId.id(),
            aUserId -> {
              try {
                var usersEntities =
                    firestore
                        .collection(UserEntity.PATH)
                        .whereEqualTo("user.id", userId.id())
                        .get()
                        .get()
                        .toObjects(UserEntity.class);
                if (usersEntities.isEmpty()) {
                  return null;
                }
                return usersEntities.getFirst().toDomain();
              } catch (InterruptedException | ExecutionException e) {
                log.error("Error fetching user settings", e);
                return null;
              }
            }));
  }

  @Override
  public void add(User user) {
    var userEntity = UserEntity.fromDomain(user);
    firestore.collection(UserEntity.PATH).document().set(userEntity);
  }
}
