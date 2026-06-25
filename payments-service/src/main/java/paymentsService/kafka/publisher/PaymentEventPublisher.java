package paymentsService.kafka.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import paymentsService.kafka.event.OrderPaymentCompletedEvent;
import paymentsService.kafka.event.OrderPaymentFailedEvent;
import paymentsService.kafka.outbox.PaymentOutbox;
import paymentsService.kafka.repository.PaymentOutboxEventRepository;

// публикация сообщений об оплате (асинхронное взаимодействие)
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

  private final PaymentOutboxEventRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @Value("${spring.kafka.topics.payment-completed}")
  private String paymentCompletedTopic;

  @Value("${spring.kafka.topics.payment-failed}")
  private String paymentFailedTopic;

  public void publishCompleted(String orderId, String userId, BigDecimal amount,
      BigDecimal newBalance) {
    try {
      var event = new OrderPaymentCompletedEvent(
          UUID.randomUUID().toString(),
          orderId,
          userId,
          amount,
          newBalance,
          Instant.now()
      );
      String json = objectMapper.writeValueAsString(event);
      var outbox = new PaymentOutbox(
          UUID.randomUUID().toString(),
          paymentCompletedTopic,
          orderId,
          json
      );
      outboxRepository.save(outbox);
      log.info(
          "Outbox event: опубликовано outbox сообщение OrderPaymentCompleted (выполнен платеж) для заказа {}",
          orderId);
    } catch (Exception e) {
      log.error(
          "Outbox event: ошибка при публикации outbox сообщения OrderPaymentCompleted (выполнен платеж) для заказа {}",
          orderId, e);
    }
  }

  public void publishFailed(String orderId, String userId, String failureReason) {
    try {
      var event = new OrderPaymentFailedEvent(
          UUID.randomUUID().toString(),
          orderId,
          userId,
          failureReason,
          Instant.now()
      );
      String json = objectMapper.writeValueAsString(event);
      var outbox = new PaymentOutbox(
          UUID.randomUUID().toString(),
          paymentFailedTopic,
          orderId,
          json
      );
      outboxRepository.save(outbox);
      log.info(
          "Outbox event: опубликовано outbox сообщение OrderPaymentFailed (отказ платежа) для заказа {}: {}",
          orderId,
          failureReason);
    } catch (Exception e) {
      log.error(
          "Outbox event: ошибка при публикации outbox сообщения (отказ платежа) для заказа {}",
          orderId, e);
    }
  }
}
