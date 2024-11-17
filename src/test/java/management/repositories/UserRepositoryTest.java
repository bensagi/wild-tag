package management.repositories;

import applications.Application;
import management.entities.users.UserDB;
import management.enums.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = Application.class)
class UserRepositoryTest extends DbTestSimulator {

  @Autowired
  UserRepository userRepository;

  @Test
  public void t() {
    UserDB u = userRepository.save(new UserDB("name", "abc@google.com", UserRole.ADMIN));
    UserDB udb = userRepository.findById(u.getId()).orElseThrow();

    Assertions.assertEquals("name", udb.getName());
  }
}
