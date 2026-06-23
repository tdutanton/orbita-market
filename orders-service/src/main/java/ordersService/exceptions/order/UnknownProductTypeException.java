package ordersService.exceptions.order;

public class UnknownProductTypeException extends RuntimeException {

  public UnknownProductTypeException(String message) {
    super(message);
  }
}
