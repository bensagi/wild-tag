package applications;

import management.services.ImagesProcessorNATSSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "management")
@EnableJpaRepositories(basePackages = "management.repositories")
@EntityScan(basePackages = "management.entities")
public class ImageMessagesProcessor implements CommandLineRunner {

  @Autowired
  private ImagesProcessorNATSSubscriber imageMessageProcessorService;

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(ImageMessagesProcessor.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    application.run(args);
  }

  @Override
  public void run(String... args) throws Exception {
    imageMessageProcessorService.subscriberTopic();
  }
}
