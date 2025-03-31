package com.bbekesi.interview.payment.repository;

import com.bbekesi.interview.payment.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
