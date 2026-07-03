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

  @KafkaListener(topics = "${spring.kafka.topics.payment-requested}", concurrency = "1")
  @Transactional
  public void consume(String message, Acknowledgment ack) {
    log.info("KafkaConsumer - вызов consume");
    try {
      OrderPaymentRequestedEvent event = objectMapper.readValue(message,
          OrderPaymentRequestedEvent.class);
      log.info(
          "KafkaConsumer - поступило сообщение OrderPaymentRequested (запрос платежа): id заказа = {}, id пользователя = {}, стоимость = {}",
          event.orderId(), event.userId(), event.amount());

      int inserted = inboxRepository.tryInsert(
          event.eventId(), event.orderId(), event.userId(), event.amount());

      if (inserted == 0) {
        log.warn("KafkaConsumer - дублирование сообщения {}, пропуск", event.eventId());
        ack.acknowledge();
        return;
      }

      processPayment(event);
      ack.acknowledge();
    } catch (Exception e) {
      log.error("KafkaConsumer - ошибка обработки сообщения OrderPaymentRequested (запрос платежа)",
          e);
    }
  }

  protected void processPayment(OrderPaymentRequestedEvent event) {
    log.info("KafkaConsumer - вызов processPayment");
    try {
      Account account = accountsRepository.findByUserId(event.userId())
          .orElse(null);

      if (account == null) {
        log.info("KafkaConsumer - вызов failPayment внутри processPayment - ACCOUNT_NOT_FOUND");
        failPayment(event, ErrorCode.ACCOUNT_NOT_FOUND.name());
        return;
      }

      if (account.getBalance().compareTo(event.amount()) < 0) {
        log.info("KafkaConsumer - вызов failPayment внутри processPayment - LOW_BALANCE");
        failPayment(event, "LOW_BALANCE");
        return;
      }
      BigDecimal newBalance = account.getBalance().subtract(event.amount());
      account.setBalance(newBalance);
      accountsRepository.save(account);

      markInboxProcessed(event.eventId(), "COMPLETED", null);
      log.info(
          "KafkaConsumer - processPayment - успешно списано {} геокредитов у пользователя {} для заказа {} update accountsRepository - обновлен balance",
          event.amount(), event.userId(), event.orderId());
      log.info(
          "KafkaConsumer - вызов eventPublisher.publishCompleted");
      eventPublisher.publishCompleted(event.orderId(), event.userId(), event.amount(), newBalance);
    } catch (Exception e) {
      log.error("KafkaConsumer - ошибка обработки платежа для заказа {}", event.orderId(), e);
      failPayment(event, ErrorCode.INTERNAL_ERROR.name());
    }
  }

  private void failPayment(OrderPaymentRequestedEvent event, String reason) {
    log.info("KafkaConsumer - вызов failPayment для заказа {}: причина фейла {}", event.orderId(),
        reason);
    markInboxProcessed(event.eventId(), "FAILED", reason);
    log.info("KafkaConsumer - вызов eventPublisher.publishFailed");
    eventPublisher.publishFailed(event.orderId(), event.userId(), reason);
  }

  private void markInboxProcessed(String eventId, String status, String failureReason) {
    log.info("KafkaConsumer - вызов markInboxProcessed");
    var inbox = inboxRepository.findById(eventId).orElse(null);
    if (inbox != null) {
      log.info(
          "KafkaConsumer - внутри markInboxProcessed в inboxRepository обновлены данные по заказу {}: статус {} failureReason {}",
          inbox.getOrderId(), status, failureReason);
      inbox.setStatus(status);
      inbox.setFailureReason(failureReason);
      inbox.setProcessedAt(Instant.now());
      inboxRepository.save(inbox);
    }
  }
}
