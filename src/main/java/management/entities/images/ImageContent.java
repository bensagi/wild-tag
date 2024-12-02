package management.entities.images;

public class ImageContent {

  byte[] content;
  String contentType;

  public ImageContent(byte[] content, String contentType) {
    this.content = content;
    this.contentType = contentType;
  }

  public byte[] getContent() {
    return content;
  }

  public String getContentType() {
    return contentType;
  }
}
