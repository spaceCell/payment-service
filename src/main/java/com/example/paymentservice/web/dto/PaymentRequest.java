package com.example.paymentservice.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.paymentservice.domain.model.PaymentMethod;
import com.example.paymentservice.domain.model.PaymentStatus;

@Data
@NoArgsConstructor
@Schema(description = "Запрос на создание или обновление платежа")
public class PaymentRequest {

    @Schema(description = "Идентификатор заказа", example = "1")
    private Long orderId;
    @Schema(description = "Статус платежа", example = "PENDING")
    private PaymentStatus status;
    @Schema(description = "Способ оплаты", example = "CARD")
    private PaymentMethod method;
    @Schema(description = "Сумма платежа")
    private PaymentAmountRequest amount;
}
