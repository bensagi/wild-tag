package management.services;

import com.wild_tag.model.CategoriesApi;import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import management.entities.CategoriesDB;
import management.repositories.CategoriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class CategoriesService {
  private final CategoriesRepository categoriesRepository;
  private final ObjectMapper objectMapper;

  public CategoriesService(CategoriesRepository categoriesRepository) {
    this.categoriesRepository = categoriesRepository;
    this.objectMapper = new ObjectMapper();
  }

  public CategoriesApi getCategories() throws JsonProcessingException {
    List<CategoriesDB> categories = categoriesRepository.findAll();
    if (CollectionUtils.isEmpty(categories)) {
      return new CategoriesApi();
    }

    Map<String, String> entries = objectMapper.readValue(categories.get(0).getCategories(), Map.class);
    return new CategoriesApi().entries(entries);

  }

  public void setCategories(Map<String, String> categories) throws JsonProcessingException {
    List<CategoriesDB> categoriesList = categoriesRepository.findAll();
    CategoriesDB categoriesDB = CollectionUtils.isEmpty(categoriesList) ? new CategoriesDB() : categoriesList.get(0);

    String categoriesJson = objectMapper.writeValueAsString(categories);
    categoriesDB.setCategories(categoriesJson);
    categoriesRepository.save(categoriesDB);
  }
}