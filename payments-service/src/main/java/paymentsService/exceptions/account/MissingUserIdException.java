package paymentsService.exceptions.account;

public class MissingUserIdException extends RuntimeException {

  public MissingUserIdException(String message) {
    super(message);
  }
}
