package randomunit;

import java.lang.reflect.Method;

/**
 * A simple log that consists of a Method that has been invoked, the arguments
 * that were used for this invocation, and the value that the method returned,
 * if any.
 * 
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public class MethodInvocationLog {
	private final Method method;
	private final PooledObject[] args;
	private final Object returnedValue;
	private final String[] targetPools;

	/**
	 * Creates a MethodInvocationLog.
	 * 
	 * @param method
	 *            the method that was invoked (cannot be null)
	 * @param args
	 *            the arguments with which the method was invoked. If null, new
	 *            Object[0] is implied
	 * @param returnedValue
	 *            the value that the method returned (or null if none)
	 * @param targetPools
	 *            the pools into which the returnedValue was appended
	 */
	public MethodInvocationLog(Method method, PooledObject[] args,
			Object returnedValue, String[] targetPools) {
		if (method == null) {
			throw new IllegalArgumentException("Method cannot be null");
		}
		this.method = method;
		if (args == null) {
			args = new PooledObject[0];
		}
		this.args = args;
		this.returnedValue = returnedValue;
		this.targetPools = targetPools;
	}

	/**
	 * Returns the method of this invocation.
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Returns the arguments with which the method of this log was invoked.
	 */
	public PooledObject[] getArgs() {
		return args;
	}

	/**
	 * Returns the value that the method of this log returned (or null if none).
	 */
	public Object getReturnedValue() {
		return returnedValue;
	}

	/**
	 * Returns the pools into which the returned value was added.
	 */
	public String[] getTargetPools() {
		return targetPools;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getName());
		sb.append("(");
		for (Object o : args) {
			sb.append(o).append(", ");
		}
		if (args.length > 0) {
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append(")-->").append(returnedValue);
		return sb.toString();
	}

	/**
	 * This class describes an object that has been appended to one or more
	 * object pools.
	 */
	public static class PooledObject {
		private final Object object;
		private final String poolName;

		/**
		 * Creates a PooledObject instance, that described the specified object,
		 * which has been appended to the given object pool.
		 */
		public PooledObject(Object object, String poolName) {
			this.object = object;
			this.poolName = poolName;
		}

		/**
		 * Returns the object that has been appended to a pool.
		 */
		public Object getObject() {
			return object;
		}

		/**
		 * Returns the name of the object pool that an object has been appended
		 * to.
		 */
		public String getPoolName() {
			return poolName;
		}

		@Override
		public String toString() {
			return "" + object;
		}
	}
}
