package management.converters.set;

public class StringSetConverter extends HibernateSetConverter<String> {

  @Override
  protected Class<? extends String> getElementsClass() {
    return String.class;
  }
}
