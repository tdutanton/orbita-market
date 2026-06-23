package ordersService.domain.inner.jsonConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(JsonNode attribute) {
    if (attribute == null) {
      return null;
    }
    try {
      return MAPPER.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Не удалось сконвертировать ноду Json в JSON строку", e);
    }
  }

  @Override
  public JsonNode convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return null;
    }
    try {
      return MAPPER.readTree(dbData);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Не удалось сконвертировать JSON строку в ноду Json", e);
    }
  }
}