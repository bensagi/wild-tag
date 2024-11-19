package management.entities.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import management.entities.AbstractEntity;
import management.enums.UserRole;

@Entity
@Table(name = "users")
public class UserDB extends AbstractEntity {

  @Column(name = "name", columnDefinition = "text")
  private String name;

  @Column(columnDefinition = "text")
  private String email;

  @Column(columnDefinition = "text")
  @Enumerated(EnumType.STRING)
  private UserRole role;

  public UserDB() {
    super();
  }

  public UserDB(String name, String email, UserRole role) {
    super();
    this.name = name;
    this.email = email;
    this.role = role;
  }

  public String getName() {
    return name;
  }

  public UserDB setName(String name) {
    this.name = name;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public UserDB setEmail(String email) {
    this.email = email;
    return this;
  }

    public UserRole getRole() {
        return role;
    }

  public void setRole(UserRole role) {
    this.role = role;
  }
}
