package paymentsService.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import paymentsService.domain.entity.account.Account;
import paymentsService.domain.response.AccountResponse;
import paymentsService.exceptions.account.AccountAlreadyExistsException;
import paymentsService.exceptions.account.AccountNotFoundException;
import paymentsService.exceptions.account.InvalidAmountException;
import paymentsService.repository.AccountsRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

  private final AccountsRepository accountsRepository;
  private final AmountValidator amountValidator;

  @Transactional
  public AccountResponse createAccount(String userId) {
    log.info("AccountService - вызов createAccount для пользователя {}", userId);
    if (accountsRepository.findById(userId).isPresent()) {
      log.warn("AccountService - счет уже создан для пользователя {}, новый сделать нельзя",
          userId);
      throw new AccountAlreadyExistsException("Счет уже создан для пользователя: " + userId);
    }
    Account account = new Account(userId);
    Account savedAccount = accountsRepository.save(account);
    log.info("AccountService - создан счет для пользователя {}, сохранено в accountRepository",
        userId);
    return new AccountResponse(
        savedAccount.getUserId(),
        savedAccount.getBalance(),
        savedAccount.getCurrency()
    );
  }

  @Transactional
  public AccountResponse deposit(String userId, BigDecimal amount) {
    log.info("AccountService - вызов deposit для пользователя {} на сумму {}", userId,
        amount);
    if (amountValidator.isInvalidDepositAmount(amount)) {
      throw new InvalidAmountException("Некорректная сумма для пополнения");
    }
    Account account = accountsRepository.findByUserIdForUpdate(userId)
        .orElseThrow(() -> new AccountNotFoundException("Пользователь и счет не найден"));

    BigDecimal newBalance = account.getBalance().add(amount).setScale(2, RoundingMode.HALF_UP);
    account.setBalance(newBalance);
    Account savedAccount = accountsRepository.save(account);
    return new AccountResponse(
        savedAccount.getUserId(),
        savedAccount.getBalance(),
        savedAccount.getCurrency()
    );
  }

  @Transactional(readOnly = true)
  public AccountResponse getAccount(String userId) {
    log.info("AccountService - вызов getAccount для пользователя {}", userId);
    Account account = accountsRepository.findById(userId)
        .orElseThrow(() -> new AccountNotFoundException("Пользователь и счет не найден"));
    return new AccountResponse(account.getUserId(), account.getBalance(), account.getCurrency());
  }

  @Transactional(readOnly = true)
  public List<AccountResponse> getAllAccounts() {
    log.info("AccountService - вызов getAllAccounts");
    List<Account> accounts = accountsRepository.findAll();
    List<AccountResponse> result = new ArrayList<>();
    for (Account account : accounts) {
      result.add(
          new AccountResponse(account.getUserId(), account.getBalance(), account.getCurrency()));
    }
    return result;
  }
}

