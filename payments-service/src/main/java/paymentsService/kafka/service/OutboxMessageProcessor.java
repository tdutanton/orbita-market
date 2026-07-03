package paymentsService.kafka.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import paymentsService.kafka.outbox.PaymentOutbox;
import paymentsService.kafka.repository.PaymentOutboxEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxMessageProcessor {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final PaymentOutboxEventRepository outboxRepository;

  @Value("${spring.kafka.max-retry-count}")
  private int maxRetryCount;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void processMessage(PaymentOutbox outbox) {
    try {
      kafkaTemplate.send(outbox.getTopic(), outbox.getKey(), outbox.getPayload()).get();
      // меняется статус с pending на sent
      outbox.setStatus("SENT");
      outbox.setSentAt(Instant.now());
      outboxRepository.save(outbox);
      log.info(
          "OutboxMessageProcessor - направлено outbox событие {} на топик {}, изменен статус на SENT",
          outbox.getId(),
          outbox.getTopic());
    } catch (Exception e) {
      outbox.setRetryCount(outbox.getRetryCount() + 1);
      if (outbox.getRetryCount() >= maxRetryCount) {
        outbox.setStatus("FAILED");
        log.error("OutboxMessageProcessor - id {} отказ после {} попыток, изменен статус на FAILED",
            outbox.getId(),
            outbox.getRetryCount());
      } else {
        log.warn("OutboxMessageProcessor - ошибка {} (попытка {}): {}",
            outbox.getId(), outbox.getRetryCount(), e.getMessage());
      }
      outboxRepository.save(outbox);
      log.info(
          "OutboxMessageProcessor - в catch Exception направлено outbox событие {} на топик {}",
          outbox.getId(),
          outbox.getTopic());
    }
  }
}
