package paymentsService.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import paymentsService.domain.entity.account.Account;
import paymentsService.exceptions.dto.ErrorCode;
import paymentsService.kafka.event.OrderPaymentRequestedEvent;
import paymentsService.kafka.publisher.PaymentEventPublisher;
import paymentsService.kafka.repository.PaymentInboxEventRepository;
import paymentsService.repository.AccountsRepository;

// обработка запросов на оплату (асинхронное взаимодействие)
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

  private final PaymentInboxEventRepository inboxRepository;
  private final AccountsRepository accountsRepository;
  private final PaymentEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "${PAYMENT_REQUESTED_TOPIC}", concurrency = "1")
  public void consume(String message, Acknowledgment ack) {
    try {
      OrderPaymentRequestedEvent event = objectMapper.readValue(message,
          OrderPaymentRequestedEvent.class);
      log.info(
          "Kafka: поступило сообщение OrderPaymentRequested (запрос платежа): id заказа = {}, id пользователя = {}, стоимость = {}",
          event.orderId(), event.userId(), event.amount());

      int inserted = inboxRepository.tryInsert(
          event.eventId(), event.orderId(), event.userId(), event.amount());

      if (inserted == 0) {
        log.info("Kafka: дублирование сообщения {}, пропуск", event.eventId());
        ack.acknowledge();
        return;
      }

      processPayment(event);
      ack.acknowledge();
    } catch (Exception e) {
      log.error("Kafka: ошибка обработки сообщения OrderPaymentRequested (запрос платежа)", e);
    }
  }

  @Transactional
  protected void processPayment(OrderPaymentRequestedEvent event) {
    try {
      Account account = accountsRepository.findByUserId(event.userId())
          .orElse(null);

      if (account == null) {
        failPayment(event, ErrorCode.ACCOUNT_NOT_FOUND.name());
        return;
      }

      if (account.getBalance().compareTo(event.amount()) < 0) {
        failPayment(event, "LOW_BALANCE");
        return;
      }
      BigDecimal newBalance = account.getBalance().subtract(event.amount());
      account.setBalance(newBalance);
      accountsRepository.save(account);

      markInboxProcessed(event.eventId(), "COMPLETED", null);
      log.info("Kafka: успешно списано {} геокредитов у пользователя {} для заказа {}",
          event.amount(), event.userId(), event.orderId());
    } catch (Exception e) {
      log.error("Kafka: ошибка обработки платежа для заказа {}", event.orderId(), e);
      failPayment(event, ErrorCode.INTERNAL_ERROR.name());
      return;
    }

    eventPublisher.publishCompleted(event.orderId(), event.userId());
  }

  private void failPayment(OrderPaymentRequestedEvent event, String reason) {
    markInboxProcessed(event.eventId(), "FAILED", reason);
    eventPublisher.publishFailed(event.orderId(), event.userId(), reason);
    log.warn("Kafka: ошибка платежа для заказа {}: {}", event.orderId(), reason);
  }

  private void markInboxProcessed(String eventId, String status, String failureReason) {
    var inbox = inboxRepository.findById(eventId).orElse(null);
    if (inbox != null) {
      inbox.setStatus(status);
      inbox.setFailureReason(failureReason);
      inbox.setProcessedAt(Instant.now());
      inboxRepository.save(inbox);
    }
  }
}
