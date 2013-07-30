package randomunit;

/**
 * Thrown by RandomizedTestCase when a precondition fails.
 *
 * @see randomunit.RandomizedTestCase
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public class PreconditionFailedException extends RuntimeException {
    public PreconditionFailedException() {
        super();
    }
    
    public PreconditionFailedException(String message) {
        super(message);
    }
}
