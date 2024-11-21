package management.repositories;

import java.util.UUID;
import management.entities.users.UserDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserDB, UUID> {

  Optional<UserDB> findByEmail(String userEmail);
}
