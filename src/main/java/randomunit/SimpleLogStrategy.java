package randomunit;

import java.lang.reflect.Method;
import java.util.LinkedList;
/**
 * A simple LogStrategy implementation that maintains a single log buffer, for every method invocation.
 * The length of the buffer is bounded at an arbitrary value.
 *
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public class SimpleLogStrategy implements LogStrategy {
    private final LinkedList<MethodInvocationLog> log = new LinkedList<MethodInvocationLog>();
    private final int bufferLength;
    
    /**
     * Creates an instance of this class, given the maximum allowable length of the internal buffer.
     * When the buffer is full, for each new entry logged, the earliest entry is removed.
     *
     * @param bufferLength the maximum length of the log buffer. Must be >= 0
     */
    public SimpleLogStrategy(int bufferLength) {
        if (bufferLength < 0) {
            throw new IllegalArgumentException("Buffer length cannot be negative");
        }
        this.bufferLength = bufferLength;
    }

    public String dump() {
        return log.toString();
    }

    public void appendLog(MethodInvocationLog entry) {
        log.addLast(entry);
        if (log.size() > bufferLength) {
            log.removeFirst();
        }
    }
}
