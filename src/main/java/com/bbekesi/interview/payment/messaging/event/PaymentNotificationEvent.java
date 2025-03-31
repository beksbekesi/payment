package com.bbekesi.interview.payment.messaging.event;

import com.bbekesi.interview.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentNotificationEvent {

    private UUID paymentId;
    private Payment.PaymentStatus paymentStatus;
}
