package randomunit;

/**
 * Thrown by RandomizedTestCase when a invariant is violated.
 *
 * @see randomunit.RandomizedTestCase
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public class InvariantFailedException extends RuntimeException {
    public InvariantFailedException() {
    }
    
    public InvariantFailedException(String message) {
        super(message);
    }
}
