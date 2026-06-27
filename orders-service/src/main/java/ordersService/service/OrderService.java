package ordersService.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ordersService.domain.entity.order.Order;
import ordersService.domain.response.OrderResponse;
import ordersService.exceptions.order.OrderNotFoundException;
import ordersService.kafka.event.OrderPaymentRequestedEvent;
import ordersService.kafka.outbox.OrderOutbox;
import ordersService.kafka.repository.OrderOutboxRepository;
import ordersService.repository.OrdersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrdersRepository ordersRepository;
  private final OrderOutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;
  private final PriceCalculation priceCalculation;


  @Transactional
  public Order createOrder(String userId, String orderIdFromClient, String productType,
      JsonNode payload) {
    String orderId = (orderIdFromClient != null && !orderIdFromClient.isBlank())
        ? orderIdFromClient
        : UUID.randomUUID().toString();
    Order order = new Order();
    order.setId(orderId);
    order.setUserId(userId);
    order.setPayload(payload);
    order.setStatus("CREATED");
    order.setProductType(productType);

    BigDecimal price = priceCalculation.calculatePrice(productType, payload.get("aoi"));
    order.setPrice(price);
    order.setCreatedAt(Instant.now());

    ordersRepository.save(order);
    log.info("В OrderService создан заказ {}", orderId);
    log.info("Заказу {} присвоен статус CREATED", orderId);

    String eventId = UUID.randomUUID().toString();
    var event = new OrderPaymentRequestedEvent(eventId, orderId, userId, price, Instant.now());

    try {
      String eventJson = objectMapper.writeValueAsString(event);
      OrderOutbox outbox = new OrderOutbox(eventId, orderId, "ORDER_PAYMENT_REQUESTED", eventJson);
      outboxRepository.save(outbox);
      log.info("В outboxRepository отправлен outbox event для заказа {}", orderId);
    } catch (Exception e) {
      log.error("Ошибка при сериализации outbox event для заказа {}", orderId, e);
    }

    order.setStatus("PAYMENT_PENDING");
    log.info("Заказу {} присвоен статус PAYMENT_PENDING", orderId);
    ordersRepository.save(order);
    log.info("ordersRepository обновил заказ {} со статусом PAYMENT_PENDING", orderId);
    return order;
  }

  public List<OrderResponse> getOrdersByUser(String userId) {
    return ordersRepository.findByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(OrderResponse::from)
        .toList();
  }

  public OrderResponse getOrderById(String orderId, String userId) {
    Order order = ordersRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Заказ не найден по Id: " + orderId));
    if (!order.getUserId().equals(userId)) {
      throw new OrderNotFoundException("Заказ не найден по Id: " + orderId);
    }
    return OrderResponse.from(order);
  }

  @Transactional
  public void completePayment(String orderId) {
    Order order = ordersRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Заказ не найден по Id: " + orderId));
    order.setStatus("PAID");
    ordersRepository.save(order);
    log.info("Заказ {} оплачен, присвоен статус PAID", orderId);
  }

  @Transactional
  public void failPayment(String orderId, String failureReason) {
    Order order = ordersRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Заказ не найден по Id: " + orderId));
    order.setStatus("PAYMENT_FAILED");
    order.setFailureReason(failureReason);
    ordersRepository.save(order);
    log.warn("У заказа {} ошибка оплаты, присвоен статус PAYMENT_FAILED: {}", orderId,
        failureReason);
  }

  @Transactional
  public void rejectOrder(String orderId, String failureReason) {
    Order order = ordersRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Заказ не найден по Id: " + orderId));
    order.setStatus("REJECTED");
    order.setFailureReason(failureReason);
    ordersRepository.save(order);
    log.warn("Заказ {} отклонен при создании, присвоен статус REJECTED: {}", orderId,
        failureReason);
  }
}
