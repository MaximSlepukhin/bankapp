//package integration;
//
//import com.github.maximslepukhin.NotificationsServiceApplication;
//import com.github.maximslepukhin.repository.NotificationRepository;
//import com.github.maximslepukhin.service.NotificationServiceImpl;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Bean;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.test.EmbeddedKafkaBroker;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.kafka.test.utils.KafkaTestUtils;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest(
//        classes = NotificationsServiceApplication.class,
//        webEnvironment = SpringBootTest.WebEnvironment.NONE
//)
//@EmbeddedKafka(
//        topics = {"notifications"},
//        partitions = 1,
//        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
//)
//@ActiveProfiles("test")
//class NotificationsKafkaConsumerTest {
//
//    @MockBean
//    private NotificationRepository notificationRepository;
//
//    @Autowired
//    private NotificationServiceImpl notificationService;
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    @Autowired
//    private EmbeddedKafkaBroker embeddedKafkaBroker;
//
//    @Autowired
//    private ConsumerFactory<String, String> consumerFactory;
//
//    @Test
//    void testNotificationConsumer() {
//        try (var consumer = consumerFactory.createConsumer()) {
//            consumer.subscribe(List.of("notifications"));
//            kafkaTemplate.send("notifications", "user1", "New notification");
//
//            var received = KafkaTestUtils.getSingleRecord(consumer, "notifications", Duration.ofSeconds(5));
//
//            assertThat(received.key()).isEqualTo("user1");
//            assertThat(received.value()).isEqualTo("New notification");
//        }
//    }
//
//    @TestConfiguration
//    static class KafkaTestConfig {
//
//        @Bean
//        public ConsumerFactory<String, String> consumerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
//            Map<String, Object> props = KafkaTestUtils.consumerProps(
//                    "testGroup",
//                    "true",
//                    embeddedKafkaBroker
//            );
//            return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());
//        }
//
//        @Bean
//        public ProducerFactory<String, String> producerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
//            Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
//            props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//            props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//            return new DefaultKafkaProducerFactory<>(props);
//        }
//
//        @Bean
//        public KafkaTemplate<String, String> kafkaTemplate(EmbeddedKafkaBroker embeddedKafkaBroker) {
//            return new KafkaTemplate<>(producerFactory(embeddedKafkaBroker));
//        }
//    }
//}
