package management.controllers;

import management.entities.users.UserDB;
import management.services.UsersService;
import org.springframework.http.HttpStatus;
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

  @GetMapping("/users")
  public ResponseEntity<List<UserDB>> getUsers() {
    List<UserDB> users = usersService.getUsers();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  @GetMapping("/users/{userEmail}")
  public ResponseEntity<UserDB> getUserByEmail(String userEmail) {
    UserDB user = usersService.getUserByEmail(userEmail);
    return new ResponseEntity<>(user, HttpStatus.OK);
  }

  @PostMapping("/users")
  public ResponseEntity<UserDB> createUser(UserDB user) {
    UserDB userDB = usersService.createUser(user);
    return new ResponseEntity<>(userDB, HttpStatus.CREATED);
  }

  @PostMapping("/users/bulk")
  public ResponseEntity<List<UserDB>> createUsers(List<UserDB> users) {
    List<UserDB> createdUsers = usersService.createUsers(users);
    return new ResponseEntity<>(createdUsers, HttpStatus.CREATED);
  }

  @PostMapping("/users/{userEmail}")
  public ResponseEntity<UserDB> updateUserByEmail(String userEmail, UserDB user) {
    UserDB updatedUser = usersService.updateUserByEmail(userEmail, user);
    return new ResponseEntity<>(updatedUser, HttpStatus.OK);
  }

  @DeleteMapping("/users/{userEmail}")
  public ResponseEntity<Void> deleteUserByEmail(String userEmail) {
    usersService.deleteUserByEmail(userEmail);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
