package paymentsService.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import paymentsService.domain.entity.account.Account;

@Repository
public interface AccountsRepository extends JpaRepository<Account, String> {

  //  c аннотацией @Lock(LockModeType.PESSIMISTIC_WRITE) Spring добавит конструкцию FOR UPDATE для блокировки
  //  - никакая транзакция не сможет изменить тут ничего, пока текущая транзакция не завершится
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Account> findByUserId(String userId);
}
