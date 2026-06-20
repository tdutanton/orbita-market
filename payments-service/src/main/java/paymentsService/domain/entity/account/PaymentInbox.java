package paymentsService.domain.entity.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "payment_inbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class PaymentInbox {

  @Id
  @Column(name = "event_id")
  @EqualsAndHashCode.Include
  private String eventId;

  @Column(name = "order_id", nullable = false)
  private String orderId;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(precision = 19, scale = 2, nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private String status = "PENDING";

  @Column(name = "failure_reason")
  private String failureReason;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Version
  private Long version = 0L;

  public PaymentInbox(String eventId, String orderId, String userId, BigDecimal amount) {
    this.eventId = eventId;
    this.orderId = orderId;
    this.userId = userId;
    this.amount = amount;
    this.status = "PENDING";
  }
}
