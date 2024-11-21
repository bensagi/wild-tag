package management.services;

import static management.entities.images.ImageStatus.VALIDATED;

import com.wild_tag.model.CoordinatesApi;
import com.wild_tag.model.ImageApi;
import com.wild_tag.model.ImageStatusApi;
import com.wild_tag.model.ImagesBucketApi;
import java.util.List;
import java.util.UUID;
import management.entities.images.CoordinateDB;
import management.entities.images.ImageDB;
import management.entities.images.ImageStatus;
import management.entities.users.UserDB;
import management.repositories.ImagesRepository;
import management.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

  private Logger logger = LoggerFactory.getLogger(ImageService.class);

  private CloudStorageService cloudStorageService;

  private ImagesRepository imagesRepository;

  private UserRepository usersRepository;

  public ImageService(CloudStorageService cloudStorageService, ImagesRepository imagesRepository, UserRepository usersRepository) {
    this.cloudStorageService = cloudStorageService;
    this.imagesRepository = imagesRepository;
    this.usersRepository = usersRepository;
  }

  public void loadImages(ImagesBucketApi imagesBucketApi) {
    Thread thread = new Thread(() -> loadImagesBackground(imagesBucketApi));
    thread.start();
  }

  public void loadImagesBackground(ImagesBucketApi imagesBucketApi) {
    logger.info("Loading images from bucket: {}", imagesBucketApi.getBucketName());
    List<String> images = cloudStorageService.listBucket(imagesBucketApi.getBucketName());
    images.forEach(imagePath -> {
      ImageDB imageDB = new ImageDB();
      imageDB.setGcsFullPath(imagePath);
      imagesRepository.save(imageDB);
    });
    logger.info(images.size() + " images loaded successfully");
  }

  public List<ImageApi> getImages() {
    return imagesRepository.findAll().stream().map(this::convertToImageApi).toList();
  }

  public void tagImage(ImageApi imageApi, String userEmail) {
    ImageDB imageDB = imagesRepository.findById(UUID.fromString(imageApi.getId())).orElseThrow();
    UserDB userDB = usersRepository.findByEmail(userEmail).orElseThrow();
    imageDB.setTaggerUser(userDB);
    imageDB.setStatus(ImageStatus.TAGGED);
    imageDB.setCoordinates(imageApi.getCoordinates().stream().map(this::convertCoordinatesApiToCoordinatesDB).toList());
    imagesRepository.save(imageDB);
  }

  public void validateImage(String imageId, String userEmail) {
    ImageDB imageDB = imagesRepository.findById(UUID.fromString(imageId)).orElseThrow();
    UserDB validatorUser = usersRepository.findByEmail(userEmail).orElseThrow();
    imageDB.setValidatorUser(validatorUser);
    imageDB.setStatus(VALIDATED);
    imagesRepository.save(imageDB);
  }

  private ImageApi convertToImageApi(ImageDB imageDB) {
    return new ImageApi().id(imageDB.getId().toString()).
        status(ImageStatusApi.fromValue(imageDB.getStatus().name())).
        coordinates(imageDB.getCoordinates().stream().map(this::convertCoordinatesDBToCoordinatesApi).toList()).
        validatorUserId(imageDB.getValidatorUser() == null ? "unassigned" : imageDB.getValidatorUser().getEmail()).
        taggerUserId(imageDB.getTaggerUser() == null ? "unassigned" : imageDB.getTaggerUser().getEmail());

  }

  private CoordinatesApi convertCoordinatesDBToCoordinatesApi(CoordinateDB coordinates) {
    return new CoordinatesApi().animalId(coordinates.getAnimalId()).
        xCenter(coordinates.getX_center()).
        yCenter(coordinates.getY_center()).
        width(coordinates.getWidth()).
        height(coordinates.getHeight());
  }

  private CoordinateDB convertCoordinatesApiToCoordinatesDB(CoordinatesApi coordinatesApi) {
    return new CoordinateDB().setAnimalId(coordinatesApi.getAnimalId()).
        setX_center(coordinatesApi.getxCenter()).
        setY_center(coordinatesApi.getyCenter()).
        setWidth(coordinatesApi.getWidth()).
        setHeight(coordinatesApi.getHeight());
  }
}
