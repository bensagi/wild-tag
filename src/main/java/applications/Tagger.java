package applications;

import java.util.List;
import management.entities.images.ImageDB;
import management.services.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "management")
@EnableJpaRepositories(basePackages = "management.repositories")
@EntityScan(basePackages = "management.entities")
public class Tagger implements CommandLineRunner {

  private Logger logger = LoggerFactory.getLogger(Tagger.class);

  @Autowired ImageService imageService;

  @Value("${tagger.batchSize:100}")
  private int limit;

  public static void main(String[] args) {
    SpringApplication.run(Tagger.class, args);
    System.exit(0);
  }

  @Override
  public void run(String... args) throws Exception {

    int handled = 0;

    while (true) {
      List<ImageDB> images = imageService.getValidatedImages(limit);
      logger.debug("found {} images to build tag", images.size());
      if (images.isEmpty()) {
        logger.info("tagging proces finished. handled {} images", handled);
        return;
      }

      for (ImageDB image : images) {
        try {
          imageService.buildImageTag(image);
          logger.debug("image {} handled", image.getId());
          handled++;
        }
        catch (Exception e) {
          logger.error("failed to tag image {}", image.getId(), e);
        }
      }
      logger.info("handled {} images", handled);
    }
  }
}
