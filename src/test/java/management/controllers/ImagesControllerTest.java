package management.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wild_tag.model.CategoriesApi;
import com.wild_tag.model.ImageStatusApi;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import management.entities.images.CoordinateDB;
import management.entities.images.ImageDB;
import management.entities.images.ImageStatus;
import management.entities.users.UserDB;
import management.enums.UserRole;
import management.repositories.ImagesRepository;
import management.services.ImageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
@AutoConfigureMockMvc
@Sql(scripts = "/setup-test-images-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ImagesControllerTest extends SpringbootTestBase {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Autowired
  private ImageService imageService;
  @Autowired
  ImagesRepository imageRepository;

  protected ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
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

  @Test
  @WithMockUser
  public void testGetNextImage_differentUserValidate() throws Exception {
    MvcResult result = mockMvc.perform(get("/images/next_task").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.USER), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()))))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();

    ImageApi image = objectMapper.readValue(result.getResponse().getContentAsString(), ImageApi.class);
    CoordinatesApi coordinates = new CoordinatesApi().animalId("1").yCenter(0.5).xCenter(0.5).height(0.5).width(0.5);
    image.coordinates(Collections.singletonList(coordinates));

    mockMvc.perform(put("/images/tag").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.ADMIN), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.ADMIN.name()))))
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(image)))
        .andExpect(status().isNoContent());

    result = mockMvc.perform(get("/images/next_task").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test2", "test2@email.com", UserRole.USER), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()))))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();

    ImageApi image2 = objectMapper.readValue(result.getResponse().getContentAsString(), ImageApi.class);

    Assertions.assertEquals(image.getId(), image2.getId());
    Assertions.assertEquals(ImageStatusApi.TAGGED, image2.getStatus());
  }

  @Test
  @WithMockUser
  public void testGetNextImage_imageInProgress() throws Exception {
    MvcResult result = mockMvc.perform(get("/images/next_task").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.USER), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()))))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();

    ImageApi image = objectMapper.readValue(result.getResponse().getContentAsString(), ImageApi.class);
    CoordinatesApi coordinates = new CoordinatesApi().animalId("1").yCenter(0.5).xCenter(0.5).height(0.5).width(0.5);
    image.coordinates(Collections.singletonList(coordinates));

    result = mockMvc.perform(get("/images/next_task").principal(
                new UsernamePasswordAuthenticationToken(new UserDB("test2", "test2@email.com", UserRole.USER), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()))))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();

    ImageApi image2 = objectMapper.readValue(result.getResponse().getContentAsString(), ImageApi.class);

    Assertions.assertNotEquals(image.getId(), image2.getId());
    Assertions.assertEquals(ImageStatusApi.PENDING, image2.getStatus());
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void testReportGeneration() throws Exception {

    String expected = """
        folder_name,jpg_name,jpg_date,jpg_time,1,2,3
        HAYUN 14.8.24-22.9.24,IMAG0001_HAYUN 14.8.24-22.9.24.jpg,14/08/2024,11:20:04,1,0,0
        HAYUN 14.8.24-22.9.24,IMAG0002_HAYUN 14.8.24-22.9.24.jpg,15/08/2024,3:23:14,0,2,0
        """;

    setCategories();

    ImageDB image1 = new ImageDB(
        "asd", ImageStatus.VALIDATED, null, null, List.of(new CoordinateDB("1", 0.1, 0.2, 0.3, 0.4)), "asd");
    image1.setJpgDate("14/08/2024");
    image1.setFolder("HAYUN 14.8.24-22.9.24");
    image1.setJpgName("IMAG0001_HAYUN 14.8.24-22.9.24.jpg");
    image1.setJpgTime("11:20:04");
    imageRepository.save(image1);

    ImageDB image2 = new ImageDB(
        "asd", ImageStatus.TRAINABLE, null, null, List.of(
            new CoordinateDB("2", 0.1, 0.2, 0.3, 0.4),
            new CoordinateDB("2", 0.1, 0.2, 0.3, 0.4)), "asd");
    image2.setJpgDate("15/08/2024");
    image2.setFolder("HAYUN 14.8.24-22.9.24");
    image2.setJpgName("IMAG0002_HAYUN 14.8.24-22.9.24.jpg");
    image2.setJpgTime("3:23:14");
    imageRepository.save(image2);


    String result = mockMvc.perform(get("/images/downloadCsv").principal(
            new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.ADMIN), null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.ADMIN.name())))))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/csv")).andReturn().getResponse().getContentAsString();

    System.out.println(result);
    Assertions.assertEquals(expected, result);

  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  public void testReportGeneration_imageWithNoMetaData() throws Exception {
    setCategories();

    String expected = "folder_name,jpg_name,jpg_date,jpg_time,1,2,3\n";

    ImageDB image1 = new ImageDB(
        "asd", ImageStatus.VALIDATED, null, null, List.of(new CoordinateDB("1", 0.1, 0.2, 0.3, 0.4)), "asd");
    imageRepository.save(image1);

    String result = mockMvc.perform(get("/images/downloadCsv").principal(
            new UsernamePasswordAuthenticationToken(new UserDB("test", "test@email.com", UserRole.ADMIN), null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + UserRole.ADMIN.name())))))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/csv")).andReturn().getResponse().getContentAsString();

    Assertions.assertEquals(expected, result);
  }

  private void setCategories() throws Exception {
    CategoriesApi newCategories = new CategoriesApi();
    Map<String, String> entries = new HashMap<>();
    entries.put("1", "יחמור");
    entries.put("2", "נאור");
    entries.put("3", "כלום");
    newCategories.setEntries(entries);

    mockMvc.perform(put("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCategories)))
        .andExpect(status().isOk())
        .andExpect(content().string("Categories updated successfully."));
  }
}
