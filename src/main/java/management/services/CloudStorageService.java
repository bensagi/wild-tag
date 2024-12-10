package management.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.CopyWriter;
import com.google.cloud.storage.StorageOptions;
import io.micrometer.common.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import management.entities.images.GCSFileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.cloud.storage.Storage;

@Service
public class CloudStorageService {

  public static final String GS = "gs://";
  private final Storage storageClient;

  private final Logger logger = LoggerFactory.getLogger(CloudStorageService.class);


  public CloudStorageService() throws IOException {
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    this.storageClient = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
  }

  public List<String> listFilesInPath(String path) {
    int slashIndex = path.indexOf('/');
    if (slashIndex == -1) {
      throw new IllegalArgumentException("The URI should contain a '/' after the bucket name");
    }

    String bucketName = path.substring(0, slashIndex);
    String prefix = path.substring(slashIndex + 1);

    List<String> files = new ArrayList<>();
    storageClient
        .list(bucketName, Storage.BlobListOption.prefix(prefix))
        .iterateAll()
        .forEach(blob -> files.add(GS + blob.getBucket() + "/" + blob.getName()));
    return files;
  }


  public String uploadFileToStorage(String bucketName, String folder, String fileName, byte[] bytes) {
    String filePath = StringUtils.isEmpty(folder) ? fileName : folder + "/" + fileName;

    BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath).build();

    storageClient.create(blobInfo, bytes);

    logger.info("uploaded file to GCS. bucket = {}, file = {}", bucketName, filePath);

    return filePath;
  }

  public String copyObject(String sourceGcsUri, String destinationBucketName, String destinationObjectName) {

    // Create BlobId for source and destination
    BlobId sourceBlobId = getGetBlobId(sourceGcsUri);
    BlobId destinationBlobId = BlobId.of(destinationBucketName, destinationObjectName);
    BlobInfo destinationBlobInfo = BlobInfo.newBuilder(destinationBlobId).build();
    // Perform the copy
    CopyWriter copyWriter = storageClient.copy(Storage.CopyRequest.newBuilder()
        .setSource(sourceBlobId)
        .setTarget(destinationBlobInfo)
        .build());
    // Get the result and print the new object's information
    copyWriter.getResult();
    String destinationUri = GS + destinationBucketName + "/" + destinationObjectName;
    logger.debug("Copied object from {} to {}", sourceGcsUri, destinationUri);
    return destinationUri;
  }

  private static BlobId getGetBlobId(String uri) {
    String[] sourceUriParts = uri.replace(GS, "").split("/", 2);
    return BlobId.of(sourceUriParts[0], sourceUriParts[1]);
  }

  public void deleteObject(String objectUri) {
    BlobId blobId = getGetBlobId(objectUri);

    boolean deleted = storageClient.delete(blobId);
    if (deleted) {
      logger.debug("deleted: {}", objectUri);
    } else {
      logger.error("Failed to delete the source object: {}", objectUri, new Exception());
    }
  }

  public GCSFileContent getGCSFileContent(String uri) {
    Blob blob = storageClient.get(getGetBlobId(uri));
    if (blob == null) {
      throw new RuntimeException("No such object exists");
    }
    String contentType = blob.getContentType();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    blob.downloadTo(outputStream);
    return new GCSFileContent(outputStream.toByteArray(), contentType);
  }
}
