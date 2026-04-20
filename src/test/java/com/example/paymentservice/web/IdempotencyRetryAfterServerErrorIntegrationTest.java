package com.example.paymentservice.web;

import com.example.paymentservice.application.PaymentApplicationService;
import com.example.paymentservice.application.dto.PaymentAmountDetails;
import com.example.paymentservice.application.dto.PaymentDetails;
import com.example.paymentservice.domain.model.IdempotencyKeyStatus;
import com.example.paymentservice.domain.model.PaymentMethod;
import com.example.paymentservice.domain.model.PaymentStatus;
import com.example.paymentservice.domain.repository.IdempotencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class IdempotencyRetryAfterServerErrorIntegrationTest {

    private static final String PAYMENTS_URL = "/api/payments";
    private static final String IDEMPOTENCY_KEY = "it-idem-retry-500";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @MockBean
    private PaymentApplicationService paymentApplicationService;

    @BeforeEach
    void setUp() {
        idempotencyRepository.deleteAll();
    }

    @Test
    void retryWithSameKey_afterServerError_createsPaymentSuccessfully() throws Exception {
        PaymentDetails successResponse = new PaymentDetails(
                42L,
                4001L,
                PaymentStatus.PENDING,
                PaymentMethod.CARD,
                new PaymentAmountDetails(BigDecimal.valueOf(1200.00), "RUB"),
                LocalDateTime.now()
        );

        given(paymentApplicationService.create(any()))
                .willThrow(new RuntimeException("Transient processing failure"))
                .willReturn(successResponse);

        String payload = """
                {
                  "orderId": 4001,
                  "status": "PENDING",
                  "method": "CARD",
                  "amount": {
                    "amount": 1200.00,
                    "currency": "RUB"
                  }
                }
                """;

        mockMvc.perform(post(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", IDEMPOTENCY_KEY)
                        .content(payload))
                .andExpect(status().isInternalServerError());

        assertThat(idempotencyRepository.findById(IDEMPOTENCY_KEY)).isEmpty();

        mockMvc.perform(post(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", IDEMPOTENCY_KEY)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.orderId").value(4001));

        assertThat(idempotencyRepository.findById(IDEMPOTENCY_KEY))
                .hasValueSatisfying(storedKey -> assertThat(storedKey.getStatus())
                        .isEqualTo(IdempotencyKeyStatus.COMPLETED));
    }
}
package com.example.paymentservice.web;

import com.example.paymentservice.application.PaymentApplicationService;
import com.example.paymentservice.application.dto.PaymentAmountDetails;
import com.example.paymentservice.application.dto.PaymentDetails;
import com.example.paymentservice.domain.model.IdempotencyKeyStatus;
import com.example.paymentservice.domain.model.PaymentMethod;
import com.example.paymentservice.domain.model.PaymentStatus;
import com.example.paymentservice.domain.repository.IdempotencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class IdempotencyRetryAfterServerErrorIntegrationTest {

    private static final String PAYMENTS_URL = "/api/payments";
    private static final String IDEMPOTENCY_KEY = "it-idem-retry-500";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @MockBean
    private PaymentApplicationService paymentApplicationService;

    @BeforeEach
    void setUp() {
        idempotencyRepository.deleteAll();
    }

    @Test
    void retryWithSameKey_afterServerError_createsPaymentSuccessfully() throws Exception {
        PaymentDetails successfulResponse = new PaymentDetails(
                42L,
                4001L,
                PaymentStatus.PENDING,
                PaymentMethod.CARD,
                new PaymentAmountDetails(BigDecimal.valueOf(1200.00), "RUB"),
                LocalDateTime.now()
        );

        given(paymentApplicationService.create(any()))
                .willThrow(new RuntimeException("Transient processing failure"))
                .willReturn(successfulResponse);

        String payload = """
                {
                  "orderId": 4001,
                  "status": "PENDING",
                  "method": "CARD",
                  "amount": {
                    "amount": 1200.00,
                    "currency": "RUB"
                  }
                }
                """;

        mockMvc.perform(post(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", IDEMPOTENCY_KEY)
                        .content(payload))
                .andExpect(status().isInternalServerError());

        // After 5xx the key must be released, otherwise retries with the same key are blocked.
        org.assertj.core.api.Assertions.assertThat(idempotencyRepository.findById(IDEMPOTENCY_KEY)).isEmpty();

        mockMvc.perform(post(PAYMENTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", IDEMPOTENCY_KEY)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.orderId").value(4001));

        org.assertj.core.api.Assertions.assertThat(idempotencyRepository.findById(IDEMPOTENCY_KEY))
                .hasValueSatisfying(storedKey ->
                        org.assertj.core.api.Assertions.assertThat(storedKey.getStatus())
                                .isEqualTo(IdempotencyKeyStatus.COMPLETED));
    }
}
