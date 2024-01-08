package management.converters.set;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HibernateSetConverter<T> implements AttributeConverter<Set<T>, String> {
  protected static ObjectMapper mapper = new ObjectMapper();
  private Logger logger = LoggerFactory.getLogger(HibernateSetConverter.class);

  @Override
  public String convertToDatabaseColumn(Set<T> attribute) {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    Set<T> attributeToConvert = attribute;

    if (attribute == null) {
      attributeToConvert = new HashSet<>();
    }

    try {
      mapper.writeValue(out, attributeToConvert);
    } catch (IOException e) {
      logger.error("Error converting attribute set to string", e);
      throw new InternalError();
    }

    return (out.toString(StandardCharsets.UTF_8));
  }

  @Override
  public Set<T> convertToEntityAttribute(String dbData) {

    try {
      if (Strings.isBlank(dbData)) {
        return new HashSet<>();
      }
      return mapper
          .readValue(dbData,
              mapper.getTypeFactory().constructCollectionLikeType(HashSet.class, getElementsClass()));
    } catch (IOException e) {
      logger.error("Error converting string to attribute set data: [{}]", dbData, e);
      throw new InternalError();
    }
  }

  /**
   *
   */
  protected abstract Class<? extends T> getElementsClass();
}
