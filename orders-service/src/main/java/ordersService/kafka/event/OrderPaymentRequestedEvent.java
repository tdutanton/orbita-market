package ordersService.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderPaymentRequestedEvent(
    @JsonProperty("event_id") String eventId,
    @JsonProperty("order_id") String orderId,
    @JsonProperty("user_id") String userId,
    @JsonProperty("amount") BigDecimal amount,
    @JsonProperty("timestamp") Instant timestamp
) {

}
