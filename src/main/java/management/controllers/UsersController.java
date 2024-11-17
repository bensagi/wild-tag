package management.controllers;

import management.entities.users.UserDB;
import management.enums.UserRole;
import management.enums.UserRole.UserRoleNames;
import management.services.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
public class UsersController {

  private final UsersService usersService;

  public UsersController(UsersService usersService) {
    this.usersService = usersService;
  }

  @Secured({UserRoleNames.ADMIN_ROLE, UserRoleNames.GLOBAL_ADMIN_ROLE})
  @GetMapping("/users")
  public ResponseEntity<List<UserDB>> getUsers() {
    List<UserDB> users = usersService.getUsers();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  @Secured({UserRoleNames.ADMIN_ROLE, UserRoleNames.GLOBAL_ADMIN_ROLE})
  @GetMapping("/users/{userEmail}")
  public ResponseEntity<UserDB> getUserByEmail(String userEmail) {
    UserDB user = usersService.getUserByEmail(userEmail);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @Secured(UserRoleNames.GLOBAL_ADMIN_ROLE)
  @PostMapping("/users/admin")
  public ResponseEntity<UserDB> createAdmin(UserDB user) {
    if (!isUserAdmin(user)) {
      throw new IllegalArgumentException("User must have the role of an admin");
    }
    UserDB userDB = usersService.createUser(user);
    return new ResponseEntity<>(userDB, HttpStatus.CREATED);
  }

  @Secured({UserRoleNames.ADMIN_ROLE, UserRoleNames.GLOBAL_ADMIN_ROLE})
  @PostMapping("/users")
  public ResponseEntity<UserDB> createUser(UserDB user) {
    if (isUserAdmin(user)) {
      throw new IllegalArgumentException("User cannot have the role of an admin");
    }
    UserDB userDB = usersService.createUser(user);
    return new ResponseEntity<>(userDB, HttpStatus.CREATED);
  }

  @Secured({UserRoleNames.ADMIN_ROLE, UserRoleNames.GLOBAL_ADMIN_ROLE})
  @PostMapping("/users/bulk")
  public ResponseEntity<List<UserDB>> createUsers(List<UserDB> users) {
    if (users.stream().anyMatch(this::isUserAdmin)) {
      throw new IllegalArgumentException("Users cannot have the role of an admin");
    }
    List<UserDB> createdUsers = usersService.createUsers(users);
    return new ResponseEntity<>(createdUsers, HttpStatus.CREATED);
  }

  @Secured({UserRoleNames.ADMIN_ROLE, UserRoleNames.GLOBAL_ADMIN_ROLE})
  @PostMapping("/users/{userEmail}")
  public ResponseEntity<UserDB> updateUserByEmail(String userEmail, UserDB user) {
    UserDB updatedUser = usersService.updateUserByEmail(userEmail, user);
    return new ResponseEntity<>(updatedUser, HttpStatus.OK);
  }

  @Secured({UserRoleNames.ADMIN_ROLE, UserRoleNames.GLOBAL_ADMIN_ROLE})
  @DeleteMapping("/users/{userEmail}")
  public ResponseEntity<Void> deleteUserByEmail(String userEmail) {
    usersService.deleteUserByEmail(userEmail);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  private boolean isUserAdmin(UserDB user) {
    return user.getRole().equals(UserRole.ADMIN) || user.getRole().equals(UserRole.GLOBAL_ADMIN);
  }
}
