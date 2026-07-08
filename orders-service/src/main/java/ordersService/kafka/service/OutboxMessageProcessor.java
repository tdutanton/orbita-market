package ordersService.kafka.service;


import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ordersService.kafka.outbox.OrderOutbox;
import ordersService.kafka.repository.OrderOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxMessageProcessor {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final OrderOutboxRepository outboxRepository;

  @Value("${spring.kafka.topics.payment-requested}")
  private String paymentRequestedTopic;

  @Value("${spring.kafka.max-retry-count}")
  private int maxRetryCount;

  @Transactional
  public void processMessage(OrderOutbox outbox) {
    try {
      kafkaTemplate.send(paymentRequestedTopic, outbox.getOrderId(), outbox.getPayload()).get();
      // меняется статус с pending на sent
      outbox.setStatus("SENT");
      outbox.setSentAt(Instant.now());
      outboxRepository.save(outbox);
      log.info(
          "OutboxMessageProcessor - направлено outbox событие {} для заказа {}, изменен статус на SENT",
          outbox.getEventId(),
          outbox.getOrderId());
    } catch (Exception e) {
      outbox.setRetryCount(outbox.getRetryCount() + 1);
      if (outbox.getRetryCount() >= maxRetryCount) {
        outbox.setStatus("FAILED");
        log.error("OutboxMessageProcessor - id {} отказ после {} попыток, изменен статус на FAILED",
            outbox.getId(),
            outbox.getRetryCount());
      } else {
        log.warn("OutboxMessageProcessor - ошибка в outbox сервисе {} (попыток {}): {}",
            outbox.getId(),
            outbox.getRetryCount(), e.getMessage());
      }
      outboxRepository.save(outbox);
      log.info(
          "OutboxMessageProcessor - в catch Exception направлено outbox событие {} на топик {}",
          outbox.getId(),
          paymentRequestedTopic);
    }
  }
}