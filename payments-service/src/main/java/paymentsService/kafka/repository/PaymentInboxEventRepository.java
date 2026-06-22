package paymentsService.kafka.repository;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import paymentsService.kafka.inbox.PaymentInbox;

@Repository
public interface PaymentInboxEventRepository extends JpaRepository<PaymentInbox, String> {

  // идемпотентность операции вставки
  @Modifying
  @Query(value = "INSERT INTO payment_inbox_events (event_id, order_id, user_id, amount, status) " +
      "VALUES (:eventId, :orderId, :userId, :amount, 'PENDING') " +
      "ON CONFLICT (event_id) DO NOTHING", nativeQuery = true)
  int tryInsert(@Param("eventId") String eventId,
      @Param("orderId") String orderId,
      @Param("userId") String userId,
      @Param("amount") BigDecimal amount);
}
