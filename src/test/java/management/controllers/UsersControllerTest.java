package management.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import applications.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wild_tag.model.RoleApi;
import com.wild_tag.model.UserApi;
import java.nio.charset.StandardCharsets;
import java.util.List;
import management.repositories.ImagesRepository;
import management.repositories.UserRepository;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class UsersControllerTest {

  @Autowired
  private UsersController usersController;
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private UserRepository usersRepository;
  ObjectMapper objectMapper = new ObjectMapper();
  @Autowired
  ImagesRepository imageRepository;

  @BeforeEach
  public void setup() {
    this.mockMvc = webAppContextSetup(webApplicationContext)
        .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
        .build();
    imageRepository.deleteAll();
    usersRepository.deleteAll();
  }

  @Test
  public void test() {
    assertThat(usersController).isNotNull();
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void getUsersTest() throws Exception {
    String response = mockMvc.perform(get("/users")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    List users = objectMapper.readValue(response, List.class);
    assertThat(users).isNotNull();
    System.out.println(users);
    assertThat(users.size()).isEqualTo(0);
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void createBulkUsers() throws Exception {
    UserApi user = new UserApi().email("user@email.com").name("user").role(RoleApi.USER);
    UserApi user_2 = new UserApi().email("user2@email.com").name("user2").role(RoleApi.USER);
    UserApi user_3 = new UserApi().email("user3@email.com").name("user3").role(RoleApi.USER);
    List<UserApi> users = List.of(user, user_2, user_3);
    mockMvc.perform(post("/users/bulk")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(users)))
        .andExpect(status().isCreated());
    String response = mockMvc.perform(get("/users")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andReturn().getResponse().getContentAsString();
    List usersResponse = objectMapper.readValue(response, List.class);
    assertThat(usersResponse).isNotNull();
    System.out.println(usersResponse);
    assertThat(users.size()).isEqualTo(3);
  }
}
