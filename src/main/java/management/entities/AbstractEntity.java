package management.entities;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.List;
import java.util.Objects;

@MappedSuperclass
public class AbstractEntity {

  @Id
  protected String id;

  protected void calcAndSetId(String... args) {
    List<String> argsList = List.of(args);
    StringBuilder stringBuilder = new StringBuilder();
    argsList.forEach(arg -> stringBuilder.append(arg).append("."));
    id = stringBuilder.toString();
  }

  public String getId() {
    return id;
  }

  public AbstractEntity setId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractEntity that = (AbstractEntity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
