package ordersService.exceptions.dto;

import lombok.Getter;

@Getter
public enum ErrorCode {

  INVALID_PAYLOAD("INVALID_PAYLOAD"),
  INVALID_PRICE("INVALID_PRICE"),
  UNKNOWN_PRODUCT_TYPE("UNKNOWN_PRODUCT_TYPE"),
  ORDER_NOT_FOUND("ORDER_NOT_FOUND"),
  MISSING_USER_ID("MISSING_USER_ID"),
  INTERNAL_ERROR("INTERNAL_ERROR");

  private final String description;

  ErrorCode(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}