package ru.iprody.paymentservice.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.iprody.paymentservice.web.dto.PaymentRequest;
import ru.iprody.paymentservice.web.dto.PaymentResponse;

@Tag(name = "Payments", description = "Операции с платежами")
public interface PaymentApi {

    @Operation(summary = "Создать платёж")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Платёж создан"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    })
    @ResponseStatus(HttpStatus.CREATED)
    PaymentResponse create(@RequestBody PaymentRequest paymentRequest);

    @Operation(summary = "Получить список платежей")
    @ApiResponse(responseCode = "200", description = "Список платежей получен")
    List<PaymentResponse> getAll();

    @Operation(summary = "Получить платёж по идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Платёж найден"),
            @ApiResponse(responseCode = "404", description = "Платёж не найден")
    })
    PaymentResponse getById(@PathVariable("id") Long paymentId);

    @Operation(summary = "Обновить платёж")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Платёж обновлён"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Платёж не найден")
    })
    PaymentResponse update(@PathVariable("id") Long paymentId, @RequestBody PaymentRequest paymentRequest);

    @Operation(summary = "Удалить платёж")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Платёж удалён"),
            @ApiResponse(responseCode = "404", description = "Платёж не найден")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable("id") Long paymentId);
}
