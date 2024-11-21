package management.entities.converters;

import com.wild_tag.model.RoleApi;
import com.wild_tag.model.UserApi;
import management.entities.users.UserDB;
import management.enums.UserRole;

public class UserConverter {

  public static UserDB convertUserToUserDB(UserApi user) {
    return new UserDB(user.getName(), user.getEmail(), convertRoleApiToRole(user.getRole()));
  }

  public static UserApi convertUserDBToUser(UserDB userDB) {
    return new UserApi().name(userDB.getName()).email(userDB.getEmail()).role(convertRoleToRoleApi(userDB.getRole()));
  }

  public static RoleApi convertRoleToRoleApi(UserRole role) {
    return RoleApi.valueOf(role.name());
  }

  public static UserRole convertRoleApiToRole(RoleApi role) {
    return UserRole.valueOf(role.name());
  }
}
