package randomunit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import randomunit.MethodInvocationLog.PooledObject;

/**
 * A LogStrategy implementation that keeps a history log for every object that appears either as a
 * parameter or as a returned value. Note that the log queues are categorized by the object pools names,
 * so if it happens that two equal objects exist in two separate object pools, two queues will exist;
 * each object will have its own history record.
 *
 * @see randomunit.RandomizedTestCase
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public class DetailedLogStrategy implements LogStrategy {
    private final int bufferPerObjectSize;
    private final Map<String, Map<Object, LinkedList<MethodInvocationLog>>> logMap =
            new HashMap<String, Map<Object, LinkedList<MethodInvocationLog>>>();
    
    /**
     * Creates a DetailedLogStrategy, that will maintain a queue of log entries for every method
     * parameter or return value. The queue will have at most the specified size.
     *
     * @param bufferPerObjectSize the maximum size of a single object's queue
     */
    public DetailedLogStrategy(int bufferPerObjectSize) {
        if (bufferPerObjectSize < 0) {
            throw new IllegalArgumentException("Buffer length cannot be negative");
        }
        this.bufferPerObjectSize = bufferPerObjectSize;
    }
    
    public void appendLog(MethodInvocationLog log) {
        for (PooledObject arg : log.getArgs()) {
            updateLog(getObjectLog(arg), log);
        }
        Object ret = log.getReturnedValue();
        if (ret != null) {
            for (String poolName : log.getTargetPools()) {            
                updateLog(getObjectLog(new PooledObject(ret, poolName)), log);
            }
        }
    }
    
    private void updateLog(LinkedList<MethodInvocationLog> logEntries, MethodInvocationLog log) {
        logEntries.addLast(log);
        if (logEntries.size() > bufferPerObjectSize) {
            logEntries.removeFirst();
        }
    }
    
    private LinkedList<MethodInvocationLog> getObjectLog(PooledObject pooledObject) {
        Map<Object, LinkedList<MethodInvocationLog>> map = logMap.get(pooledObject.getPoolName());
        if (map == null) {
            map = new HashMap<Object, LinkedList<MethodInvocationLog>>();
            logMap.put(pooledObject.getPoolName(), map);
        }
        LinkedList<MethodInvocationLog> list = map.get(pooledObject.getObject());
        if (list == null) {
            list = new LinkedList<MethodInvocationLog>();
            map.put(pooledObject.getObject(), list);
        }
        return list;
    }
    
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String pool : logMap.keySet()) {
            sb.append("Pool:'").append(pool).append("'=");
            sb.append(logMap.get(pool).toString());
            sb.append(", ");
        }
        if (!logMap.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("]");
        
        return sb.toString();
    }
}
