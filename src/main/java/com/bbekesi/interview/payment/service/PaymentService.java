package com.bbekesi.interview.payment.service;

import com.bbekesi.interview.payment.dto.PaymentRequest;
import com.bbekesi.interview.payment.dto.PaymentResponse;
import com.bbekesi.interview.payment.entity.Account;
import com.bbekesi.interview.payment.entity.Outbox;
import com.bbekesi.interview.payment.entity.Payment;
import com.bbekesi.interview.payment.exception.AccountNotFoundException;
import com.bbekesi.interview.payment.exception.InvalidUUIDException;
import com.bbekesi.interview.payment.repository.AccountRepository;
import com.bbekesi.interview.payment.repository.OutboxRepository;
import com.bbekesi.interview.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final OutboxRepository outboxRepository;
    private final PaymentRepository paymentRepository;

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Account fromAccount = accountRepository.findById(getUUIDFromString(request.fromAccount()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found for fromAccount."));
        Account toAccount = accountRepository.findById(getUUIDFromString(request.toAccount()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found for toAccount."));

        if (balanceInsufficient(request, fromAccount)) {
            Payment rejectedPayment = savePaymentWithStatus(request, Payment.PaymentStatus.REJECTED);
            saveOutboxMessageWithStatus(rejectedPayment);

            return mapPaymentResponse(rejectedPayment);
        }

        accountRepository.save(fromAccount.deduct(request.amount()));
        accountRepository.save(toAccount.add(request.amount()));

        Payment savedPayment = savePaymentWithStatus(request, Payment.PaymentStatus.SUCCESS);
        saveOutboxMessageWithStatus(savedPayment);

        return mapPaymentResponse(savedPayment);
    }

    @Recover
    @Transactional
    public PaymentResponse handlePaymentFailure(OptimisticLockingFailureException e, PaymentRequest request) {
        log.error("Payment processing failed after retries for request: {}", request, e);

        Payment failedPayment = savePaymentWithStatus(request, Payment.PaymentStatus.FAILED);
        saveOutboxMessageWithStatus(failedPayment);

        return mapPaymentResponse(failedPayment);
    }

    private UUID getUUIDFromString(String stringId) {
        try {
            return UUID.fromString(stringId);
        } catch (IllegalArgumentException ex) {
            log.error("Provided id is not a valid uuid : {}", stringId);
            throw new InvalidUUIDException("Provided id is not a valid uuid " + stringId);
        }
    }

    private boolean balanceInsufficient(PaymentRequest request, Account fromAccount) {
        return fromAccount.getBalance()
                .compareTo(request.amount()) < 0;
    }

    private Payment savePaymentWithStatus(PaymentRequest request, Payment.PaymentStatus paymentStatus) {
        log.debug("Saving payment : {}", request);
        return paymentRepository.save(Payment.builder()
                .amount(request.amount())
                .fromAccountId(getUUIDFromString(request.fromAccount()))
                .toAccountId(getUUIDFromString(request.toAccount()))
                .status(paymentStatus)
                .build());
    }

    private void saveOutboxMessageWithStatus(Payment payment) {
        log.debug("Saving outboxMessage for payment : {}", payment);
        outboxRepository.save(Outbox.builder()
                .paymentId(payment.getId())
                .outboxStatus(Outbox.OutboxStatus.PENDING)
                .paymentStatus(payment.getStatus())
                .build());
    }

    private PaymentResponse mapPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId()
                        .toString(),
                payment.getStatus()
                        .name());
    }
}
