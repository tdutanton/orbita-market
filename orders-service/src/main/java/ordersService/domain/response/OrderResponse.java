package ordersService.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import ordersService.domain.entity.order.Order;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(
    @JsonProperty("order_id") String orderId,
    @JsonProperty("status") String status,
    @JsonProperty("product_type") String productType,
    @JsonProperty("price") BigDecimal price,
    @JsonProperty("created_at") Instant createdAt
) {

  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getStatus(),
        order.getProductType(),
        order.getPrice(),
        order.getCreatedAt()
    );
  }
}
