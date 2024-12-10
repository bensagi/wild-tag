package management.services;

import com.github.dockerjava.api.exception.BadRequestException;
import com.wild_tag.model.CategoriesApi;import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import management.entities.CategoriesDB;
import management.repositories.CategoriesRepository;
import org.jetbrains.annotations.NotNull;
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

  @NotNull
  public CategoriesApi getCategoriesOrThrow() throws JsonProcessingException {
    CategoriesApi categories = getCategories();
    if (CollectionUtils.isEmpty(categories.getEntries())) {
      throw new BadRequestException("no categories to manage by");
    }
    return categories;
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