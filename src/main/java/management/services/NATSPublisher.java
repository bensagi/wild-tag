package management.services;

import io.nats.client.Connection;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NATSPublisher {

  private final Logger logger = LoggerFactory.getLogger(NATSPublisher.class);

  @Autowired
  private Connection connection;

  public void sendMessage(String topic, String message) {
    logger.info("Publishing message to NATS topic: {}", topic);
    connection.publish(topic, message.getBytes(StandardCharsets.UTF_8));
  }
}
