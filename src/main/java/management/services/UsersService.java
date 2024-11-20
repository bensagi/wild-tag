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
    UserDB currUserDB = getUserByEmail(userEmail);
    UserDB newUserDB = new UserDB(user.getName(), user.getEmail(), user.getRole());
    userRepository.deleteById(currUserDB.getId());
    return userRepository.save(newUserDB);
  }

  public void deleteUserByEmail(String userEmail) {
    UserDB user = getUserByEmail(userEmail);
    userRepository.delete(user);
  }
}
