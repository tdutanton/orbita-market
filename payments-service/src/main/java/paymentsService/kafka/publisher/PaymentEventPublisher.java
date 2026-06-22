package paymentsService.kafka.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import paymentsService.kafka.event.OrderPaymentCompletedEvent;
import paymentsService.kafka.event.OrderPaymentFailedEvent;

// публикация сообщений об оплате (асинхронное взаимодействие)
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void publishCompleted(String orderId, String userId) {
    try {
      var event = new OrderPaymentCompletedEvent(
          UUID.randomUUID().toString(),
          orderId,
          userId,
          Instant.now()
      );
      String json = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("${PAYMENT_COMPLETED_TOPIC}", orderId, json);
      log.info(
          "Kafka: опубликовано сообщение OrderPaymentCompleted (выполнен платеж) для заказа {}",
          orderId);
    } catch (Exception e) {
      log.error(
          "Kafka: ошибка при публикации сообщения OrderPaymentCompleted (выполнен платеж) для заказа {}",
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
      kafkaTemplate.send("${PAYMENT_FAILED_TOPIC}", orderId, json);
      log.info("Kafka: опубликовано сообщение OrderPaymentFailed (отказ платежа) для заказа {}: {}",
          orderId,
          failureReason);
    } catch (Exception e) {
      log.error("Kafka: ошибка при публикации сообщения (отказ платежа) для заказа {}", orderId, e);
    }
  }
}
