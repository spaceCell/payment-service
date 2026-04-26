package ru.iprody.paymentservice.web.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сумма платежа в запросе")
public class PaymentAmountRequest {

    private BigDecimal amount;
    private String currency;
}
