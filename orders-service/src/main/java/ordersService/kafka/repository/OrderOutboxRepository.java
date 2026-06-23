package ordersService.kafka.repository;

import java.util.List;
import ordersService.kafka.outbox.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderOutboxRepository extends JpaRepository<OrderOutbox, Long> {

  List<OrderOutbox> findBySentFalseOrderByCreatedAtAsc();
}
