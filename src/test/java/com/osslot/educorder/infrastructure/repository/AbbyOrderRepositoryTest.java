package com.osslot.educorder.infrastructure.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osslot.educorder.infrastructure.repository.abby.Billing;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Paths;

@SpringBootTest
class AbbyOrderRepositoryTest implements WithAssertions {

    @Autowired
    private AbbyOrderRepository abbyOrderRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getProjectsCategories() {
        var result = abbyOrderRepository.getOrders();
        assertThat(result).isNotNull();
    }

    @Test
    void getOrder_whenKnownOrderId_returnsOrder() {
        var result = abbyOrderRepository.getOrder("6581f9d1502efc89dd7c29a4");
        assertThat(result).isNotNull();
    }

    @Test
    void createOrder_whenValidOrder_returnsOrder() throws IOException {
        // Given
        var biilingPath = Paths.get("src","test","resources", "abby", "billing-to-create-2.json");

        var billing = objectMapper.readValue(biilingPath.toFile(), Billing.class);
        var serialized = objectMapper.writeValueAsString(billing);

        var result = abbyOrderRepository.createOrder(billing);
        assertThat(result).isNotNull();
    }

}