package ordersService.kafka.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "order_inbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class OrderInbox {

  @Id
  @Column(name = "event_id")
  @EqualsAndHashCode.Include
  private String eventId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(nullable = false)
  private Boolean processed = false;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Version
  private Long version = 0L;

  public OrderInbox(String eventId, String orderId, String eventType) {
    this.eventId = eventId;
    this.orderId = orderId;
    this.eventType = eventType;
    this.processed = false;
    this.createdAt = Instant.now();
  }
}
