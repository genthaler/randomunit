package randomunit;

import java.util.List;

/**
 * Thrown by RandomizedTestCase to indicate a bug.
 *
 * @see randomunit.RandomizedTestCase
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public class TestFailedException extends RuntimeException {
    private final LogStrategy log;
    
    public TestFailedException(String message, MethodInvocationLog attempedExecution,
            Throwable cause, LogStrategy log) {
        super(message, cause);
        this.log = log;
    }
    
    public LogStrategy getLog() {
        return log;
    }
    
    @Override
    public String getMessage() {
        return super.getMessage() + ", log=" + (log != null ? log.dump() : "null") + ", cause=" + getCause();
    }
}
