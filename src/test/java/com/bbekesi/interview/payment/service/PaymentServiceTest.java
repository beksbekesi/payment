package com.bbekesi.interview.payment.service;

import com.bbekesi.interview.payment.dto.PaymentRequest;
import com.bbekesi.interview.payment.dto.PaymentResponse;
import com.bbekesi.interview.payment.entity.Account;
import com.bbekesi.interview.payment.entity.Outbox;
import com.bbekesi.interview.payment.entity.Payment;
import com.bbekesi.interview.payment.repository.AccountRepository;
import com.bbekesi.interview.payment.repository.OutboxRepository;
import com.bbekesi.interview.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest successRequest;
    private PaymentRequest insufficientFundsRequest;
    private final UUID fromAccountId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID toAccountId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setup() {
        successRequest = new PaymentRequest(fromAccountId.toString(), toAccountId.toString(), BigDecimal.valueOf(100));

        insufficientFundsRequest = new PaymentRequest(fromAccountId.toString(), toAccountId.toString(), BigDecimal.valueOf(500));
    }

    @Test
    void processPayment_successful() {
        Account fromAccount = Account.builder()
                .id(fromAccountId)
                .balance(BigDecimal.valueOf(200))
                .build();
        Account toAccount = Account.builder()
                .id(toAccountId)
                .balance(BigDecimal.valueOf(50))
                .build();

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        Payment savedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .amount(successRequest.amount())
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .status(Payment.PaymentStatus.SUCCESS)
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(outboxRepository.save(any(Outbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(successRequest);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(Payment.PaymentStatus.SUCCESS.name());
        verify(accountRepository, times(1)).findById(fromAccountId);
        verify(accountRepository, times(1)).findById(toAccountId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(outboxRepository, times(1)).save(any(Outbox.class));
    }

    @Test
    void processPayment_rejectedDueToInsufficientBalance() {
        Account fromAccount = Account.builder()
                .id(fromAccountId)
                .balance(BigDecimal.valueOf(300))
                .build();
        Account toAccount = Account.builder()
                .id(toAccountId)
                .balance(BigDecimal.valueOf(50))
                .build();

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));

        Payment rejectedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .amount(insufficientFundsRequest.amount())
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .status(Payment.PaymentStatus.REJECTED)
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(rejectedPayment);
        when(outboxRepository.save(any(Outbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(insufficientFundsRequest);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(Payment.PaymentStatus.REJECTED.name());
        verify(accountRepository, never()).save(ArgumentMatchers.any(Account.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(outboxRepository, times(1)).save(any(Outbox.class));
    }

    @Test
    void processPayment_retryExhausted() {
        Payment savedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .amount(successRequest.amount())
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .status(Payment.PaymentStatus.FAILED)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = paymentService.handlePaymentFailure(
                new OptimisticLockingFailureException("Simulated exception"),
                successRequest
        );

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(Payment.PaymentStatus.FAILED.name());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(outboxRepository, times(1)).save(any(Outbox.class));
    }
}
