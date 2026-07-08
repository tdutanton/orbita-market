package paymentsService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import paymentsService.domain.response.ErrorResponse;
import paymentsService.exceptions.account.AccountAlreadyExistsException;
import paymentsService.exceptions.account.AccountNotFoundException;
import paymentsService.exceptions.account.InvalidAmountException;
import paymentsService.exceptions.account.MissingUserIdException;
import paymentsService.exceptions.dto.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
    // badRequest() - код ошибки 400
    return ResponseEntity.badRequest()
        .body(
            new ErrorResponse(ErrorCode.MISSING_USER_ID.getDescription(),
                "Ошибка в заголовке: " + ex.getHeaderName()));
  }

  @ExceptionHandler(MissingUserIdException.class)
  public ResponseEntity<ErrorResponse> handleMissingUserId() {
    // badRequest() - код ошибки 400
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.MISSING_USER_ID.getDescription(),
            "Ошибка в заголовке: X-User-Id"));
  }

  @ExceptionHandler(InvalidAmountException.class)
  public ResponseEntity<ErrorResponse> handleInvalidAmount() {
    // badRequest() - код ошибки 400
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.INVALID_AMOUNT.getDescription(),
            "Некорректное значение суммы"));
  }

  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
    // HttpStatus.NOT_FOUND - код ошибки 404
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ErrorCode.ACCOUNT_NOT_FOUND.getDescription(), ex.getMessage()));
  }

  @ExceptionHandler(AccountAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleAccountAlreadyExists(
      AccountAlreadyExistsException ex) {
    // HttpStatus.CONFLICT - код ошибки 409
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new ErrorResponse(ErrorCode.ACCOUNT_ALREADY_EXISTS.getDescription(), ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    // HttpStatus.INTERNAL_SERVER_ERROR - код ошибки 500
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR.getDescription(),
            "Внутренняя ошибка сервера"));
  }
}
