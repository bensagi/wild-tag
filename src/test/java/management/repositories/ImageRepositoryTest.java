package management.repositories;

import applications.Application;
import java.util.Collections;
import java.util.Optional;
import management.controllers.NATSTestSimulator;
import management.entities.images.CoordinateDB;
import management.entities.images.ImageDB;
import management.entities.images.ImageStatus;
import management.entities.users.UserDB;
import management.enums.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
public class ImageRepositoryTest extends NATSTestSimulator {

  @Autowired
  UserRepository userRepository;

  @Autowired
  ImagesRepository imageRepository;

  @Test
  public void t() {
    UserDB u1 = userRepository.save(new UserDB("name", "abc@google.com", UserRole.ADMIN));
    UserDB u2 = userRepository.save(new UserDB("name", "efg@google.com", UserRole.ADMIN));
    CoordinateDB coordinateDB = new CoordinateDB("1", 0.67, 0.5, 0.7, 0.3);
    ImageDB image = new ImageDB("gcsPath", ImageStatus.PENDING, u1, u2, Collections.singletonList(coordinateDB), "taggedPath");
    image = imageRepository.save(image);

    Optional<ImageDB> imageOptional = imageRepository.findById(image.getId());
    ImageDB imageDB = imageOptional.orElseThrow();
    Assertions.assertEquals("gcsPath", imageDB.getGcsFullPath());
    Assertions.assertEquals("taggedPath", imageDB.getGcsTaggedPath());
    Assertions.assertEquals(u1.getEmail(), imageDB.getTaggerUser().getEmail());
  }
}
