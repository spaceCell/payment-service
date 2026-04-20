package com.example.paymentservice.web;

import com.example.paymentservice.domain.repository.IdempotencyRepository;
import com.example.paymentservice.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class PaymentControllerIntegrationTest {

    private static final String PAYMENTS_URL = "/api/payments";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @BeforeEach
    void cleanUp() {
        paymentRepository.deleteAll();
        idempotencyRepository.deleteAll();
    }

    @Test
    void createGetUpdateDeletePayment_successFlow() throws Exception {
        String idempotencyKey = "it-success-" + UUID.randomUUID();
        String createPayload = """
                {
                  "orderId": 1001,
                  "status": "PENDING",
                  "method": "CARD",
                  "amount": {
                    "amount": 1499.99,
                    "currency": "RUB"
                  }
                }
                """;

        String updatePayload = """
                {
                  "orderId": 1001,
                  "status": "CAPTURED",
                  "method": "CARD",
                  "amount": {
                    "amount": 1499.99,
                    "currency": "RUB"
                  }
                }
                """;

        mockMvc.perform(post(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", idempotencyKey)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.orderId").value(1001))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.method").value("CARD"))
                .andExpect(jsonPath("$.amount.amount").value(1499.99))
                .andExpect(jsonPath("$.amount.currency").value("RUB"));

        mockMvc.perform(get(PAYMENTS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(1001))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        mockMvc.perform(get(PAYMENTS_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(put(PAYMENTS_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CAPTURED"));

        mockMvc.perform(delete(PAYMENTS_URL + "/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(PAYMENTS_URL + "/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createPayment_withoutIdempotencyKey_returnsBadRequest() throws Exception {
        String payload = """
                {
                  "orderId": 2001,
                  "status": "PENDING",
                  "method": "CARD",
                  "amount": {
                    "amount": 50.00,
                    "currency": "RUB"
                  }
                }
                """;

        mockMvc.perform(post(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("X-Idempotency-Key header is required"));
    }

    @Test
    void createPayment_withInvalidEnum_returnsBadRequest() throws Exception {
        String payload = """
                {
                  "orderId": 3001,
                  "status": "UNKNOWN",
                  "method": "CARD",
                  "amount": {
                    "amount": 100.00,
                    "currency": "RUB"
                  }
                }
                """;

        mockMvc.perform(post(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "it-invalid-enum-" + UUID.randomUUID())
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_withNegativeAmount_returnsBadRequest() throws Exception {
        String payload = """
                {
                  "orderId": 3002,
                  "status": "PENDING",
                  "method": "CARD",
                  "amount": {
                    "amount": -1.00,
                    "currency": "RUB"
                  }
                }
                """;

        mockMvc.perform(post(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "it-negative-amount-" + UUID.randomUUID())
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Payment amount must be greater than or equal to zero"));
    }

    @Test
    void getById_whenMissing_returnsNotFound() throws Exception {
        mockMvc.perform(get(PAYMENTS_URL + "/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Payment with id 9999 was not found"));
    }
}
