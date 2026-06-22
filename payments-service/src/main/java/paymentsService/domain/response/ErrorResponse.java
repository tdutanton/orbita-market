package paymentsService.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public record ErrorResponse(
    String error_code,
    String message,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant timestamp
) {

  public ErrorResponse(String errorCode, String message) {
    this(errorCode, message, Instant.now());
  }
}
