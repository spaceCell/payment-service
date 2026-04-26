package ru.iprody.paymentservice;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.iprody.paymentservice.domain.model.PaymentMethod;
import ru.iprody.paymentservice.domain.model.PaymentStatus;
import ru.iprody.paymentservice.domain.repository.PaymentRepository;
import ru.iprody.paymentservice.web.dto.PaymentAmountRequest;
import ru.iprody.paymentservice.web.dto.PaymentRequest;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void shouldPerformCrudForPayment() throws Exception {
        PaymentRequest createRequest = new PaymentRequest();
        createRequest.setOrderId(1L);
        createRequest.setStatus(PaymentStatus.PENDING);
        createRequest.setMethod(PaymentMethod.CARD);
        createRequest.setAmount(new PaymentAmountRequest(new BigDecimal("83970.00"), "RUB"));

        MvcResult createResult = mockMvc.perform(post("/api/payments")
                        .header("X-Idempotency-Key", "test-create-payment-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount.amount").value(83970.00))
                .andReturn();

        JsonNode createdPayment = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long paymentId = createdPayment.get("id").asLong();

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(paymentId));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.method").value("CARD"));

        PaymentRequest updateRequest = new PaymentRequest();
        updateRequest.setOrderId(1L);
        updateRequest.setStatus(PaymentStatus.CAPTURED);
        updateRequest.setMethod(PaymentMethod.BANK_TRANSFER);
        updateRequest.setAmount(new PaymentAmountRequest(new BigDecimal("83970.00"), "RUB"));

        mockMvc.perform(put("/api/payments/{id}", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.method").value("BANK_TRANSFER"));

        mockMvc.perform(delete("/api/payments/{id}", paymentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isNotFound());
    }
}
