package ordersService.exceptions.order;

public class InvalidPriceException extends RuntimeException {

  public InvalidPriceException(String message) {
    super(message);
  }
}
