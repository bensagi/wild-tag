package management.entities.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import jakarta.persistence.AttributeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListConverter<T> implements AttributeConverter<List<T>, String> {

  protected static ObjectMapper mapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<T> attribute) {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    List<T> attributeToConvert = attribute;

    if (attribute == null) {
      attributeToConvert = new ArrayList<>();
    }

    try {
      mapper.writeValue(out, attributeToConvert);
    } catch (IOException e) {
//      logger.error("Error converting attribute list to string", e);
      throw new InternalError();
    }

    return (out.toString(StandardCharsets.UTF_8));
  }

  @Override
  public List<T> convertToEntityAttribute(String dbData) {

    try {
      if (Strings.isNullOrEmpty(dbData)) {
        return new ArrayList<>();
      }
      return mapper
          .readValue(dbData,
              mapper.getTypeFactory().constructCollectionLikeType(ArrayList.class, getElementsClass()));
    } catch (IOException e) {
//      logger.error("Error converting string to attribute list data: [{}]", dbData, e);
      throw new InternalError();
    }
  }

  /**
   *
   */
  protected abstract Class<? extends T> getElementsClass();

  public static class ListConversionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ListConversionException with the specified error message and cause.
     *
     * @param message Describes the error encountered.
     */
    public ListConversionException(String message, Throwable cause) {
      super(message, cause);
    }


  }
}
