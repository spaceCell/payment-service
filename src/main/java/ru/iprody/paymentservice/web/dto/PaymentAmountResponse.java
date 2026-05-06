package ru.iprody.paymentservice.web.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Сумма платежа в ответе")
public class PaymentAmountResponse {

    private BigDecimal amount;
    private String currency;
}
