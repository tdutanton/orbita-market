package paymentsService.domain.response;

import java.math.BigDecimal;

public record AccountResponse(String userId, BigDecimal balance, String currency) {

}
