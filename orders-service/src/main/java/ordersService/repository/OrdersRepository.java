package ordersService.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import ordersService.domain.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<Order, String> {

  List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Order> findById(String id);
}
