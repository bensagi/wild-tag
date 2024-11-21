package management.services;

import static management.entities.converters.UserConverter.convertUserDBToUser;
import static management.entities.converters.UserConverter.convertUserToUserDB;

import com.wild_tag.model.UserApi;
import java.util.UUID;
import java.util.stream.Collectors;
import management.entities.converters.UserConverter;
import management.entities.users.UserDB;
import management.enums.UserRole;
import management.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

  private final UserRepository userRepository;

  public UsersService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<UserApi> getUsers() {
    List<UserDB> userDBS = userRepository.findAll();
    return userDBS.stream().map(UserConverter::convertUserDBToUser).collect(Collectors.toList());
  }

  public UserApi getUserByEmailByUserApi(String userEmail) {
    UserDB userDB = userRepository.findByEmail(userEmail).orElseThrow();
    return convertUserDBToUser(userDB);
  }

  public UserDB getUserByEmailByUserDb(String userEmail) {
    return userRepository.findByEmail(userEmail).orElseThrow();
  }

  public UserApi createUser(UserApi user) {
    UserDB userDB = userRepository.save(convertUserToUserDB(user));
    return convertUserDBToUser(userDB);
  }

  public List<UserApi> createUsers(List<UserApi> users) {
    List<UserDB> usersDB = users.stream().map(UserConverter::convertUserToUserDB).toList();
    return userRepository.saveAll(usersDB).stream().map(UserConverter::convertUserDBToUser).toList();
  }

  public UserApi updateUserByEmail(String userEmail, UserApi user) {
    UserDB currUserDB = getUserByEmailByUserDb(userEmail);
    UserDB newUserDB = new UserDB(user.getName(), user.getEmail(), UserRole.valueOf(user.getRole().name()));
    userRepository.deleteById(currUserDB.getId());
    newUserDB = userRepository.save(newUserDB);
    return convertUserDBToUser(newUserDB);
  }

  public void deleteUserByEmail(String userEmail) {
    userRepository.delete(getUserByEmailByUserDb(userEmail));
  }
}
