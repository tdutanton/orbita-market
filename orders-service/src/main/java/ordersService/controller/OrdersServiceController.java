package ordersService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ordersService.domain.entity.order.Order;
import ordersService.domain.entity.productType.ProductType;
import ordersService.domain.request.OrderRequest;
import ordersService.domain.response.ErrorResponse;
import ordersService.domain.response.OrderListResponse;
import ordersService.domain.response.OrderResponse;
import ordersService.exceptions.order.InternalErrorException;
import ordersService.exceptions.order.InvalidPayloadException;
import ordersService.exceptions.order.MissingUserIdException;
import ordersService.exceptions.order.OrderNotFoundException;
import ordersService.service.OrderService;
import ordersService.service.PayloadService;
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

  private final OrderService orderService;

  @PostMapping("/orders")
  @Operation(summary = "Создание заказа")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Заказ успешно создан",
          content = @Content(schema = @Schema(implementation = OrderResponse.class))),
      @ApiResponse(responseCode = "400",
          description =
              "Некорректный запрос (нет обязательных полей в payload, некорректная цена, неподдерживаемый тип продукта (заказа)), "
                  + "нет id пользователя",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<OrderResponse> createOrder(
      @RequestHeader("X-User-Id") String userId,
      @RequestBody OrderRequest request) {
    if (userId == null || userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }

    ProductType.validateFromName(request.productType());

    if (request.payload() == null || request.payload().isEmpty()) {
      throw new InvalidPayloadException("Некорректная составляющая payload");
    }
    String validationError = PayloadService.validatePayload(request.productType(),
        request.payload());
    if (validationError != null) {
      throw new InvalidPayloadException("Некорректная составляющая payload");
    }
    try {
      Order order = orderService.createOrder(userId, request.productType(), request.payload());
      return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    } catch (Exception e) {
      log.error("Ошибка при создании заказа", e);
      throw new InternalErrorException("Ошибка при создании заказа");
    }
  }

  @GetMapping
  @Operation(summary = "Получение заказов по user id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Список заказов успешно загружен",
          content = @Content(schema = @Schema(implementation = OrderListResponse.class))),
      @ApiResponse(responseCode = "400", description = "Нет id пользователя",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<OrderListResponse> getOrders(@RequestHeader("X-User-Id") String userId) {
    if (userId == null || userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    List<OrderResponse> orders = orderService.getOrdersByUser(userId);
    return ResponseEntity.ok(new OrderListResponse(orders, orders.size()));
  }

  @GetMapping("/{orderId}")
  @Operation(summary = "Получение заказа по id и по user id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Заказ успешно загружен",
          content = @Content(schema = @Schema(implementation = OrderResponse.class))),
      @ApiResponse(responseCode = "400", description = "Нет id пользователя",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Заказ не найден или чужой user id",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
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
