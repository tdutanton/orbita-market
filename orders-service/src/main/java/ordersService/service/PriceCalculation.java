package ordersService.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class PriceCalculation {

  public static BigDecimal orderPrice(double aoi, Integer rate) {
    return BigDecimal.valueOf(aoi)
        .multiply(BigDecimal.valueOf(rate))
        .setScale(2, RoundingMode.HALF_UP);
  }
}
