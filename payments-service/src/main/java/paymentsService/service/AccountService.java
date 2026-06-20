package paymentsService.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paymentsService.domain.entity.account.Account;
import paymentsService.exceptions.account.AccountAlreadyExistsException;
import paymentsService.exceptions.account.AccountNotFoundException;
import paymentsService.exceptions.account.IllegalAmountException;
import paymentsService.repository.AccountsRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

  private final AccountsRepository accountsRepository;

  @Transactional
  public Account createAccount(String userId) {
    if (accountsRepository.findById(userId).isPresent()) {
      throw new AccountAlreadyExistsException("Счет уже создан для пользователя: " + userId);
    }
    Account account = new Account(userId);
    return accountsRepository.save(account);
  }

  @Transactional
  public Account deposit(String userId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalAmountException("Некорректная сумма для пополнения");
    }
    Account account = accountsRepository.findByUserId(userId)
        .orElseThrow(() -> new AccountNotFoundException("Пользователь и счет не найден"));

    BigDecimal newBalance = account.getBalance().add(amount).setScale(2, RoundingMode.HALF_UP);
    account.setBalance(newBalance);
    return accountsRepository.save(account);
  }

  @Transactional(readOnly = true)
  public Account getAccount(String userId) {
    return accountsRepository.findById(userId)
        .orElseThrow(() -> new AccountNotFoundException("Пользователь и счет не найден"));
  }
}

