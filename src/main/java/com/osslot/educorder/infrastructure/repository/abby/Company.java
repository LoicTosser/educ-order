package com.osslot.educorder.infrastructure.repository.abby;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class Company {
    private String id;
    private String commercialName;
    private String siret;
    private String name;

    // getters and setters
}
