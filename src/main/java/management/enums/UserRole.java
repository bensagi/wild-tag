package management.enums;

public enum UserRole {
  GLOBAL_ADMIN, ADMIN, USER, CONTRIBUTOR, VALIDATOR;

  public static class UserRoleNames {
    public static final String ADMIN_ROLE = "ROLE_ADMIN";
    public static final String USER_ROLE = "ROLE_USER";
    public static final String GLOBAL_ADMIN_ROLE = "ROLE_GLOBAL_ADMIN";
    public static final String CONTRIBUTOR_ROLE = "ROLE_CONTRIBUTOR";
    public static final String VALIDATOR_ROLE = "ROLE_VALIDATOR";
  }
}
