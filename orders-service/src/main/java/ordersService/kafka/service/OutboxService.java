package ordersService.kafka.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ordersService.kafka.outbox.OrderOutbox;
import ordersService.kafka.repository.OrderOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxService {

  private final OrderOutboxRepository outboxRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;

  @Value("${spring.kafka.topics.payment-requested}")
  private String paymentRequestedTopic;

  @Scheduled(fixedDelayString = "${spring.kafka.outbox-delay-msec}")
  @Transactional
  public void processOutbox() {
    log.info("Kafka OutboxService - вызов processOutbox по расписанию");
    List<OrderOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");
    // отправка каждого события в kafka
    for (OrderOutbox outbox : pending) {
      try {
        kafkaTemplate.send(paymentRequestedTopic, outbox.getOrderId(), outbox.getPayload()).get();
        // меняется статус с pending на sent
        outbox.setStatus("SENT");
        outbox.setSentAt(Instant.now());
        outboxRepository.save(outbox);
        log.info(
            "Kafka OutboxService - направлено outbox событие {} для заказа {}, изменен статус на SENT",
            outbox.getEventId(),
            outbox.getOrderId());
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
            paymentRequestedTopic);
      }
    }
  }
}
