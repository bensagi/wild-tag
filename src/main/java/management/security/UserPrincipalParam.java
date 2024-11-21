package management.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserPrincipalParam {

  /**
   * Alias for {@link #name}.
   */
  @AliasFor("name")
  String value() default "";

  /**
   * The name of the attribute to bind to.
   * <p>The default name is inferred from the method parameter name.
   */
  @AliasFor("value")
  String name() default "";

  /**
   * Whether the attribute is required.
   * <p>Defaults to {@code true}, leading to an exception being thrown
   */
  boolean required() default true;
}
