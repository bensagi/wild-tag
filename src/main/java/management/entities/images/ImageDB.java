package management.entities.images;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.List;
import management.entities.AbstractEntity;
import management.entities.users.UserDB;

@Entity
@Table(name = "images")
public class ImageDB extends AbstractEntity {
  @Column(name = "gcsFullPath", columnDefinition = "text")
  private String gcsFullPath;
  @Column(name = "status", columnDefinition = "text")
  @Enumerated(EnumType.STRING)
  private ImageStatus status;
  @ManyToOne
  private UserDB taggerUser;
  @ManyToOne
  private UserDB validatorUser;
  @Column(name = "coordinates", columnDefinition = "text")
  @Convert(converter = CoordinateConverter.class)
  private List<CoordinateDB> coordinates;
  @Column(name = "gcsTaggedPath", columnDefinition = "text")
  private String gcsTaggedPath;

  @Column(name = "start_handled")
  private Timestamp startHandled = Timestamp.valueOf("1970-01-01 00:00:00");

  @Column(name = "folder_name", columnDefinition = "text")
  private String folder;

  @Column(name = "jbg_name", columnDefinition = "text")
  private String jpgName;


  @Column(name = "jbg_date", columnDefinition = "text")
  private String jpgDate;


  @Column(name = "jbg_time", columnDefinition = "text")
  private String jpgTime;


  public ImageDB() {
    super();
  }

  public ImageDB(String gcsFullPath, ImageStatus status, UserDB taggerUser, UserDB validatorUser, List<CoordinateDB> coordinates, String gcsTaggedPath) {
    super();
    this.gcsFullPath = gcsFullPath;
    this.status = status;
    this.taggerUser = taggerUser;
    this.validatorUser = validatorUser;
    this.coordinates = coordinates;
    this.gcsTaggedPath = gcsTaggedPath;
  }

  public String getGcsFullPath() {
    return gcsFullPath;
  }

  public ImageDB setGcsFullPath(String gcsFullPath) {
    this.gcsFullPath = gcsFullPath;
    return this;
  }

  public ImageStatus getStatus() {
    return status;
  }

  public ImageDB setStatus(ImageStatus status) {
    this.status = status;
    return this;
  }

  public UserDB getTaggerUser() {
    return taggerUser;
  }

  public ImageDB setTaggerUser(UserDB taggerUser) {
    this.taggerUser = taggerUser;
    return this;
  }

  public UserDB getValidatorUser() {
    return validatorUser;
  }

  public ImageDB setValidatorUser(UserDB validatorUser) {
    this.validatorUser = validatorUser;
    return this;
  }

  public List<CoordinateDB> getCoordinates() {
    return coordinates;
  }

  public ImageDB setCoordinates(List<CoordinateDB> json) {
    this.coordinates = json;
    return this;
  }

  public String getGcsTaggedPath() {
    return gcsTaggedPath;
  }

  public ImageDB setGcsTaggedPath(String gcsTaggedPath) {
    this.gcsTaggedPath = gcsTaggedPath;
    return this;
  }

  public Timestamp getStartHandled() {
    return startHandled;
  }

  public ImageDB setStartHandled(Timestamp startHandled) {
    this.startHandled = startHandled;
    return this;
  }

  public String getFolder() {
    return folder;
  }

  public ImageDB setFolder(String folder) {
    this.folder = folder;
    return this;
  }

  public String getJpgName() {
    return jpgName;
  }

  public ImageDB setJpgName(String jpgName) {
    this.jpgName = jpgName;
    return this;
  }

  public String getJpgDate() {
    return jpgDate;
  }

  public ImageDB setJpgDate(String jpgDate) {
    this.jpgDate = jpgDate;
    return this;
  }

  public String getJpgTime() {
    return jpgTime;
  }

  public ImageDB setJpgTime(String jpgTime) {
    this.jpgTime = jpgTime;
    return this;
  }
}
