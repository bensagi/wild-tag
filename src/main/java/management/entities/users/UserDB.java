package management.entities.users;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import management.entities.AbstractEntity;
import org.springframework.util.CollectionUtils;

@Entity
@Table(name = "users")
public class UserDB extends AbstractEntity {

  @Column(name = "name", columnDefinition = "text")
  private String name;

  @Column(columnDefinition = "text")
  private String email;

  @Column(columnDefinition = "text")
  private String password;

  public UserDB() {
    super();
  }

  public UserDB(String name, String email, String password) {
    super();
    this.name = name;
    this.email = email;
    this.password = password;
  }

  public String name() {
    return name;
  }

  public UserDB setName(String name) {
    this.name = name;
    return this;
  }

  public String email() {
    return email;
  }

  public UserDB setEmail(String email) {
    this.email = email;
    return this;
  }

  public String password() {
    return password;
  }

  public UserDB setPassword(String password) {
    this.password = password;
    return this;
  }
}
