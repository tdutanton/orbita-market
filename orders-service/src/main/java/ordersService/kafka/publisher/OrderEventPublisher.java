package ordersService.kafka.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void publishToKafka(String topic, String key, Object event) {
    try {
      String json = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(topic, key, json);
      log.debug("Опубликован event для топика {} с ключом {}", topic, key);
    } catch (Exception e) {
      log.error("Ошибка публикации event для топика {} с ключом {}", topic, key, e);
    }
  }
}
