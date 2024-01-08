package management.converters.set;

import management.entities.vulnerabilities.CvssInfoDB;

public class CvssInfoSetConverter extends HibernateSetConverter<CvssInfoDB> {

  @Override
  protected Class<? extends CvssInfoDB> getElementsClass() {
    return CvssInfoDB.class;
  }
}
