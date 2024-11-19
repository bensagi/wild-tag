package management.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.sql.Timestamp;

@Entity(name = "categories")
public class CategoriesDB extends AbstractEntity {

  @Column(columnDefinition = "text")
  private String categories;

  public CategoriesDB() {
  }

  public CategoriesDB(String categories) {
    this.categories = categories;
  }

  public String getCategories() {
    return categories;
  }

  public CategoriesDB setCategories(String categories) {
    this.categories = categories;
    return this;
  }
}
