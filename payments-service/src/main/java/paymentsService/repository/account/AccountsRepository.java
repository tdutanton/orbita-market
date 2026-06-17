package paymentsService.repository.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import paymentsService.domain.entity.account.Account;

@Repository
public interface AccountsRepository extends JpaRepository<Account, Long> {

  public Optional<Account> findByUserId(Long userId);
}
