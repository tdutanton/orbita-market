package paymentsService.domain.request;

import java.math.BigDecimal;

public record TopUpRequest(BigDecimal value) {

}
