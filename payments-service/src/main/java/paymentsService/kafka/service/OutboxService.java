package paymentsService.kafka.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import paymentsService.kafka.outbox.PaymentOutbox;
import paymentsService.kafka.repository.PaymentOutboxEventRepository;

// kafka периодически смотрит outboxRepository и обрабатывает pending
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxService {

  private final PaymentOutboxEventRepository outboxRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;

  // запускается метод каждые 2 сек
  @Scheduled(fixedDelayString = "${spring.kafka.outbox-delay-msec}")
  @Transactional
  public void processOutbox() {
    log.info("Kafka OutboxService - вызов processOutbox по расписанию");
    // запрос записей со статусом PENDING (отсортировано по дате создания)
    List<PaymentOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");
    // отправка каждого события в kafka
    for (PaymentOutbox outbox : pending) {
      try {
        kafkaTemplate.send(outbox.getTopic(), outbox.getKey(), outbox.getPayload()).get();
        // меняется статус с pending на sent
        outbox.setStatus("SENT");
        outbox.setSentAt(Instant.now());
        outboxRepository.save(outbox);
        log.info(
            "Kafka OutboxService - направлено outbox событие {} на топик {}, изменен статус на SENT",
            outbox.getId(),
            outbox.getTopic());
      } catch (Exception e) {
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        if (outbox.getRetryCount() >= 10) {
          outbox.setStatus("FAILED");
          log.error("Kafka OutboxService - id {} отказ после {} попыток, изменен статус на FAILED",
              outbox.getId(),
              outbox.getRetryCount());
        } else {
          log.warn("Kafka OutboxService - ошибка в outbox сервисе {} (попыток {}): {}",
              outbox.getId(),
              outbox.getRetryCount(), e.getMessage());
        }
        outboxRepository.save(outbox);
        log.info("Kafka OutboxService - в catch Exception направлено outbox событие {} на топик {}",
            outbox.getId(),
            outbox.getTopic());
      }
    }
  }
}
