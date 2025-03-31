package com.bbekesi.interview.payment.controller;

import com.bbekesi.interview.payment.dto.PaymentRequest;
import com.bbekesi.interview.payment.dto.PaymentResponse;
import com.bbekesi.interview.payment.entity.Payment;
import com.bbekesi.interview.payment.service.IdempotencyService;
import com.bbekesi.interview.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "API for processing payments")
public class PaymentController {

    private final PaymentService paymentService;

    private final IdempotencyService idempotencyService;
    @Operation(
            summary = "Create a payment",
            description = "Processes a payment request. Returns 200 OK for success, 422 for insufficient balance, and 500 for server errors."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "422", description = "Payment rejected due to insufficient balance"),
            @ApiResponse(responseCode = "500", description = "Payment processing failed due to internal error"),
            @ApiResponse(responseCode = "409", description = "Conflict due to duplicate idempotency key")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        if (!idempotencyService.idempotencyCheck(idempotencyKey)) {
            return ResponseEntity.status(409)
                    .build();
        }

        PaymentResponse response = paymentService.processPayment(request);

        if (Payment.PaymentStatus.REJECTED.name().equals(response.status())) {
            return ResponseEntity.status(422)
                    .body(response);
        } else if (Payment.PaymentStatus.FAILED.name().equals(response.status())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }

        return ResponseEntity.ok()
                .body(response);
    }

}
