package management.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wild_tag.model.ImagesBucketApi;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImagesProcessorNATSSubscriber {

  @Value("${job.nats.imageProcessingTopic}")
  private String topic;

  @Autowired
  private Connection connection;

  @Autowired
  private ImageService imageService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @PostConstruct
  public void subscriberTopic() {
    Dispatcher dispatcher = connection.createDispatcher();
    dispatcher.subscribe(topic, message -> {
      String messageData = new String(message.getData(), StandardCharsets.UTF_8);
      ImagesBucketApi imagesBucketApi = null;
      try {
        imagesBucketApi = objectMapper.readValue(messageData, ImagesBucketApi.class);
        imageService.loadImagesBackground(imagesBucketApi);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
