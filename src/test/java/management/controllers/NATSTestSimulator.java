package management.controllers;

import management.services.CloudStorageService;
import net.bytebuddy.utility.dispatcher.JavaDispatcher.Container;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public class NATSTestSimulator {

  static GenericContainer<?> natsContainer = new GenericContainer<>("nats:2.10-alpine")
      .withExposedPorts(4222);

  @MockBean
  private CloudStorageService dependencyServiceMock;

  @BeforeAll
  public static void startContainer() {
    natsContainer.start();
  }

  @DynamicPropertySource
  static void postgresqlProperties(DynamicPropertyRegistry registry) {
    Integer natsPort = natsContainer.getMappedPort(4222);
    registry.add("job.nats.uri", () -> "nats://localhost:" + natsPort);
  }

  @AfterAll
  public static void stopContainer() {
    natsContainer.stop();
  }
}
