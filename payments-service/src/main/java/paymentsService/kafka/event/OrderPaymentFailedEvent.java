package paymentsService.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record OrderPaymentFailedEvent(
    @JsonProperty("event_id") String eventId,
    @JsonProperty("order_id") String orderId,
    @JsonProperty("user_id") String userId,
    @JsonProperty("failure_reason") String failureReason,
    @JsonProperty("timestamp") Instant timestamp
) {
}
