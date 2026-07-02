package ordersService.kafka.config;


import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka // подключение поддержки @Kafkalistener
public class KafkaConfig {

  // адрес для kafka
  @Value("${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}")
  private String bootstrapServers;

  // имя группы потребителей
  @Value("${KAFKA_GROUP_ID:orders-service}")
  private String groupId;

  @Bean
  public NewTopic paymentRequestedTopic(
      @Value("${PAYMENT_REQUESTED_TOPIC:order.payment.requested}") String topicName) {
    return TopicBuilder.name(topicName)
        .partitions(1)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic paymentCompletedTopic(
      @Value("${PAYMENT_COMPLETED_TOPIC:order.payment.completed}") String topicName) {
    return TopicBuilder.name(topicName)
        .partitions(1)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic paymentFailedTopic(
      @Value("${PAYMENT_FAILED_TOPIC:order.payment.failed}") String topicName) {
    return TopicBuilder.name(topicName)
        .partitions(1)
        .replicas(1)
        .build();
  }

  // создание фабрики для создания kafka-продюсеров (генерирует данные и отправляет в топики)
  @Bean
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> config = new HashMap<>();
    // где находится kafka сервер
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    // как сериализовать ключ сообщения (превратить в байты)
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // как сериализовать тело сообщения
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // вкл идемпотентное поведение (игнорирование сообщений с уже обработанным номером)
    config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    // что делать если не удалась отправка
    // пробует максимальное количество раз для соблюдения идемпотентности
    config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
    // ждет 1 секунду между попытками
    config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
    // ждет подтверждения от всех реплик партиции
    config.put(ProducerConfig.ACKS_CONFIG, "all");
    // реализация фабрики от spring
    return new DefaultKafkaProducerFactory<>(config);
  }

  // класс-шаблон создает продюсера и отправляет данные в топики
  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  // создание фабрики для создания kafka-консьюмеров
  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> config = new HashMap<>();
    // где находится kafka кластер
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    // группа потребителей
    config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    // как делать десериализацию
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    // отключение автоматических коммитов смещений (кафка сама не отмечает сообщения как обработанные)
    config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    // если сервис запускается впервые - читать с самого начала
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    // за один poll() получать максимум 10 сообщений
    config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
    // ждет 1 секунду перед первой попыткой переподключения при обрыве связи
    config.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "1000");
    // максимальная задержка между попытками переподключения
    config.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, "10000");
    // если Kafka не отвечает 30 секунд, считать запрос плохим
    config.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");
    return new DefaultKafkaConsumerFactory<>(config);
  }

  // отвечает за создание и настройку слушателей kafka
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    // связывает с настройками, заданными в consumerFactory()
    factory.setConsumerFactory(consumerFactory());
    // ручной режим подтверждения
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    return factory;
  }
}
