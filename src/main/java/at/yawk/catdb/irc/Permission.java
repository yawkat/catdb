package at.yawk.catdb.irc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yawkat
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
// todo: repeatable
public @interface Permission {
    String value();
}
