package com.bbekesi.interview.payment.entity;

import com.bbekesi.interview.payment.entity.Payment.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Outbox {

    @Id
    @UuidGenerator
    private UUID id;

    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;

    public enum OutboxStatus {
        PENDING, COMPLETED
    }

}
