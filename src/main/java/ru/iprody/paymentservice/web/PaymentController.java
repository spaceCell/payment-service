package ru.iprody.paymentservice.web;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.iprody.paymentservice.application.PaymentApplicationService;
import ru.iprody.paymentservice.web.dto.PaymentRequest;
import ru.iprody.paymentservice.web.dto.PaymentResponse;
import ru.iprody.paymentservice.web.mapper.PaymentWebMapper;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentApplicationService paymentApplicationService;
    private final PaymentWebMapper paymentWebMapper;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@RequestBody PaymentRequest paymentRequest) {
        return paymentWebMapper.toPaymentResponse(
                paymentApplicationService.create(paymentWebMapper.toPaymentCommand(paymentRequest))
        );
    }

    @Override
    @GetMapping
    public List<PaymentResponse> getAll() {
        return paymentApplicationService.getAll()
                .stream()
                .map(paymentWebMapper::toPaymentResponse)
                .toList();
    }

    @Override
    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable("id") Long paymentId) {
        return paymentWebMapper.toPaymentResponse(paymentApplicationService.getById(paymentId));
    }

    @Override
    @PutMapping("/{id}")
    public PaymentResponse update(@PathVariable("id") Long paymentId, @RequestBody PaymentRequest paymentRequest) {
        return paymentWebMapper.toPaymentResponse(
                paymentApplicationService.update(paymentId, paymentWebMapper.toPaymentCommand(paymentRequest))
        );
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long paymentId) {
        paymentApplicationService.delete(paymentId);
    }
}
