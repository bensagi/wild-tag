package management.configuration;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NATSConfig {

  @Value("${job.nats.uri}")
  private String natsUri;

  @Bean
  public Connection getNATSConnection() throws Exception {
    Options options = Options.builder()
        .server(natsUri)
        .build();
    return Nats.connect(options);
  }
}
