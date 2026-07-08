package paymentsService.kafka.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paymentsService.kafka.outbox.PaymentOutbox;

@Repository
public interface PaymentOutboxEventRepository extends JpaRepository<PaymentOutbox, String> {

  List<PaymentOutbox> findByStatusOrderByCreatedAtAsc(String status);
}

