package management.repositories;

import java.util.Optional;
import management.entities.CategoriesDB;
import management.entities.users.UserDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoriesRepository extends JpaRepository<CategoriesDB, String> {
}
