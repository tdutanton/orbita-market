package ordersService.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ordersService.domain.entity.order.Order;
import ordersService.domain.request.OrderRequest;
import ordersService.domain.response.OrderListResponse;
import ordersService.domain.response.OrderResponse;
import ordersService.exceptions.order.InternalErrorException;
import ordersService.exceptions.order.InvalidPayloadException;
import ordersService.exceptions.order.InvalidPriceException;
import ordersService.exceptions.order.MissingUserIdException;
import ordersService.exceptions.order.OrderNotFoundException;
import ordersService.exceptions.order.UnknownProductTypeException;
import ordersService.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrdersServiceController {

  private static final Set<String> VALID_PRODUCT_TYPES = Set.of("ARCHIVE", "TASKING", "MONITORING");
  private static final Set<String> ARCHIVE_FIELDS = Set.of("aoi", "capture_date", "sensor_type");
  private static final Set<String> TASKING_FIELDS = Set.of("aoi", "time_window", "sensor_type");
  private static final Set<String> MONITORING_FIELDS = Set.of("aoi", "cadence", "duration_days");
  private final OrderService orderService;

  private String validatePayload(String productType, JsonNode payload) {
    Set<String> requiredFields;
    switch (productType) {
      case "ARCHIVE" -> requiredFields = ARCHIVE_FIELDS;
      case "TASKING" -> requiredFields = TASKING_FIELDS;
      case "MONITORING" -> requiredFields = MONITORING_FIELDS;
      default -> {
        return "Неизвестный тип продукта";
      }
    }

    for (String field : requiredFields) {
      if (!payload.has(field) || payload.get(field).isNull()) {
        return "Пропущено необходимое поле: " + field;
      }
    }

    if (productType.equals("MONITORING")) {
      JsonNode cadence = payload.get("cadence");
      if (cadence != null && !List.of("DAILY", "WEEKLY").contains(cadence.asText())) {
        return "Некорректная периодичность. Должно быть DAILY или WEEKLY";
      }
    }

    return null;
  }

  @PostMapping("/orders")
  public ResponseEntity<OrderResponse> createOrder(
      @RequestHeader("X-User-Id") String userId,
      @RequestBody OrderRequest request) {
    if (userId == null || userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    if (request.productType() == null || !VALID_PRODUCT_TYPES.contains(request.productType())) {
      throw new UnknownProductTypeException(
          "Неподдерживаемый тип продукта: " + request.productType());
    }
    if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidPriceException("Некорректная стоимость");
    }
    if (request.payload() == null || request.payload().isEmpty()) {
      throw new InvalidPayloadException("Некорректная составляющая payload");
    }
    String validationError = validatePayload(request.productType(), request.payload());
    if (validationError != null) {
      throw new InvalidPayloadException("Некорректная составляющая payload");
    }
    try {
      Order order = orderService.createOrder(userId, request.productType(), request.price(),
          request.payload());
      return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    } catch (Exception e) {
      log.error("Ошибка при создании заказа", e);
      throw new InternalErrorException("Ошибка при создании заказа");
    }
  }

  @GetMapping
  public ResponseEntity<OrderListResponse> getOrders(@RequestHeader("X-User-Id") String userId) {
    if (userId == null || userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    List<OrderResponse> orders = orderService.getOrdersByUser(userId);
    return ResponseEntity.ok(new OrderListResponse(orders, orders.size()));
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getOrder(
      @RequestHeader("X-User-Id") String userId,
      @PathVariable String orderId) {
    if (userId == null || userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    try {
      OrderResponse response = orderService.getOrderById(orderId, userId);
      return ResponseEntity.ok(response);
    } catch (OrderNotFoundException e) {
      throw new OrderNotFoundException("Заказ не найден");
    }
  }
}
