package com.bbekesi.interview.payment.repository;

import com.bbekesi.interview.payment.entity.IdempotencyEntry;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IdempotencyRepository extends JpaRepository<IdempotencyEntry, String> {
}
