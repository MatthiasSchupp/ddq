package eu.domaindriven.ddq.hal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
public @interface RequestLink {

    String path() default "";

    String rel() default "";

    String condition() default "";
}
