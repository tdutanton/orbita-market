package ordersService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

public class PriceCalculationTest {

  @Test
  void orderPrice_withPositiveAoiAndRate_shouldReturnProduct() {
    BigDecimal result = PriceCalculation.orderPrice(10.0, 100);
    assertEquals(BigDecimal.valueOf(1000.00).setScale(2, RoundingMode.HALF_UP), result);
  }

  @Test
  void orderPrice_withZeroAoi_shouldReturnZero() {
    BigDecimal result = PriceCalculation.orderPrice(0.0, 100);
    assertEquals(BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP), result);
  }

  @Test
  void orderPrice_withNegativeAoi_shouldReturnNegative() {
    BigDecimal result = PriceCalculation.orderPrice(-5.0, 100);
    assertEquals(BigDecimal.valueOf(-500.00).setScale(2, RoundingMode.HALF_UP), result);
  }

  @Test
  void orderPrice_withFractionalAoi_shouldRoundToTwoDecimals() {
    BigDecimal result = PriceCalculation.orderPrice(3.14159, 100);
    assertEquals(BigDecimal.valueOf(314.16).setScale(2, RoundingMode.HALF_UP), result);
  }

  @Test
  void orderPrice_withZeroRate_shouldReturnZero() {
    BigDecimal result = PriceCalculation.orderPrice(100.0, 0);
    assertEquals(BigDecimal.valueOf(0.00).setScale(2, RoundingMode.HALF_UP), result);
  }
}
