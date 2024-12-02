package management.configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

@ControllerAdvice
public class ExceptionsHandler {

  private final Logger logger = LoggerFactory.getLogger(ExceptionsHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> generalExceptionHandler(Exception exception, ServletWebRequest request) {
    final Map<String, String[]> map = request.getParameterMap();
    final String params = map.keySet().stream()
        .map(key -> key + "=" + Arrays.toString(map.get(key)))
        .collect(Collectors.joining(", ", "{", "}"));
    logger.error("Encountered exception. path: {}. params: {}", request.getRequest().getRequestURI(),
        params, exception);
    return new ResponseEntity<>("General server error", HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
