package management.repositories;

import management.controllers.SpringbootTestBase;
import management.entities.users.UserDB;
import management.enums.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserRepositoryTest extends SpringbootTestBase {

  @Autowired
  UserRepository userRepository;

  @Test
  public void t() {
    UserDB u = userRepository.save(new UserDB("name", "abc@google.com", UserRole.ADMIN));
    UserDB udb = userRepository.findById(u.getId()).orElseThrow();

    Assertions.assertEquals("name", udb.getName());
  }
}
