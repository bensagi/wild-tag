package management.repositories;

import applications.Application;
import java.util.Collections;
import java.util.Set;
import management.entities.users.UserDB;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest(classes = Application.class)
class UserRepositoryTest extends DbTestSimulator {

  @Autowired
  UserRepository userRepository;

  @Test
  public void t() {
    UserDB u = userRepository.save(new UserDB("name", "abc@google.com", "password"));
    UserDB udb = userRepository.findById(u.getId()).orElseThrow();

    Assertions.assertEquals("name", udb.name());
  }
}