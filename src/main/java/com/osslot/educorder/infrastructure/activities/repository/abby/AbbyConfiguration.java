package com.osslot.educorder.infrastructure.activities.repository.abby;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class AbbyConfiguration {

  @Bean
  public RestClient abbyRestClient() {
    Jackson2ObjectMapperBuilder builder =
        new Jackson2ObjectMapperBuilder()
            .indentOutput(true)
            .serializationInclusion(JsonInclude.Include.NON_EMPTY);
    return RestClient.builder()
        .messageConverters(
            list -> {
              list.clear();
              list.add(new MappingJackson2HttpMessageConverter(builder.build()));
            })
        .build();
  }
}
