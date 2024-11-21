package management.repositories;

import java.util.UUID;
import management.entities.images.ImageDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagesRepository extends JpaRepository<ImageDB, UUID> {

}
