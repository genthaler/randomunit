package randomunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the object pools from which to choose random elements, to inject in a method as arguments. The
 * number of the method arguments must exactly match the number of strings that this annotation declares.
 *
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Params {
    String[] value();
}
