package com.bbekesi.interview.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "idempotency_entries")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class IdempotencyEntry {

    @Id
    private String id;

    @CreationTimestamp
    private Instant createdAt;
}
