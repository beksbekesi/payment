package com.bbekesi.interview.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Account {

    @Id
    @UuidGenerator
    private UUID id;

    private BigDecimal balance;

    @Version
    private Long version;

    public Account deduct(BigDecimal amount){
        balance = balance.subtract(amount);
        return this;
    }

    public Account add(BigDecimal amount){
        balance = balance.add(amount);
        return this;
    }
}
