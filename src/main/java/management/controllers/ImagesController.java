package management.controllers;

import com.wild_tag.model.ImageApi;
import com.wild_tag.model.ImageStatusApi;
import com.wild_tag.model.ImagesBucketApi;
import java.util.List;
import management.entities.images.ImageDB;
import management.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/images")
@RequestMapping("/images")
public class ImagesController {

  @Autowired
  private ImageService imageService;

  @PostMapping("/upload")
  public ResponseEntity<Void> uploadImage(@RequestBody ImagesBucketApi imagesBucketApi) {
    imageService.loadImages(imagesBucketApi);
    return ResponseEntity.ok().build();
  }

  @GetMapping()
  public ResponseEntity<List<ImageApi>> getImages() {
    List<ImageDB> images = imageService.getImages();
    List<ImageApi> imageApis = images.stream()
        .map(image -> new ImageApi().gcsFullPath(image.getGcsFullPath()).gcsTaggedPath(image.getGcsTaggedPath()).status(
            ImageStatusApi.valueOf(image.getStatus().name()))).toList();
    return new ResponseEntity<>(imageApis, HttpStatus.OK);
  }
}
