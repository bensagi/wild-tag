package management.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import applications.Application;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import management.entities.users.UserDB;
import management.enums.UserRole;
import management.repositories.ImagesRepository;
import management.services.ImageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.wild_tag.model.ImageApi;
import com.wild_tag.model.CoordinatesApi;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Sql(scripts = "/setup-test-images-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ImagesControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired
  private ImageService imageService;
  @Autowired
  ImagesRepository imageRepository;

  protected ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//    imageRepository.deleteAll();
  }

  @Test
  @WithMockUser()
  public void testGetImages() throws Exception {
    MvcResult result = mockMvc.perform(get("/images").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.USER), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()))))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();
    List<ImageApi> images = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
    });
    // Add assertions here
    Assertions.assertEquals(5, images.size());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void testTagImage() throws Exception {
    MvcResult result = mockMvc.perform(get("/images").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.USER), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()))))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();
    List<ImageApi> images = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
    });

    ImageApi image = images.get(0);
    String imageId = image.getId();
    CoordinatesApi coordinates = new CoordinatesApi().animalId("1").yCenter(0.5).xCenter(0.5).height(0.5).width(0.5);
    image.coordinates(Collections.singletonList(coordinates));
    mockMvc.perform(put("/images/tag").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.ADMIN), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.ADMIN.name()))))
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(image)))
        .andExpect(status().isNoContent());

    result = mockMvc.perform(get("/images").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.USER), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()))))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();
    images = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
    });

    image = images.stream().filter(imageApi -> imageApi.getId().equals(imageId))
        .findFirst().get();
    Assertions.assertEquals(1, image.getCoordinates().size());
    Assertions.assertEquals("test@email.com", image.getTaggerUserId());

    mockMvc.perform(put("/images/" + imageId + "/validate").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.ADMIN), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.ADMIN.name()))))
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(image)))
        .andExpect(status().isNoContent());

    result = mockMvc.perform(get("/images").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.USER), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()))))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();
    images = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
    });
    image = images.stream().filter(imageApi -> imageApi.getId().equals(imageId))
        .findFirst().get();
    Assertions.assertEquals("test@email.com", image.getValidatorUserId());
  }
}
