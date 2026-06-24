package ordersService.kafka.repository;

import java.util.Optional;
import ordersService.kafka.inbox.OrderInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderInboxEventRepository extends JpaRepository<OrderInbox, String> {

  Optional<OrderInbox> findByEventId(String eventId);

  @Modifying
  @Query(value = "INSERT INTO order_inbox_events (event_id, order_id, event_type) " +
      "VALUES (:eventId, :orderId, :eventType) " +
      "ON CONFLICT (event_id) DO NOTHING", nativeQuery = true)
  int tryInsert(@Param("eventId") String eventId,
      @Param("orderId") String orderId,
      @Param("eventType") String eventType);
}
