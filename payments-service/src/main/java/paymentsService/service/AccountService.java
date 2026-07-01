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

  @Transactional
  public AccountResponse createAccount(String userId) {
    if (accountsRepository.findById(userId).isPresent()) {
      throw new AccountAlreadyExistsException("Счет уже создан для пользователя: " + userId);
    }
    Account account = new Account(userId);
    Account savedAccount = accountsRepository.save(account);
    return new AccountResponse(
        savedAccount.getUserId(),
        savedAccount.getBalance(),
        savedAccount.getCurrency()
    );
  }

  @Transactional
  public AccountResponse deposit(String userId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
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
    Account account = accountsRepository.findById(userId)
        .orElseThrow(() -> new AccountNotFoundException("Пользователь и счет не найден"));
    return new AccountResponse(account.getUserId(), account.getBalance(), account.getCurrency());
  }

  @Transactional(readOnly = true)
  public List<AccountResponse> getAllAccounts() {
    List<Account> accounts = accountsRepository.findAll();
    List<AccountResponse> result = new ArrayList<>();
    for (Account account : accounts) {
      result.add(
          new AccountResponse(account.getUserId(), account.getBalance(), account.getCurrency()));
    }
    return result;
  }
}

