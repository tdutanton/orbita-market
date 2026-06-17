package paymentsService.exceptions.user;

public class InvalidUserNameException extends RuntimeException {

  public InvalidUserNameException(String message) {
    super(message);
  }

}
