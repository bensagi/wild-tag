package management.controllers;

import applications.Application;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import management.services.CloudStorageService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class SpringbootTestBase {

  @Autowired
  DataSource dataSource;
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

  @AfterEach
  public void clearDatabase() {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(
          "SELECT tablename FROM pg_tables WHERE schemaname='public'");
      List<String> tables = new ArrayList<>();
      while (rs.next()) {
        tables.add(rs.getString(1));
      }
      for (String table : tables) {
        stmt.execute("TRUNCATE TABLE " + table + " CASCADE");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
