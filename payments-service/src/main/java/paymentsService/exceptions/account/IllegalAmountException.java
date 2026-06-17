package paymentsService.exceptions.account;

public class IllegalAmountException extends RuntimeException {

  public IllegalAmountException(String message) {
    super(message);
  }
}
