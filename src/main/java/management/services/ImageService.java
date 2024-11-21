package management.services;

import com.wild_tag.model.ImagesBucketApi;
import java.util.List;
import management.entities.images.ImageDB;
import management.repositories.ImagesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

  private Logger logger = LoggerFactory.getLogger(ImageService.class);

  @Autowired
  private CloudStorageService cloudStorageService;

  @Autowired
  private ImagesRepository imagesRepository;

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

  public List<ImageDB> getImages() {
    return imagesRepository.findAll();
  }
}
