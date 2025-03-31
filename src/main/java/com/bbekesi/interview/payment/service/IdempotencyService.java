package com.bbekesi.interview.payment.service;

import com.bbekesi.interview.payment.entity.IdempotencyEntry;
import com.bbekesi.interview.payment.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    @Transactional
    public boolean idempotencyCheck(String idempotencyId) {
        try {
            Optional<IdempotencyEntry> idempotencyEntryOptional = idempotencyRepository.findById(idempotencyId);
            if (idempotencyEntryOptional.isEmpty()) {
                IdempotencyEntry newIdempotencyEntry = IdempotencyEntry.builder()
                        .id(idempotencyId)
                        .build();

                idempotencyRepository.save(newIdempotencyEntry);
                return true;
            }
            return false;
        } catch (DataIntegrityViolationException e) {
            log.debug("Idempotency check failed, another service probably picked up the same request : ", e);
            return false;
        }
    }

}
