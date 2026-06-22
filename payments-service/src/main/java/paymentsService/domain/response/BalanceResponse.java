package paymentsService.domain.response;

import java.math.BigDecimal;

public record BalanceResponse(String userId, BigDecimal balance) {

}
