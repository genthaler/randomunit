package randomunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a specific method checks the invariant of some object. This annotation must specify
 * the name of the object pool in which the tested object belongs. The method that is annotated with @Invariant
 * must accept exactly one parameter (some object from the specified object pool).
 *
 * @see randomunit.RandomizedTestCase
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Invariant {
    String value();
}
