package management.security;

import java.security.Principal;
import management.entities.users.UserDB;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;

public class UserPrincipalParamHandlerMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

  @Override
  protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
    UserPrincipalParam ann = parameter.getParameterAnnotation(UserPrincipalParam.class);
    Assert.state(ann != null, "No PortshiftWebPrincipalParam annotation");
    return new NamedValueInfo(ann.name(), ann.required(), ValueConstants.DEFAULT_NONE);
  }

  @Override
  protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest nativeWebRequest) {
    Principal principal = nativeWebRequest.getUserPrincipal();

    UserDB userDB = null;
    if (principal instanceof UsernamePasswordAuthenticationToken) {
      /* for some reason in real life the Principal in nativeWebRequest is an Authentication object (that holds the Principal as a field) */
      userDB = (UserDB) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }
    Assert.state(userDB != null, "No Principal in request");
    return userDB.getEmail();
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    Class<?> paramType = parameter.getParameterType();
    return parameter.hasParameterAnnotation(UserPrincipalParam.class) && paramType.equals(String.class);
  }
}
