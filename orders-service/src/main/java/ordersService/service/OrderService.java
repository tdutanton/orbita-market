package ordersService.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
  public OrderResponse createOrder(String userId, String orderIdFromClient, String productType,
      JsonNode payload) {
    log.info("Order Service - вызов createOrder для пользователя {} product type {}", userId,
        productType);
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
    log.info("Order Service - создан заказ {}, присвоен статус CREATED в ordersRepository",
        orderId);
    String eventId = UUID.randomUUID().toString();
    var event = new OrderPaymentRequestedEvent(eventId, orderId, userId, price, Instant.now());

    String eventJson;
    try {
      eventJson = objectMapper.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Ошибка сериализации outbox event", e);
    }
    OrderOutbox outbox = new OrderOutbox(eventId, orderId, "ORDER_PAYMENT_REQUESTED", eventJson);
    outboxRepository.save(outbox);
    log.info(
        "Order Service - в outboxRepository отправлен OrderOutbox с ORDER_PAYMENT_REQUESTED для заказа {}",
        orderId);

    order.setStatus("PAYMENT_PENDING");
    ordersRepository.save(order);
    log.info("Order Service - заказу {} присвоен статус PAYMENT_PENDING в ordersRepository",
        orderId);
    return new OrderResponse(order.getId(), order.getStatus(), order.getProductType(),
        order.getPrice(), order.getCreatedAt());
  }

  public List<OrderResponse> getOrdersByUser(String userId) {
    log.info("Order Service - вызов getOrdersByUser для пользователя {}", userId);
    return ordersRepository.findByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(OrderResponse::from)
        .toList();
  }

  public OrderResponse getOrderById(String orderId, String userId) {
    log.info("Order Service - вызов getOrderById для пользователя {} для заказа {}", userId,
        orderId);
    Order order = ordersRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Заказ не найден по Id: " + orderId));
    if (!order.getUserId().equals(userId)) {
      throw new OrderNotFoundException("Заказ не найден по Id: " + orderId);
    }
    return OrderResponse.from(order);
  }

  @Transactional
  public void completePayment(String orderId) {
    log.info("Order Service - вызов completePayment для заказа {}", orderId);
    Order order = ordersRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Заказ не найден по Id: " + orderId));
    order.setStatus("PAID");
    ordersRepository.save(order);
    log.info(
        "Order Service - в completePayment заказ {} оплачен, в ordersRepository поставлен статус PAID",
        orderId);
  }

  @Transactional
  public void failPayment(String orderId, String failureReason) {
    log.info("Order Service - вызов failPayment для заказа {}", orderId);
    Order order = ordersRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Заказ не найден по Id: " + orderId));
    order.setStatus("PAYMENT_FAILED");
    order.setFailureReason(failureReason);
    ordersRepository.save(order);
    log.info(
        "Order Service - в failPayment у заказа {} ошибка оплаты, в ordersRepository поставлен статус PAYMENT_FAILED: {}",
        orderId, failureReason);
  }

  @Transactional
  public void rejectOrder(String orderId, String failureReason) {
    log.info("Order Service - вызов rejectOrder для заказа {}", orderId);
    Order order = ordersRepository.findByIdForUpdate(orderId)
        .orElseThrow(() -> new OrderNotFoundException("Заказ не найден по Id: " + orderId));
    order.setStatus("REJECTED");
    order.setFailureReason(failureReason);
    ordersRepository.save(order);
    log.info(
        "Order Service - в failPayment заказ {} отклонен при создании, в ordersRepository поставлен статус REJECTED: {}",
        orderId, failureReason);
  }
}
