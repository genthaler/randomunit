package randomunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the returned value of the marked method is to be added to the specified object pool (or pools).
 *
 * @see randomunit.RandomizedTestCase
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Creates {
    String[] value();
}
