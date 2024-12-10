package management.services;

import static management.services.CloudStorageService.GS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

import com.wild_tag.model.ImagesBucketApi;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import management.entities.images.CoordinateDB;
import management.entities.images.GCSFileContent;
import management.entities.images.ImageDB;
import management.entities.images.ImageStatus;
import management.entities.users.UserDB;
import management.repositories.ImagesRepository;
import management.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class ImageServiceTest {

  public static final String ROOT = "root";
  public static final String DATA_SET_BUCKET = "dataSetBucket";
  ImagesRepository imagesRepository = Mockito.mock(ImagesRepository.class);
  UserRepository userRepository = Mockito.mock(UserRepository.class);
  CloudStorageService cloudStorageService = Mockito.mock(CloudStorageService.class);
  private final NATSPublisher natsPublisher = Mockito.mock(NATSPublisher.class);
  private final CategoriesService categoriesService = Mockito.mock(CategoriesService.class);


  ImageService imageService = new ImageService(cloudStorageService, imagesRepository,  userRepository, natsPublisher,
      categoriesService);

  @BeforeEach
  public void setUp() {
    imageService.setDataSetBucket(DATA_SET_BUCKET);
    imageService.setStorageRootDir(ROOT);
    imageService.setValidateRate(50);
  }

//    coordinates.add(new CoordinateDB("1", 0.1, 0.5, 0.3, 0.2));
//    coordinates.add(new CoordinateDB("2", 0.3, 0.2, 0.1, 0.1));
  @Test
  void testBuildImageTag_singleObj() throws IOException {

    UserDB tagger = new UserDB();
    UserDB validator = new UserDB();
    List<CoordinateDB> coordinates = new ArrayList<>();
    coordinates.add(new CoordinateDB("1", 0.5, 0.5, 0.3, 0.2));
    ImageDB imageDb = new ImageDB("gs://bucket/path/to/yahmor.png", ImageStatus.TAGGED, tagger, validator, coordinates, null);

    Mockito.when(cloudStorageService.copyObject(eq(imageDb.getGcsFullPath()), eq(DATA_SET_BUCKET), eq("root/dataset/images/train/yahmor.png"))).thenReturn("GS://dataSetBucket/ROOT/dataset/images/train/yahmor.png");

    imageService.buildImageTag(imageDb);

    ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);

    Mockito.verify(cloudStorageService, times(1)).uploadFileToStorage(
        eq(DATA_SET_BUCKET), eq("root/dataset/labels/train"), eq("yahmor.txt"),
        captor.capture()
    );
    assertTrue(captor.getValue().length > 10);
    Mockito.verify(cloudStorageService).copyObject(imageDb.getGcsFullPath(), DATA_SET_BUCKET, "root/dataset/images/train/yahmor.png");
    Mockito.verify(cloudStorageService, times(1)).deleteObject(imageDb.getGcsFullPath());
    ArgumentCaptor<ImageDB> imageCaptor = ArgumentCaptor.forClass(ImageDB.class);
    Mockito.verify(imagesRepository, times(1)).save(imageCaptor.capture());
    ImageDB savedImage = imageCaptor.getValue();

    assertEquals(ImageStatus.TRAINABLE, savedImage.getStatus());
    assertEquals(String.format("GS://%s/%s", DATA_SET_BUCKET, "ROOT/dataset/images/train/yahmor.png"), savedImage.getGcsTaggedPath());
  }

  @Test
  void testBuildImageTag_validate() throws IOException {

    UserDB tagger = new UserDB();
    UserDB validator = new UserDB();
    List<CoordinateDB> coordinates = new ArrayList<>();
    coordinates.add(new CoordinateDB("1", 0.5, 0.5, 0.3, 0.2));
    coordinates.add(new CoordinateDB("1", 0.1, 0.5, 0.3, 0.2));

    ImageDB imageDb = new ImageDB("gs://bucket/path/to/yahmor.png", ImageStatus.TAGGED, tagger, validator, coordinates, null);

    Mockito.when(cloudStorageService.copyObject(eq(imageDb.getGcsFullPath()), eq(DATA_SET_BUCKET), eq("root/dataset/images/val/yahmor.png"))).thenReturn("GS://dataSetBucket/ROOT/dataset/images/val/yahmor.png");

    imageService.buildImageTag(imageDb);

    ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);

    Mockito.verify(cloudStorageService, times(1)).uploadFileToStorage(
        eq(DATA_SET_BUCKET), eq("root/dataset/labels/val"), eq("yahmor.txt"),
        captor.capture()
    );
    assertTrue(captor.getValue().length > 10);
    Mockito.verify(cloudStorageService).copyObject(imageDb.getGcsFullPath(), DATA_SET_BUCKET, "root/dataset/images/val/yahmor.png");
    Mockito.verify(cloudStorageService, times(1)).deleteObject(imageDb.getGcsFullPath());
    ArgumentCaptor<ImageDB> imageCaptor = ArgumentCaptor.forClass(ImageDB.class);
    Mockito.verify(imagesRepository, times(1)).save(imageCaptor.capture());
    ImageDB savedImage = imageCaptor.getValue();

    assertEquals(ImageStatus.TRAINABLE, savedImage.getStatus());
    assertEquals(String.format("GS://%s/%s", DATA_SET_BUCKET, "ROOT/dataset/images/val/yahmor.png"), savedImage.getGcsTaggedPath());
  }

  @Test
  public void testLoadImagesBackground() throws IOException {

    String bucketName = "bucket/dir";
    List<String> filesList = new ArrayList<>();
    filesList.add(GS + "bucket/dir/image1.jpg");
    filesList.add(GS + "bucket/dir/image2.jpg");
    filesList.add(GS + "bucket/dir/metaData.csv");

    byte[] byteArray = Files.readAllBytes(Paths.get("src/test/resources/meta.csv"));

    Mockito.when(cloudStorageService.listFilesInPath(bucketName)).thenReturn(filesList);
    Mockito.when(cloudStorageService.getGCSFileContent(GS + "bucket/dir/metaData.csv")).thenReturn(new GCSFileContent(byteArray, "csv"));

    imageService.loadImagesBackground(new ImagesBucketApi().bucketName(bucketName));

    ArgumentCaptor<ImageDB> captor = ArgumentCaptor.forClass(ImageDB.class);

    Mockito.verify(imagesRepository, times(2)).save(captor.capture());

    ImageDB image1 = captor.getAllValues().get(0);

    assertEquals("image1.jpg", image1.getName());
    assertEquals("dir", image1.getFolder());
    assertEquals("11:20:04", image1.getTime());
    assertEquals("2024-08-14", image1.getDate());
    assertEquals(GS + "bucket/dir/image1.jpg", image1.getGcsFullPath());

    ImageDB image2 = captor.getAllValues().get(1);
    assertEquals("image2.jpg", image2.getName());
    assertEquals("dir", image2.getFolder());
    assertEquals("03:23:14", image2.getTime());
    assertEquals("2024-08-15", image2.getDate());
    assertEquals(GS + "bucket/dir/image2.jpg", image2.getGcsFullPath());
  }
}