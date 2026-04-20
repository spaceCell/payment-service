package com.example.paymentservice.web.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.example.paymentservice.domain.model.PaymentMethod;
import com.example.paymentservice.domain.model.PaymentStatus;

@Data
@AllArgsConstructor
@Schema(description = "Ответ с данными платежа")
public class PaymentResponse {

    @Schema(description = "Идентификатор платежа", example = "1")
    private Long id;
    @Schema(description = "Идентификатор заказа", example = "1")
    private Long orderId;
    @Schema(description = "Статус платежа", example = "CAPTURED")
    private PaymentStatus status;
    @Schema(description = "Способ оплаты", example = "CARD")
    private PaymentMethod method;
    @Schema(description = "Сумма платежа")
    private PaymentAmountResponse amount;
    @Schema(description = "Дата и время создания платежа")
    private LocalDateTime createdAt;
}
