package ordersService.kafka.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ordersService.kafka.outbox.OrderOutbox;
import ordersService.kafka.repository.OrderOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxService {

  private final OrderOutboxRepository outboxRepository;
  private final OutboxMessageProcessor outboxMessageProcessor;

  @Value("${spring.kafka.topics.payment-requested}")
  private String paymentRequestedTopic;

  @Scheduled(fixedDelayString = "${spring.kafka.outbox-delay-msec}")
  public void processOutbox() {
    //    log.info("Kafka OutboxService - вызов processOutbox по расписанию");
    List<OrderOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");
    // отправка каждого события в kafka
    for (OrderOutbox outbox : pending) {
      outboxMessageProcessor.processMessage(outbox);
    }
  }
}
