package at.yawk.catdb.irc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.RegExp;

/**
 * @author yawkat
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// todo: repeatable
public @interface CommandHandler {
    @RegExp String value();
}
