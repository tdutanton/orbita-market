package paymentsService.kafka.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import paymentsService.kafka.outbox.PaymentOutbox;
import paymentsService.kafka.repository.PaymentOutboxEventRepository;

// kafka периодически смотрит outboxRepository и обрабатывает pending
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxService {

  private final PaymentOutboxEventRepository outboxRepository;
  private final OutboxMessageProcessor outboxMessageProcessor;

  // запускается метод каждые 2 сек
  @Scheduled(fixedDelayString = "${spring.kafka.outbox-delay-msec}")
  public void processOutbox() {
    //    log.info("Kafka OutboxService - вызов processOutbox по расписанию");
    // запрос записей со статусом PENDING (отсортировано по дате создания)
    List<PaymentOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");
    // отправка каждого события в kafka через OutboxMessageProcessor
    for (PaymentOutbox outbox : pending) {
      outboxMessageProcessor.processMessage(outbox);
    }
  }
}
