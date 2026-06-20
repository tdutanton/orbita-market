package paymentsService.service;

import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.Optional;
import javax.security.auth.login.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paymentsService.domain.entity.account.Account;
import paymentsService.domain.entity.user.User;
import paymentsService.exceptions.account.IllegalAmountException;
import paymentsService.repository.AccountsRepository;
import paymentsService.repository.user.UsersRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableRetry
public class CentralService {

  private final AccountsRepository accountsRepository;
  private final UsersRepository usersRepository;

  public User createUser() {
    return new User();
  }

  @Transactional
  public Long createAndSaveUser() {
    User user = createUser();
    usersRepository.save(user);
    return user.getId();
  }

  @Transactional
  public Optional<Long> createAndSaveAccount(Long userId) {
    if (usersRepository.findById(userId).isPresent() && accountsRepository.findByUserId(userId)
        .isEmpty()) {
      User user = usersRepository.findById(userId).get();
      Account account = new Account(user);
      accountsRepository.save(account);
      return Optional.of(account.getId());
    }
    return Optional.empty();
  }

  @Retryable(
      retryFor = OptimisticLockException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 100)
  )
  @Transactional
  public void depositTo(BigDecimal amount, Long userId) throws AccountNotFoundException {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalAmountException("Некорректная сумма");
    }
    Account account = accountsRepository.findByUserId(userId)
        .orElseThrow(() -> new AccountNotFoundException("Счет не найден"));

    account.setBalance(account.getBalance().add(amount));
  }
}
