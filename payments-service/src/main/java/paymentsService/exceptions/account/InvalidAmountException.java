package paymentsService.exceptions.account;

public class InvalidAmountException extends RuntimeException {

  public InvalidAmountException(String message) {
    super(message);
  }
}
