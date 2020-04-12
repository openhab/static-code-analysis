import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TestInterface {
    
    String property1() default "";

    String property2() default "";

    String property2() default "";

}
