package management.entities.vulnerabilities;

import java.util.Objects;

public class CvssInfoDB implements Comparable<CvssInfoDB> {

  private Float score;
  private String type;
  private String source;
  private String version;

  private String severity;

  private String vector;

  public CvssInfoDB() {
  }

  public CvssInfoDB(Float score, String type, String source, String version, String severity, String vector) {
    this.score = score;
    this.type = type;
    this.source = source;
    this.version = version;
    this.severity = severity;
    this.vector = vector;
  }

  public Float getScore() {
    return score;
  }

  public void setScore(Float score) {
    this.score = score;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getVector() {
    return vector;
  }

  public void setVector(String vector) {
    this.vector = vector;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CvssInfoDB that = (CvssInfoDB) o;
    return Objects.equals(score, that.score) && Objects.equals(type, that.type)
        && Objects.equals(source, that.source) && Objects.equals(version, that.version)
        && Objects.equals(severity, that.severity) && Objects.equals(vector, that.vector);
  }

  @Override
  public int hashCode() {
    return Objects.hash(score, type, source, version, severity, vector);
  }

  @Override
  public int compareTo(CvssInfoDB cvssInfoDB) {
    return this.toString().compareTo(cvssInfoDB.toString());
  }
}