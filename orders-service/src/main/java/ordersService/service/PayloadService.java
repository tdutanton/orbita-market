package ordersService.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Set;
import ordersService.domain.entity.productType.ProductType;
import org.springframework.stereotype.Service;

@Service
public class PayloadService {

  private static final Set<String> ARCHIVE_FIELDS = Set.of("aoi", "capture_date", "sensor_type");
  private static final Set<String> TASKING_FIELDS = Set.of("aoi", "time_window", "sensor_type");
  private static final Set<String> MONITORING_FIELDS = Set.of("aoi", "cadence", "duration_days");

  static public String validatePayload(String productType, JsonNode payload) {
    Set<String> requiredFields = Set.of();
    ProductType type;
    try {
      type = ProductType.valueOf(productType);
    } catch (IllegalArgumentException e) {
      return "Неизвестный тип продукта";
    }
    switch (type) {
      case ARCHIVE -> requiredFields = ARCHIVE_FIELDS;
      case TASKING -> requiredFields = TASKING_FIELDS;
      case MONITORING -> requiredFields = MONITORING_FIELDS;
      default -> {
        return "Неизвестный тип продукта";
      }
    }
    for (String field : requiredFields) {
      if (!payload.has(field) || payload.get(field).isNull()) {
        return "Пропущено необходимое поле в payload: " + field;
      }
    }
    JsonNode aoiNode = payload.get("aoi");
    if (aoiNode != null && !aoiNode.isNull()) {
      if (!aoiNode.isNumber()) {
        return "Поле 'aoi' должно быть числом";
      }
    }
    if (productType.equals("MONITORING")) {
      JsonNode cadence = payload.get("cadence");
      if (cadence != null && !List.of("DAILY", "WEEKLY").contains(cadence.asText())) {
        return "Некорректная периодичность. Должно быть DAILY или WEEKLY";
      }
    }
    return null;
  }

}
