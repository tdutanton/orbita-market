package paymentsService.exceptions.account;

public class ImpossibleOperationException extends RuntimeException {

  ImpossibleOperationException(String message) {
    super(message);
  }
}
