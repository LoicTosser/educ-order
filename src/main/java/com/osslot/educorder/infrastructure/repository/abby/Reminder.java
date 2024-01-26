package com.osslot.educorder.infrastructure.repository.abby;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
