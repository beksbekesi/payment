package com.bbekesi.interview.payment.messaging;

import com.bbekesi.interview.payment.entity.Outbox;
import com.bbekesi.interview.payment.messaging.event.PaymentNotificationEvent;
import com.bbekesi.interview.payment.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;


    @Scheduled(fixedDelayString = "${outbox.poll.delay:500}")
    public void publishOutboxMessages() {
        processPendingOutboxes();
    }

    @Transactional
    public void processPendingOutboxes() {
        List<Outbox> pendingOutboxes = outboxRepository.findPendingOutboxesForUpdate();

        for (Outbox outbox : pendingOutboxes) {
            log.debug("Sending payment notification event to kafka for payment:  {}", outbox.getPaymentId()
                    .toString());
            String message = createMessageFromOutbox(outbox);
            kafkaTemplate.send("payments-topic", message)
                    .thenAccept(result -> markOutboxAsCompleted(outbox.getId()));

        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markOutboxAsCompleted(UUID outboxId) {
        outboxRepository.findById(outboxId)
                .ifPresent(outbox -> {
                    outbox.setOutboxStatus(Outbox.OutboxStatus.COMPLETED);
                    outboxRepository.save(outbox);
                    log.debug("Marking outbox message {} as completed. ", outboxId);
                });
    }

    private String createMessageFromOutbox(Outbox outbox) {
        PaymentNotificationEvent event = new PaymentNotificationEvent(outbox.getPaymentId(), outbox.getPaymentStatus());
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }

}
