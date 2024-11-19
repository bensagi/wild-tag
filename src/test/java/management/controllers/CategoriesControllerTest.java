package management.controllers;

import applications.Application;
import com.example.model.CategoriesApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import management.services.CategoriesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CategoriesControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private CategoriesService categoriesService; // Assuming this is a Spring-managed bean

  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    this.mockMvc = webAppContextSetup(webApplicationContext)
        .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
        .build();
  }


  @Test
  @WithMockUser(roles = {"USER"})
  public void testGetCategories() throws Exception {
    // This is a simple test case assuming categoriesService.getCategories() returns non-null CategoriesApi
    mockMvc.perform(get("/categories")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
  }

  @Test
  @WithMockUser(roles = {"GLOBAL_ADMIN"})
  public void testPutCategories() throws Exception {
    CategoriesApi newCategories = new CategoriesApi(); // Assume a valid CategoriesApi object
    Map<String, String> entries = new HashMap<>();
    entries.put("1", "יחמור");
    entries.put("2", "נאור");
    newCategories.setEntries(entries);

    mockMvc.perform(put("/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newCategories)))
        .andExpect(status().isOk())
        .andExpect(content().string("Categories updated successfully."));

    String jsonResponse = mockMvc.perform(get("/categories")
            .characterEncoding("UTF-8")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    CategoriesApi categories = objectMapper.readValue(jsonResponse, CategoriesApi.class);

    System.out.println(categories);

    Assertions.assertTrue(categories.getEntries().get("1").equals("יחמור"));
    Assertions.assertTrue(categories.getEntries().get("2").equals("נאור"));
  }
}