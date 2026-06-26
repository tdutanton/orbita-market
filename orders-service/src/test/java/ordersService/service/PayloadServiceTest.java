package ordersService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PayloadServiceTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void validatePayload_archiveWithAllFields_shouldReturnNull() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", 100.0);
    payload.put("capture_date", "2024-01-01");
    payload.put("sensor_type", "OPTICAL");

    assertNull(PayloadService.validatePayload("ARCHIVE", payload));
  }

  @Test
  void validatePayload_archiveMissingField_shouldReturnError() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", 100.0);
    payload.put("capture_date", "2024-01-01");

    String error = PayloadService.validatePayload("ARCHIVE", payload);
    assertEquals("Пропущено необходимое поле в payload: sensor_type", error);
  }

  @Test
  void validatePayload_taskingWithAllFields_shouldReturnNull() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", 100.0);
    payload.put("time_window", "2024-01-01/2024-01-07");
    payload.put("sensor_type", "SAR");

    assertNull(PayloadService.validatePayload("TASKING", payload));
  }

  @Test
  void validatePayload_taskingMissingField_shouldReturnError() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", 100.0);

    String error = PayloadService.validatePayload("TASKING", payload);
    assertTrue(error != null && error.startsWith("Пропущено необходимое поле в payload:"));
  }

  @Test
  void validatePayload_monitoringWithAllFields_shouldReturnNull() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", 100.0);
    payload.put("cadence", "WEEKLY");
    payload.put("duration_days", 30);

    assertNull(PayloadService.validatePayload("MONITORING", payload));
  }

  @Test
  void validatePayload_monitoringInvalidCadence_shouldReturnError() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", 100.0);
    payload.put("cadence", "YEARLY");
    payload.put("duration_days", 30);

    String error = PayloadService.validatePayload("MONITORING", payload);
    assertEquals("Некорректная периодичность. Должно быть DAILY или WEEKLY", error);
  }

  @Test
  void validatePayload_monitoringMissingCadence_shouldReturnError() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", 100.0);
    payload.put("duration_days", 30);

    String error = PayloadService.validatePayload("MONITORING", payload);
    assertEquals("Пропущено необходимое поле в payload: cadence", error);
  }

  @Test
  void validatePayload_aoiNotNumber_shouldReturnError() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", "not-a-number");
    payload.put("capture_date", "2024-01-01");
    payload.put("sensor_type", "OPTICAL");

    String error = PayloadService.validatePayload("ARCHIVE", payload);
    assertEquals("Поле 'aoi' должно быть числом", error);
  }

  @Test
  void validatePayload_unknownProductType_shouldReturnError() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("aoi", 100.0);

    String error = PayloadService.validatePayload("UNKNOWN", payload);
    assertEquals("Неизвестный тип продукта", error);
  }

  @Test
  void validatePayload_nullField_shouldReturnError() {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.putNull("aoi");
    payload.put("capture_date", "2024-01-01");
    payload.put("sensor_type", "OPTICAL");

    String error = PayloadService.validatePayload("ARCHIVE", payload);
    assertEquals("Пропущено необходимое поле в payload: aoi", error);
  }
}
