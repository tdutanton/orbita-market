package paymentsService.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import paymentsService.domain.entity.account.Account;
import paymentsService.domain.request.TopUpRequest;
import paymentsService.domain.response.AccountResponse;
import paymentsService.domain.response.BalanceResponse;
import paymentsService.exceptions.account.MissingUserIdException;
import paymentsService.service.AccountService;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsServiceController {

  private final AccountService accountService;

  @PostMapping("/accounts")
  public ResponseEntity<AccountResponse> createAccount(@RequestHeader("X-User-Id") String userId) {
    if (userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    Account account = accountService.createAccount(userId);
    return ResponseEntity.ok(new AccountResponse(account.getUserId(), account.getBalance()));
  }

  @PostMapping("/accounts/top-up")
  public ResponseEntity<BalanceResponse> topUp(
      @RequestHeader("X-User-Id") String userId,
      @RequestBody TopUpRequest request) {
    if (userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    Account account = accountService.deposit(userId, request.value());
    return ResponseEntity.ok(
        new BalanceResponse(account.getUserId(), account.getBalance(), account.getCurrency()));
  }

  @GetMapping("/accounts/balance")
  public ResponseEntity<BalanceResponse> getBalance(@RequestHeader("X-User-Id") String userId) {
    if (userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    Account account = accountService.getAccount(userId);
    return ResponseEntity.ok(
        new BalanceResponse(account.getUserId(), account.getBalance(), account.getCurrency()));
  }

  @GetMapping("/accounts/overview")
  public ResponseEntity<List<AccountResponse>> overview() {
    List<Account> accounts = accountService.getAllAccounts();
    List<AccountResponse> responses = accounts.stream()
        .map(account -> new AccountResponse(account.getUserId(), account.getBalance()))
        .toList();
    return ResponseEntity.ok(responses);
  }
}
