package paymentsService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import paymentsService.domain.request.TopUpRequest;
import paymentsService.domain.response.AccountResponse;
import paymentsService.domain.response.BalanceResponse;
import paymentsService.domain.response.ErrorResponse;
import paymentsService.exceptions.account.MissingUserIdException;
import paymentsService.service.AccountService;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsServiceController {

  private final AccountService accountService;

  @PostMapping("/accounts")
  @Operation(summary = "Создание счета для пользователя")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Счет успешно создан",
          content = @Content(schema = @Schema(implementation = AccountResponse.class))),
      @ApiResponse(responseCode = "400", description = "Нет id пользователя",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Счет уже существует",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<AccountResponse> createAccount(@RequestHeader("X-User-Id") String userId) {
    if (userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    AccountResponse accountResponse = accountService.createAccount(userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new AccountResponse(accountResponse.userId(), accountResponse.balance(),
                accountResponse.currency()));
  }

  @PostMapping("/accounts/top-up")
  @Operation(summary = "Пополнение счета для пользователя")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Счет успешно пополнен",
          content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
      @ApiResponse(responseCode = "400", description = "Нет id пользователя, некорректная сумма",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Счет не создан",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<BalanceResponse> topUp(
      @RequestHeader("X-User-Id") String userId,
      @RequestBody TopUpRequest request) {
    if (userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    AccountResponse accountResponse = accountService.deposit(userId, request.value());
    return ResponseEntity.ok(
        new BalanceResponse(accountResponse.userId(), accountResponse.balance(),
            accountResponse.currency()));
  }

  @GetMapping("/accounts/balance")
  @Operation(summary = "Получить баланс пользователя")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Баланс успешно загружен",
          content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
      @ApiResponse(responseCode = "400", description = "Нет id пользователя",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "Счет не найден",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<BalanceResponse> getBalance(@RequestHeader("X-User-Id") String userId) {
    if (userId.isBlank()) {
      throw new MissingUserIdException("X-User-Id нет в заголовке");
    }
    AccountResponse accountResponse = accountService.getAccount(userId);
    return ResponseEntity.ok(
        new BalanceResponse(accountResponse.userId(), accountResponse.balance(),
            accountResponse.currency()));
  }

  @GetMapping("/accounts/overview")
  @Operation(summary = "Получить список пользователей и балансов")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Список пользователей и балансов успешно загружен",
          content = @Content(schema = @Schema(implementation = AccountResponse.class))),
      @ApiResponse(responseCode = "400", description = "Нет id пользователя",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ResponseEntity<List<AccountResponse>> overview() {
    List<AccountResponse> responses = accountService.getAllAccounts().stream()
        .map(
            account -> new AccountResponse(account.userId(), account.balance(), account.currency()))
        .toList();
    return ResponseEntity.ok(responses);
  }
}
