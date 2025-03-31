package com.bbekesi.interview.payment.repository;

import com.bbekesi.interview.payment.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

    @Query(value = "SELECT * FROM outbox WHERE outbox_status = 'PENDING' FOR UPDATE SKIP LOCKED LIMIT 50", nativeQuery = true)
    List<Outbox> findPendingOutboxesForUpdate();
}
