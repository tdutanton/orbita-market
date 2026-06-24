package ordersService.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record OrderPaymentCompletedEvent(
    @JsonProperty("event_id") String eventId,
    @JsonProperty("order_id") String orderId,
    @JsonProperty("user_id") String userId,
    @JsonProperty("timestamp") Instant timestamp
) {

}
