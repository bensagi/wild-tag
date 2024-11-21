package management.entities.images;

public class CoordinateDB {
  private String animalId;
  private double x_center;
  private double y_center;
  private double height;
  private double width;

  public CoordinateDB() {
  }

  public CoordinateDB(String id, double x_center, double y_center, double height, double width) {
    this.animalId = id;
    this.x_center = x_center;
    this.y_center = y_center;
    this.height = height;
    this.width = width;
  }

  public String getAnimalId() {
    return animalId;
  }

  public CoordinateDB setAnimalId(String animalId) {
    this.animalId = animalId;
    return this;
  }

  public double getX_center() {
    return x_center;
  }

  public CoordinateDB setX_center(double x_center) {
    this.x_center = x_center;
    return this;
  }

  public double getY_center() {
    return y_center;
  }

  public CoordinateDB setY_center(double y_center) {
    this.y_center = y_center;
    return this;
  }

  public double getHeight() {
    return height;
  }

  public CoordinateDB setHeight(double height) {
    this.height = height;
    return this;
  }

  public double getWidth() {
    return width;
  }

  public CoordinateDB setWidth(double width) {
    this.width = width;
    return this;
  }
}
