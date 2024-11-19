package com.osslot.educorder.infrastructure.activities.repository.abby;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
@Setter
public class Reminder {

  private Boolean idCopy;
  private Integer frequency;
  private Integer numberOfRemindersToSend;
  private Boolean active;
  private List<String> recipients;
}
