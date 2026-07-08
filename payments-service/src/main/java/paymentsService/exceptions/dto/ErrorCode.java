package paymentsService.exceptions.dto;

import lombok.Getter;

@Getter
public enum ErrorCode {

  ACCOUNT_ALREADY_EXISTS("ACCOUNT_ALREADY_EXISTS"),
  INVALID_AMOUNT("INVALID_AMOUNT"),
  ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND"),
  MISSING_USER_ID("MISSING_USER_ID"),
  INTERNAL_ERROR("INTERNAL_ERROR");

  private final String description;

  ErrorCode(String description) {
    this.description = description;
  }

}