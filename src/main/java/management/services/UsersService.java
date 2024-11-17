package management.services;

import management.entities.users.UserDB;
import management.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

  private final UserRepository userRepository;

  public UsersService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<UserDB> getUsers() {
    return userRepository.findAll();
  }
  
  public UserDB getUserByEmail(String userEmail) {
    return userRepository.findByEmail(userEmail).orElseThrow();
  }

  public UserDB createUser(UserDB user) {
    return userRepository.save(user);
  }

  public List<UserDB> createUsers(List<UserDB> users) {
    return userRepository.saveAll(users);
  }

  public UserDB updateUserByEmail(String userEmail, UserDB user) {
    UserDB userDB = getUserByEmail(userEmail);
    userDB.setName(user.getName());
    userDB.setEmail(user.getEmail());
    userDB.setRole(user.getRole());
    return userRepository.save(userDB);
  }

  public void deleteUserByEmail(String userEmail) {
    UserDB user = getUserByEmail(userEmail);
    userRepository.delete(user);
  }
}
