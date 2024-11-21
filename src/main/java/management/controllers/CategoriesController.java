package management.controllers;

import com.wild_tag.model.CategoriesApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import management.enums.UserRole.UserRoleNames;
import management.services.CategoriesService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/categories")
public class CategoriesController {

  private final CategoriesService categoriesService;

  public CategoriesController(CategoriesService categoriesService) {
    this.categoriesService = categoriesService;
  }

  // GET /categories - Returns a JSON list of categories
  @GetMapping
  @Secured({UserRoleNames.ADMIN_ROLE, UserRoleNames.USER_ROLE})
  public ResponseEntity<CategoriesApi> getCategories() throws JsonProcessingException {
    return new ResponseEntity<>(categoriesService.getCategories(), HttpStatus.OK);
  }

  // PUT /categories - Overrides the existing saved JSON list
  @PutMapping
  @Secured(UserRoleNames.ADMIN_ROLE)
  public ResponseEntity<String> putCategories(@RequestBody CategoriesApi newCategories) throws JsonProcessingException {
    if (newCategories != null) {
      categoriesService.setCategories(newCategories.getEntries());
      return new ResponseEntity<>("Categories updated successfully.", HttpStatus.OK);
    } else {
      return new ResponseEntity<>("Invalid data format.", HttpStatus.BAD_REQUEST);
    }
  }
}