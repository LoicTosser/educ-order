package com.osslot.educorder.infrastructure.repository.entity;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.infrastructure.activities.repository.entity.ActivityEntity;
import java.time.ZoneOffset;
import org.assertj.core.api.WithAssertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ActivityEntityTest implements WithAssertions {

  @Nested
  class FromDomain {
    @Test
    void fromDomain_withValidActivity_shouldReturnActivityEntity() {
      // Given
      Activity activity = Instancio.create(Activity.class);

      // When
      var activityEntity = ActivityEntity.fromDomain(activity);

      // Then
      assertThat(activityEntity.getId()).isEqualTo(activity.id());
      assertThat(activityEntity.getEventId()).isEqualTo(activity.eventId());
      assertThat(activityEntity.getBeginDate())
          .isEqualTo(activity.beginDate().withZoneSameInstant(ZoneOffset.UTC).toInstant());
      assertThat(activityEntity.getDurationInSeconds()).isEqualTo(activity.duration().getSeconds());
      assertThat(activityEntity.getActivityType()).isEqualTo(activity.activityType());
      assertThat(activityEntity.getStatus()).isEqualTo(activity.status());
    }
  }

  @Nested
  class ToDomain {
    @Test
    void testName() {
      // Given
      ActivityEntity activityEntity = Instancio.create(ActivityEntity.class);

      // When
      var result = activityEntity.toDomain();

      // Then
      assertThat(result.id()).isEqualTo(activityEntity.getId());
      assertThat(result.eventId()).isEqualTo(activityEntity.getEventId());
      assertThat(result.beginDate().withZoneSameInstant(ZoneOffset.UTC).toInstant())
          .isEqualTo(activityEntity.getBeginDate());
      assertThat(result.duration().getSeconds()).isEqualTo(activityEntity.getDurationInSeconds());
      assertThat(result.activityType()).isEqualTo(activityEntity.getActivityType());
      assertThat(result.status()).isEqualTo(activityEntity.getStatus());
    }
  }
}
