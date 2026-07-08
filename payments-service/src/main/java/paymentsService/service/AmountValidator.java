package paymentsService.service;

import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AmountValidator {

  @Value("${validate.min-deposit-amount}")
  private BigDecimal minDepositAmount;

  public boolean isValidDepositAmount(BigDecimal amount) {
    log.info("AmountValidator - вызов isValidDepositAmount");
    if (amount == null) {
      return false;
    }
    return amount.compareTo(minDepositAmount) > 0;
  }

  public boolean isInvalidDepositAmount(BigDecimal amount) {
    log.info("AmountValidator - вызов isInvalidDepositAmount");
    if (amount == null) {
      return true;
    }
    return amount.compareTo(minDepositAmount) <= 0;
  }
}
