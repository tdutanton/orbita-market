package ordersService.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Instant;
import ordersService.domain.entity.order.Order;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(
    @JsonProperty("order_id") String orderId,
    @JsonProperty("user_id") String userId,
    @JsonProperty("product_type") String productType,
    @JsonProperty("status") String status,
    @JsonProperty("price") BigDecimal price,
    @JsonProperty("payload") JsonNode payload,
    @JsonProperty("failure_reason") String failureReason,
    @JsonProperty("created_at") Instant createdAt
) {

  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getUserId(),
        order.getProductType(),
        order.getStatus(),
        order.getPrice(),
        order.getPayload(),
        order.getFailureReason(),
        order.getCreatedAt()
    );
  }
}
