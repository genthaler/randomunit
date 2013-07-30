package randomunit;

import java.lang.reflect.Method;

/**
 * This interface reads <code>MethodInvocationLog</code>s and reports its contents to help diagnose
 * a problem.
 *
 * @see RandomizedTestCase
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public interface LogStrategy {
    /**
     * Appends an execution log. The test method that was executed is provided,
     * its arguments, and the returned value.
     */
    void appendLog(MethodInvocationLog log);
    
    /**
     * Returns a textual representation of the log entries (possibly truncated) in this LogStrategy.
     * This is used for the messages of exceptions thrown by a randomized test.
     */
    String dump();
}
