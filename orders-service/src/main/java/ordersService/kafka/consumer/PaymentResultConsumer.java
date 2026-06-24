package ordersService.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ordersService.kafka.event.OrderPaymentCompletedEvent;
import ordersService.kafka.event.OrderPaymentFailedEvent;
import ordersService.kafka.repository.OrderInboxEventRepository;
import ordersService.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

  private final OrderInboxEventRepository inboxRepository;
  private final OrderService orderService;
  private final ObjectMapper objectMapper;


  @KafkaListener(topics = "${spring.kafka.topics.payment-completed}", concurrency = "1")
  public void consumeCompleted(String message, Acknowledgment ack) {
    try {
      OrderPaymentCompletedEvent event = objectMapper.readValue(message,
          OrderPaymentCompletedEvent.class);
      log.info("Kafka: поступило сообщение OrderPaymentCompleted: id заказа = {}", event.orderId());

      int inserted = inboxRepository.tryInsert(
          event.eventId(), event.orderId(), "ORDER_PAYMENT_COMPLETED");

      if (inserted == 0) {
        log.info("Kafka: дублирование сообщения {}, пропуск", event.eventId());
        ack.acknowledge();
        return;
      }

      orderService.completePayment(event.orderId());
      ack.acknowledge();
    } catch (Exception e) {
      log.error("Kafka: ошибка обработки сообщения OrderPaymentCompleted", e);
    }
  }

  @KafkaListener(topics = "${spring.kafka.topics.payment-failed}", concurrency = "1")
  public void consumeFailed(String message, Acknowledgment ack) {
    try {
      OrderPaymentFailedEvent event = objectMapper.readValue(message,
          OrderPaymentFailedEvent.class);
      log.info("Kafka: поступило сообщение OrderPaymentFailed: id заказа = {}, причина = {}",
          event.orderId(),
          event.failureReason());

      int inserted = inboxRepository.tryInsert(
          event.eventId(), event.orderId(), "ORDER_PAYMENT_FAILED");

      if (inserted == 0) {
        log.info("Kafka: дублирование сообщения {}, пропуск", event.eventId());
        ack.acknowledge();
        return;
      }

      orderService.failPayment(event.orderId(), event.failureReason());
      ack.acknowledge();
    } catch (Exception e) {
      log.error("Kafka: ошибка обработки платежа для заказа заказа", e);
    }
  }
}
