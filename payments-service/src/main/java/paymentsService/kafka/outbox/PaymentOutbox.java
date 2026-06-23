package paymentsService.kafka.outbox;

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
@Table(name = "payment_outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PaymentOutbox {

  @Id
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private String id;

  @Column(name = "topic", nullable = false, length = 100)
  private String topic;

  @Column(name = "key")
  private String key;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "status", nullable = false)
  private String status = "PENDING";

  @Column(name = "retry_count", nullable = false)
  private int retryCount = 0;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Version
  private Long version = 0L;

  public PaymentOutbox(String id, String topic, String key, String payload) {
    this.id = id;
    this.topic = topic;
    this.key = key;
    this.payload = payload;
    this.status = "PENDING";
    this.createdAt = Instant.now();
  }
}
