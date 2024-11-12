package management.entities.vulnerabilities;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import management.converters.set.CvssInfoSetConverter;
import management.converters.set.StringSetConverter;
import management.entities.AbstractEntity;
import org.springframework.util.CollectionUtils;

@Entity
@Table(name = "users")
public class User extends AbstractEntity {

  @Column(name = "name", columnDefinition = "text")
  private String name;

  @Column(columnDefinition = "text")
  private String email;

  @Column(columnDefinition = "text")
  private String password;


  //TODO:
  /*
      POC / Exploit and source link

      CISA indication (is exploited in the wild)

      SANS Top 25

      CWE Top 25

      OWASP Top 10
   */

  public User() {
  }

  public VulnerabilityDB(String cveId) {
    calcAndSetId(cveId);
  }

  public VulnerabilitySeverityDB getSeverity() {
    return severity;
  }

  public VulnerabilityDB setSeverity(VulnerabilitySeverityDB severity) {
    this.severity = severity;
    return this;
  }

  public float getCvssScore() {
    return cvssScore;
  }

  public VulnerabilityDB setCvssScore(float cvssScore) {
    this.cvssScore = cvssScore;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public VulnerabilityDB setDescription(String description) {
    this.description = description;
    return this;
  }

  public Set<CvssInfoDB> getCvssInfoSet() {
    return cvssInfoSet;
  }

  public VulnerabilityDB setCvssInfoSet(Set<CvssInfoDB> cvssInfoSet) {
    this.cvssInfoSet = cvssInfoSet;
    return this;
  }

  public String getSourceLink() {
    return sourceLink;
  }

  public VulnerabilityDB setSourceLink(String sourceLink) {
    this.sourceLink = sourceLink;
    return this;
  }

  public Set<String> getAdditionalReferences() {
    return additionalReferences;
  }

  public VulnerabilityDB setAdditionalReferences(Set<String> additionalReferences) {
    this.additionalReferences = additionalReferences;
    return this;
  }

  public Set<String> getCwes() {
    return cwes;
  }

  public VulnerabilityDB setCwes(Set<String> cwes) {
    this.cwes = cwes;
    return this;
  }

  public Set<String> getAliases() {
    return aliases;
  }

  public VulnerabilityDB setAliases(Set<String> aliases) {
    this.aliases = aliases;
    return this;
  }
}
