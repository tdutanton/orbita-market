package ordersService.kafka.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ordersService.kafka.outbox.OrderOutbox;
import ordersService.kafka.repository.OrderOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
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

  @Scheduled(fixedDelay = 2000)
  @Transactional
  public void processOutbox() {
    List<OrderOutbox> unsent = outboxRepository.findBySentFalseOrderByCreatedAtAsc();

    for (OrderOutbox msg : unsent) {
      try {
        kafkaTemplate.send(paymentRequestedTopic, msg.getOrderId(), msg.getPayload());
        msg.setSent(true);
        outboxRepository.save(msg);
        log.debug("Kafka: опубликовано исходящее сообщение {} для заказа {}", msg.getEventId(),
            msg.getOrderId());
      } catch (OptimisticLockingFailureException e) {
        log.warn(
            "Kafka: конкурентное обновление, другое подключение уже обработало сообщение {} для заказа {}",
            msg.getEventId(), msg.getOrderId());
      } catch (Exception e) {
        log.error("Kafka: ошибка при публикации исходящего сообщения {} для заказа {}",
            msg.getEventId(), msg.getOrderId(), e);
      }
    }
  }
}
