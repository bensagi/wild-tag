package management.repositories;

import applications.Application;
import java.util.ArrayList;
import java.util.Optional;
import management.entities.images.ImageDB;
import management.entities.users.UserDB;
import management.enums.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
public class ImageRepositoryTest {

  @Autowired
  UserRepository userRepository;

  @Autowired
  ImagesRepository imageRepository;

  @Test
  public void t() {
    UserDB u1 = userRepository.save(new UserDB("name", "abc@google.com", UserRole.ADMIN));
    UserDB u2 = userRepository.save(new UserDB("name", "efg@google.com", UserRole.ADMIN));
    ImageDB image = new ImageDB("gcsPath", null, u1, u2, new ArrayList<>(), "taggedPath");
    image = imageRepository.save(image);

    Optional<ImageDB> imageOptional = imageRepository.findById(image.getId());
    ImageDB imageDB = imageOptional.orElseThrow();
    Assertions.assertEquals("gcsPath", imageDB.getGcsFullPath());
    Assertions.assertEquals("taggedPath", imageDB.getGcsTaggedPath());
    Assertions.assertEquals(u1.getEmail(), imageDB.getTaggerUser().getEmail());
  }
}
