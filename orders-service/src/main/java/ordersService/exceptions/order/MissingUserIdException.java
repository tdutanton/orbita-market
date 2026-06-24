package ordersService.exceptions.order;

public class MissingUserIdException extends RuntimeException {

  public MissingUserIdException(String message) {
    super(message);
  }
}
