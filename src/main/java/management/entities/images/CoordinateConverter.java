package management.entities.images;

import management.entities.converters.AbstractListConverter;

public class CoordinateConverter extends AbstractListConverter<CoordinateDB> {

  @Override
  protected Class<? extends CoordinateDB> getElementsClass() {
    return CoordinateDB.class;
  }
}
