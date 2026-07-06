package ordersService.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;
import ordersService.domain.entity.productType.ProductType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PriceCalculation {

  @Value("${rates.price-to-aoi.archive}")
  private Integer archiveRate;

  @Value("${rates.price-to-aoi.tasking}")
  private Integer taskingRate;

  @Value("${rates.price-to-aoi.monitoring}")
  private Integer monitoringRate;


  private BigDecimal orderPrice(double aoi, Integer rate) {
    return BigDecimal.valueOf(aoi)
        .multiply(BigDecimal.valueOf(rate))
        .setScale(2, RoundingMode.HALF_EVEN);
  }

  public BigDecimal calculatePrice(String productType, JsonNode aoi) {
    log.info("PriceCalculation - вызов calculatePrice");
    ProductType type;
    try {
      type = ProductType.valueOf(productType);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Неизвестный тип продукта: " + productType);
    }
    return switch (type) {
      case ProductType.ARCHIVE -> orderPrice(aoi.asDouble(), archiveRate);
      case TASKING -> orderPrice(aoi.asDouble(), taskingRate);
      case MONITORING -> orderPrice(aoi.asDouble(), monitoringRate);
    };
  }
}
