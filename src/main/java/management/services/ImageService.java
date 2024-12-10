package management.services;

import static management.entities.images.ImageStatus.PENDING;
import static management.entities.images.ImageStatus.TAGGED;
import static management.entities.images.ImageStatus.TRAINABLE;
import static management.entities.images.ImageStatus.VALIDATED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wild_tag.model.CategoriesApi;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wild_tag.model.CoordinatesApi;
import com.wild_tag.model.ImageApi;
import com.wild_tag.model.ImageStatusApi;
import com.wild_tag.model.ImagesBucketApi;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import management.entities.images.CoordinateDB;
import management.entities.images.GCSFileContent;
import management.entities.images.ImageDB;
import management.entities.images.ImageStatus;
import management.entities.users.UserDB;
import management.repositories.ImagesRepository;
import management.repositories.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

  public static final String DATASET = "dataset";
  public static final String LABELS = "labels";
  public static final String VAL = "val";
  public static final String TRAIN = "train";
  public static final String IMAGES = "images";
  private Logger logger = LoggerFactory.getLogger(ImageService.class);

  private static final String IMAGE_ASSIGN_LOCK = "image assign lock";


  @Value("${data_set_bucket:wild-tag-data-set}")
  private String dataSetBucket;

  @Value("${storage_root_dir:dev}")
  private String storageRootDir;

  @Value("${validate_rate:15}")
  private Integer validateRate;

  @Value("${csv_batch_size:1000}")
  private Integer csvBatchSize;


  private final Map<String, Integer> categoriesHistogram = new HashMap<>();

  private final CloudStorageService cloudStorageService;

  private final ImagesRepository imagesRepository;

  private final UserRepository usersRepository;

  private final NATSPublisher natsPublisher;

  private final ObjectMapper objectMapper;

  private final CategoriesService categoriesService;

  @Value("${job.nats.imageProcessingTopic}")
  private String topic;
  @Value("${imageValidationMinutes:30}")
  private int imageValidationMinutes;

  public ImageService(CloudStorageService cloudStorageService, ImagesRepository imagesRepository,
      UserRepository usersRepository, NATSPublisher natsPublisher, CategoriesService categoriesService) {
    this.cloudStorageService = cloudStorageService;
    this.imagesRepository = imagesRepository;
    this.usersRepository = usersRepository;
    this.natsPublisher = natsPublisher;
    this.categoriesService = categoriesService;
    this.objectMapper = new ObjectMapper();
  }

  public void setDataSetBucket(String dataSetBucket) {
    this.dataSetBucket = dataSetBucket;
  }

  public void setStorageRootDir(String storageRootDir) {
    this.storageRootDir = storageRootDir;
  }

  public void setValidateRate(Integer validateRate) {
    this.validateRate = validateRate;
  }

  public void loadImages(ImagesBucketApi imagesBucketApi) throws JsonProcessingException {
    natsPublisher.sendMessage(topic, objectMapper.writeValueAsString(imagesBucketApi));
  }

  public void loadImagesBackground(ImagesBucketApi imagesBucketApi) throws IOException {
    logger.info("Loading images from bucket: {}", imagesBucketApi.getBucketName());
    List<String> files = cloudStorageService.listFilesInPath(imagesBucketApi.getBucketName());

    Map<String, ImageMetaData> imageNameToMetaData = extractFolderMetaData(imagesBucketApi, files);

    files.forEach(imagePath -> insertImageToDB(imagePath, imageNameToMetaData));
    logger.info(files.size() + " images loaded successfully");
  }

  private Map<String, ImageMetaData> extractFolderMetaData(ImagesBucketApi imagesBucketApi, List<String> files)
      throws IOException {
    String csvUri = files.stream().filter(x -> x.endsWith(".csv")).findFirst()
        .orElseThrow(() -> new RuntimeException("csv not found in " + imagesBucketApi.getBucketName()));
    GCSFileContent csvContent = cloudStorageService.getGCSFileContent(csvUri);
    Map<String, ImageMetaData> imageNameToMetaData = loadCsvToMap(csvContent.getContent());
    files.remove(csvUri);
    return imageNameToMetaData;
  }

  private void insertImageToDB(String imagePath, Map<String, ImageMetaData> imageNameToMetaData) {
    ImageDB imageDB = new ImageDB();
    ImageMetaData imageMetaData = imageNameToMetaData.get(imagePath.replace("GS://", "").split("/", 2)[1]);
    if (imageMetaData == null) {
      logger.warn("missing image meta data for {}", imagePath);
      imageMetaData = new ImageMetaData(null, null, null, null);
    }
    imageDB.setGcsFullPath(imagePath);
    imageDB.setFolder(imageMetaData.folderName());
    imageDB.setJpgName(imageMetaData.jpgName());
    imageDB.setJpgTime(imageMetaData.jpgTime());
    imageDB.setJpgDate(imageMetaData.jpgDate());
    imagesRepository.save(imageDB);
  }

  public List<ImageApi> getImages() {
    return imagesRepository.findAll().stream().map(this::convertToImageApi).toList();
  }

  public void tagImage(ImageApi imageApi, String userEmail) {
    synchronized (IMAGE_ASSIGN_LOCK) {
      ImageDB imageDB = imagesRepository.findById(UUID.fromString(imageApi.getId())).orElseThrow();
      UserDB userDB = usersRepository.findByEmail(userEmail).orElseThrow();
      imageDB.setTaggerUser(userDB);
      imageDB.setStatus(ImageStatus.TAGGED);
      imageDB.setStartHandled(getMinTimestamp());
      imageDB.setCoordinates(
          imageApi.getCoordinates().stream().map(this::convertCoordinatesApiToCoordinatesDB).toList());
      imagesRepository.save(imageDB);
    }
  }

  public void validateImage(String imageId, String userEmail) {
    synchronized (IMAGE_ASSIGN_LOCK) {
      ImageDB imageDB = imagesRepository.findById(UUID.fromString(imageId)).orElseThrow();
      UserDB validatorUser = usersRepository.findByEmail(userEmail).orElseThrow();
      imageDB.setValidatorUser(validatorUser);
      imageDB.setStatus(VALIDATED);
      imageDB.setStartHandled(getMinTimestamp());
      imagesRepository.save(imageDB);
    }
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

  public List<ImageDB> getValidatedImages(int limit) {
    return imagesRepository.getByStatus(ImageStatus.VALIDATED, PageRequest.of(0, limit));
  }

  public void buildImageTag(ImageDB imageDB) throws IOException {
    String yoloImagePath = createYoloFiles(imageDB);
    imageDB.setGcsTaggedPath(yoloImagePath);
    imageDB.setStatus(ImageStatus.TRAINABLE);
    imagesRepository.save(imageDB);
  }

  private String createYoloFiles(ImageDB imageDB) throws IOException {
    List<CoordinateDB> coordinates = imageDB.getCoordinates(); // Assuming a getter for coordinates exists

    boolean isValidate = isValidate(coordinates);
    String[] parts = imageDB.getGcsFullPath().split("/");
    String objectName = parts[parts.length - 1];

    uploadYoloText(imageDB, coordinates, isValidate, objectName);

    return moveImageToDataSetPath(isValidate, imageDB.getGcsFullPath(), objectName);
  }

  private void uploadYoloText(ImageDB imageDB, List<CoordinateDB> coordinates, boolean isValidate, String objectName)
      throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {

      byte[] yoloTextByteArray = getYoloText(coordinates, writer, outputStream);

      uploadTextFile(isValidate, convertToTxtFileName(objectName), yoloTextByteArray);
    } catch (IOException e) {
      logger.error("failed to create yolo files for image {}", imageDB.getId(), e);
      throw e;
    }
  }

  private static String convertToTxtFileName(String imageFileName) {

    int dotIndex = imageFileName.lastIndexOf('.');
    if (dotIndex == -1) {
      return imageFileName + ".txt"; // No extension found, append .txt
    } else {
      return imageFileName.substring(0, dotIndex) + ".txt"; // Replace existing extension
    }
  }


  private void uploadTextFile(boolean isValidate, String objectName, byte[] yoloTextByteArray) {
    String folder = String.format("%s/%s/%s/%s", storageRootDir, DATASET, LABELS, isValidate ? VAL : TRAIN);
    cloudStorageService.uploadFileToStorage(dataSetBucket, folder, objectName, yoloTextByteArray);
  }

  private String moveImageToDataSetPath(boolean isValidate, String gcsFullPath, String objectName) {

    String destinationObject = String.format("%s/%s/%s/%s/%s", storageRootDir, DATASET, IMAGES,
        isValidate ? VAL : TRAIN, objectName);
    String copiedImageUri = cloudStorageService.copyObject(gcsFullPath, dataSetBucket, destinationObject);
    cloudStorageService.deleteObject(gcsFullPath);
    return copiedImageUri;
  }

  private boolean isValidate(List<CoordinateDB> coordinates) {
    boolean isValidate = false;

    for (CoordinateDB coordinateDB : coordinates) {
      String category = coordinateDB.getAnimalId();
      int categoryObjects = categoriesHistogram.get(category) != null ? categoriesHistogram.get(category) : 0;
      categoriesHistogram.put(category, ++categoryObjects);
      int modulusValue = (int) Math.round(100.0 / validateRate);
      if (categoryObjects % modulusValue == 0) {
        isValidate = true;
      }
    }

    return isValidate;
  }

  private static byte[] getYoloText(List<CoordinateDB> coordinates, Writer writer, ByteArrayOutputStream outputStream)
      throws IOException {
    for (CoordinateDB coord : coordinates) {
      int classId = Integer.parseInt(coord.getAnimalId());
      double xCenter = coord.getX_center();
      double yCenter = coord.getY_center();
      double width = coord.getWidth();
      double height = coord.getHeight();

      // Format the line in YOLO format: class x_center y_center width height
      String line = String.format("%d %.2f %.2f %.2f %.2f", classId, xCenter, yCenter, width, height);
      writer.write(line);
      writer.write("\n");
    }
    writer.flush();

    return outputStream.toByteArray();
  }

  public GCSFileContent getImageContent(String imageId) {
    ImageDB imageDB = imagesRepository.findById(UUID.fromString(imageId)).orElseThrow();
    return cloudStorageService.getGCSFileContent(imageDB.getGcsFullPath());
  }

  public ImageApi getNextTask(String email) {

    synchronized (IMAGE_ASSIGN_LOCK) {
      UserDB user = usersRepository.findByEmail(email).orElseThrow();

      Timestamp startHandled = getTimestampMinutesAgo(imageValidationMinutes);
      Pageable pageable = PageRequest.of(0, 1); // Fetch only the first result

      List<ImageDB> images = imagesRepository.getNextTask(PENDING, TAGGED, user, startHandled, pageable);

      if (images.isEmpty()) {
        return new ImageApi();
      }

      ImageDB imageDB = images.get(0);
      imageDB.setStartHandled(getCurrentTimestamp());
      imagesRepository.save(imageDB);
      return convertToImageApi(imageDB);
    }
  }

  static Timestamp getCurrentTimestamp() {
    long currentTimeMillis = System.currentTimeMillis();
    return new Timestamp(currentTimeMillis);
  }


  static Timestamp getTimestampMinutesAgo(int minutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, -minutes);
    return new Timestamp(calendar.getTimeInMillis());
  }

  static Timestamp getMinTimestamp() {
    return Timestamp.valueOf("1970-01-01 00:00:00");
  }

  public Map<String, ImageMetaData> loadCsvToMap(byte[] csvData) throws IOException {
    Map<String, ImageMetaData> imageMetaDataMap = new HashMap<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvData)));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(',').withHeader())) {

      for (CSVRecord record : csvParser) {
        String folderName = record.get("folder_name");
        String jpgName = record.get("jpg_name");
        String jpgDate = record.get("jpg_date");
        String jpgTime = record.get("jpg_time");

        ImageMetaData imageMetaData = new ImageMetaData(folderName, jpgName, jpgDate, jpgTime);
        imageMetaDataMap.put(jpgName, imageMetaData);
      }
    }

    return imageMetaDataMap;

  }

  public String createReport() throws JsonProcessingException {
    StringBuilder csvBuilder = new StringBuilder();
    // Append the header
    CategoriesApi categories = categoriesService.getCategoriesOrThrow();

    setReportHeader(csvBuilder, categories);

    int offset = 0;
    List<ImageDB> images;
    while (true) {
      images = imagesRepository.findImagesWithStatusAndNonNullJpgName(VALIDATED, TRAINABLE, PageRequest.of(offset, csvBatchSize));
      if (images.isEmpty()) {
        logger.info("csv report completed");
        break;
      }

      for (ImageDB image : images) {
        appendImage(image, csvBuilder, categories.getEntries().keySet());
      }
      offset += csvBatchSize;
      logger.debug("finished batch for report. current offset = {}", offset);
    }

    return csvBuilder.toString();
  }

  private static void setReportHeader(StringBuilder csvBuilder, CategoriesApi categories) {
    csvBuilder.append("folder_name,jpg_name,jpg_date,jpg_time");
    for (String category : categories.getEntries().keySet()) {
      csvBuilder.append(",").append(category);
    }
    csvBuilder.append("\n");
  }

  private static void appendImage(ImageDB image, StringBuilder csvBuilder, Set<String> categories) {

    csvBuilder.append(image.getFolder())
        .append(",")
        .append(image.getJpgName())
        .append(",")
        .append(image.getJpgDate())
        .append(",")
        .append(image.getJpgTime());

    List<CoordinateDB> sortedCoordinates = image.getCoordinates().stream()
        .sorted(Comparator.comparing(CoordinateDB::getAnimalId)).toList();

    int categoriesIdx = 0;
    int coordinatesIdx = 0;

    List<String> categoriesList = new ArrayList<>(categories);
    while (categoriesIdx < categoriesList.size()) {
      int count = 0;

      while (coordinatesIdx < image.getCoordinates().size() &&
          categoriesList.get(categoriesIdx).equals(sortedCoordinates.get(coordinatesIdx).getAnimalId())) {
        count++;
        coordinatesIdx++;
      }
      csvBuilder.append(",");
      csvBuilder.append(count);
      categoriesIdx++;
    }

    csvBuilder.append("\n");
  }


  record ImageMetaData(String folderName, String jpgName, String jpgDate, String jpgTime) {

  }
}
