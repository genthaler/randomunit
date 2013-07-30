package randomunit;

/**
 * Thrown by RandomizedTestCase when a postcondition is violated.
 *
 * @see randomunit.RandomizedTestCase
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public class PostconditionFailedException extends RuntimeException {
    public PostconditionFailedException() {
        super();
    }
    
    public PostconditionFailedException(String message) {
        super(message);
    }
}
