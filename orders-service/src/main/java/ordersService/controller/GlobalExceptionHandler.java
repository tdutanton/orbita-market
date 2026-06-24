package ordersService.controller;

import ordersService.domain.response.ErrorResponse;
import ordersService.exceptions.dto.ErrorCode;
import ordersService.exceptions.order.InternalErrorException;
import ordersService.exceptions.order.InvalidPayloadException;
import ordersService.exceptions.order.InvalidPriceException;
import ordersService.exceptions.order.MissingUserIdException;
import ordersService.exceptions.order.OrderNotFoundException;
import ordersService.exceptions.order.UnknownProductTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

  @ExceptionHandler(InvalidPriceException.class)
  public ResponseEntity<ErrorResponse> handleInvalidAmount() {
    // badRequest() - код ошибки 400
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.INVALID_PRICE.getDescription(),
            "Некорректное значение цены"));
  }

  @ExceptionHandler(InvalidPayloadException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPayload() {
    // badRequest() - код ошибки 400
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.INVALID_PAYLOAD.getDescription(),
            "Нет обязательных полей в payload"));
  }

  @ExceptionHandler(UnknownProductTypeException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFound(UnknownProductTypeException ex) {
    // badRequest() - код ошибки 400
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.UNKNOWN_PRODUCT_TYPE.getDescription(),
            "Неподдерживаемый product_type"));
  }

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAccountNotFound(OrderNotFoundException ex) {
    // HttpStatus.NOT_FOUND - код ошибки 404
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ErrorCode.ORDER_NOT_FOUND.getDescription(),
            "Заказ не найден или чужой user_id"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    // HttpStatus.INTERNAL_SERVER_ERROR - код ошибки 500
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR.getDescription(),
            "Внутренняя ошибка сервера"));
  }

  @ExceptionHandler(InternalErrorException.class)
  public ResponseEntity<ErrorResponse> handleInternalError(InternalErrorException ex) {
    // HttpStatus.INTERNAL_SERVER_ERROR - код ошибки 500
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR.getDescription(),
            "Внутренняя ошибка"));
  }
}
