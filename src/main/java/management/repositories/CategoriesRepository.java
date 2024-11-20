package management.repositories;

import management.entities.CategoriesDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoriesRepository extends JpaRepository<CategoriesDB, String> {
}
