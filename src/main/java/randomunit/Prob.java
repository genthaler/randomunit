package randomunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an array of probabilities for a test method. Examples:
 *<BR><BR>
 * <pre>
 * &#064;Prob(0.5)
 * void myMethod() { }
 *
 * &#064;Prob({1.5, 17.0})
 * void myOtherMethod() { }
 * </pre>
 *
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Prob {
    double[] value();
}
