package randomunit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.*;
import java.util.*;
import java.util.Map.Entry;
import randomunit.MethodInvocationLog.PooledObject;

/**
 * This class facilitates the creation of randomized tests. Deterministic, hand-written tests usually
 * cover only limited amount of cases, and can miss subtle bugs. Randomized tests can help in this regard,
 * as they can cover a great deal of cases automatically. Still, it is sensible to write specific (usual)
 * tests for rare corner cases, to complement the randomized search.
 * <p>
 * To implement a randomized test, we need some collaborating objects. These objects are grouped into
 * named <i>pools</i>, that denote objects with similar semantics. For instance, an object pool named
 * "keys" could be containing objects that are used as keys in some map. By effectively organizing
 * the objects into groups, the search space is very much smaller, so the test concentrate more on
 * cases that actually matter.
 * <p>
 * The creation of a randomized test involves defining a subclass of this class, and defining object pools
 * and random test methods. Both these goals are reached by simply annotating the test methods. An example
 * follows, that depicts a method that tests a special stack implementation that does not accept null values.<BR><BR>
 * <p>
 * <pre>
 * &#064;Prob(0.5)
 * &#064;Params( { "stackPool", "elementPool" } )
 * public void check(MyStack stack, Object element) throws Exception {
 *     precondition(element != null, "Null elements are not allowed");
 *     precondition(!stack.isFull(), "Stack cannot be empty");
 *
 *     int size = stack.size();
 *     stack.push(element);
 *
 *     postcondition(!stack.isEmpty(), "Stack is not empty");
 *     postcondition(stack.size() == size + 1, "Stack size grows by one");
 * }
 * </pre>
 * <p>
 * The &#064;Params annotation specifies the names of the respective object pools, from which to choose random
 * members to inject in the parameters of the method. This concept is a form of dependency-injection:
 * the method describes its dependencies, and the "container" (the randomized test) injects appropriate values.
 * Note that &#064;Params is necessary as the type of the arguments cannot express possible semantics differences
 * between objects of the same type (ie, objects of same class that are used in a substantially different way).
 * Please note that the methods are not forced to specify the exact type of the arguments: any super type will
 * do. Also, primitive parameters can be defined. Yet, if a primitive argument is prescribed, and the
 * specified object pool contains null values, there is the possibility that <code>null</code> will be randomly
 * selected and attempted to be injected as a primitive, but this is impossible and an exception will be thrown.
 * <p>
 * A random test method usually is composed by three parts: preconditions, actual testing of some object via
 * its methods, and postconditions. If a precondition fails, nothing happens; this just means that the random
 * arguments that were chosen were illegal for the respective action to take place. This is not counted as a
 * complete step. But if the testing of the object throws an exception, this constitutes a bug: valid arguments
 * (preconditions passed) caused a failed execution. If any postcondition fails, this is also a bug: the object
 * under test did not maintain its contract.
 * <p>
 * How are objects inserted into object pools? By the means of random test methods too. Each method that
 * has a &#064;Prob annotation (i.e., a random test method), can return a value and request it to be added to a
 * object pool (or more), as declared by the &#064;Creates annotation. This is done like this:
 * <p>
 * <pre>
 * &#064;Creates("elementPool")
 * &#064;Prob(0.8)
 * public int createRandomElement() {
 *     return random.nextInt(10); //this is added into the "elementPool" pool as Integer automatically
 * }
 * </pre>
 * <p>
 * A method can very well play the two roles simultaneously. It can declare a &#064;Creates annotation and return
 * a value, yet it can take arguments and declare a &#064;Params annotation. This notion captures the expressiveness
 * needed for cases where we specifically want to test objects that are derived from (or anyhow depended on) other
 * objects into consideration.
 * <p>
 * Random test methods do not have to start with "test" prefix. Such methods will be considered by JUnit as usual.
 * If wanted, some prefix like "random" can be used for method names, but this is not required.
 * <p>
 * When a random method is chosen, that needs objects from pools that are currently empty, the method is
 * skipped, and it is not recorded as an executed step. So, if there is only one test method that requires
 * a parameter, and the only method that creates an object for the respective object pool is never called
 * (for instance, it has a zero probability), an infinite loop will ensue.
 * <p>
 * What if we want to create many objects at the start, and after that only a few? This case is satisfied
 * by using different <i>phases</i> during the test. Each &#064;Prob actually declares an array of doubles:
 * the first double is the method's relative probability for phase 0, the second for phase 2 etc, as in this example:
 * <p>
 * <pre>
 * &#064;Prob({2, 0}
 * void myMethod(...) { }
 * </pre>
 * The example shows a method that will never be executed at the second phase (phase == 1), but it has a positive
 * probability ( == 2) in the first phase (phase == 0). Trailing zero probabilities are implied. The randomized test can change the current phase wherever it wants. A good
 * place is at the <a href="#onStep(int)">onStep(int executedSteps)</a> call-back method, that is called after
 * the execution of each random test method.
 * <p>
 * Invariant checking is also supported. Annotate a method with &#064;Invariant("poolName"), that tests the invariant
 * of some object belonging to the specified object pool. Such a method must have exactly one argument defined,
 * that is injected by a value from the respective object pool. Here is an example:
 * <p>
 * <pre>
 * &#064;Invariant("stackPool")
 * public void checkStackInvariant(Stack stack) {
 *     invariant(stack.isEmpty() ^ stack.size() > 0); //stack is empty XOR stack size is not zero, not both
 * }
 * </pre>
 * <p>
 * An object pool can have as many invariant-testing methods associated with it as desired. After each random
 * method invocation, <strong>all</strong> respective invariant-testing methods are called for every argument
 * that was supplied to the method. If the method is declared as &#064;Creates, the respective invariant-testing
 * methods are called for its return value as well.
 * <p>
 * For further customization, another call-back method is provided, <a href="#filterNewObject(java.lang.String, java.lang.Object)">
 * filterNewObject(String poolName, Object newObject)</a> which can be overrided to control exactly the objects that are inserted into an object pool.
 * <p>
 * A LogStrategy is associated with each RandomizedTestCase. It logs method invocations, along with
 * information about its parameters and the pools from which they were chosen.
 * <p>
 * Finally, the contents of the object pools can be always be queried using the method <a href="#getPool(String)>
 * getPool(String poolName)</a>.
 *
 *
 * @author Andreou Dimitris, andreou &lt at &gt csd dot uoc dot gr
 */
public abstract class RandomizedTestCase extends TestCase {
    /**
     * Random instance that is initialized with a constant seed (so that the tests are repeatable).
     * Tests implementations are free to use it to generate random numbers.
     */
    protected final Random random = new Random(0);
    
    /**
     * The log strategy used by this RandomizedTestCase instance.
     */
    protected final LogStrategy logStrategy;
    
    private final int steps;
    private int currentPhase;
    
    private final List<ProbabilitiesManager> probManagers;
    
    private final Map<String, List<Object>> pools = new HashMap<String, List<Object>>();
    private final Map<String, List<Method>> invariants = new HashMap<String, List<Method>>();
        
    private int executedSteps;
    
    /**
     * Creates a randomized test that will execute as many random steps as specified, and will
     * log a specified amount of steps in an internal cyclic buffer.
     *
     * To successfully call this constructor, a list of rules apply:
     * - Every method that declares a &#064;Creates annotation, cannot have void return type.
     * - Every method that declares a &#064;Prob annotation must be non-private.
     * - Every method that declares a &#064;Prob annotation and has parameters, it must declare a &#064;Params
     * annotation with a valid pool name for each parameter.
     * - At least one &#064;Creates method must exist.
     * - Each &#064;Invariant method must have exactly one parameter.
     * - No method can both declare &#064;Creates and &#064;Invariant.
     * - &#064;Invariant annotations must specify an object pool which at least one &#064;Creates specifies.
     * - At least one random test method (&#064;Prob) must be declared.
     *
     *
     * @param testName the name of the test (used for JUnit)
     * @param steps the number of random steps to execute (must be > 0)
     * @param logStrategy the strategy that will handle the produced log entries. May be null (a
     * default LogStrategy will be used)
     * @see #getLogStrategy()
     */
    public RandomizedTestCase(String testName, int steps, LogStrategy logStrategy) {
        super(testName);
        if (testName.startsWith("test") && testName.length() > 4) {
            //ignore testXXX methods, but do not ignore test() method used by this class
            //The constructor was called by JUnit to execute a test method - do nothing
            this.steps = 0;
            this.logStrategy = null;
            this.probManagers = null;
            return;
        }
        if (steps <= 0) {
            throw new IllegalArgumentException("Steps must be > 0");
        }
        if (logStrategy == null) {
            logStrategy = new SimpleLogStrategy(0);
        }
        this.logStrategy = logStrategy;
        this.steps = steps;
        initPools();
        initInvariants();
        probManagers = initMethods();
    }
    
    private void initPools() {
        for (Method m : getClass().getDeclaredMethods()) {
            Creates creator = m.getAnnotation(Creates.class);
            if (creator == null) { continue; }
            if (m.getReturnType().getName().equals("void")) {
                throw new RuntimeException("@Creator annotation cannot be declared in a method that has void return type: " + m);
            }
            for (String poolName : creator.value()) {
                if (!pools.containsKey(poolName)) {
                    pools.put(poolName, new ArrayList<Object>());
                    invariants.put(poolName, new ArrayList<Method>());
                }
            }
        }
    }
    
    private void initInvariants() {
        for (Method m : getClass().getDeclaredMethods()) {
            Invariant invariant = m.getAnnotation(Invariant.class);
            if (invariant == null) { continue; }
            if (m.getAnnotation(Creates.class) != null) {
                throw new RuntimeException("Illegal combination of annotations: @Invariant cannot coexist with @Creator, in method: " + m);
            }
            if (m.getParameterTypes().length != 1) {
                throw new RuntimeException("Method: '" + m + "' was annotated with @Invariant, so it MUST take exactly one parameter");
            }
            if (Modifier.isPrivate(m.getModifiers())) {
                throw new RuntimeException("Method: '" + m + "' has an @Invariant annotation - must not be private");
            }
            List<Method> methods = invariants.get(invariant.value());
            if (methods == null) {
                throw new RuntimeException("Method: '" + m + "' has an @Invariant annotation which has " +
                        "an invalid object pool name: '" + invariant.value() + "'");
            }
            methods.add(m);
            m.setAccessible(true);
        }
    }
    
    private List<ProbabilitiesManager> initMethods() {
        List<ProbabilitiesManager> managers = new ArrayList<ProbabilitiesManager>();
        int count = 0;
        for (Method m : getClass().getDeclaredMethods()) {
            Prob prob = m.getAnnotation(Prob.class);
            if (prob == null) { continue; }
            if (Modifier.isPrivate(m.getModifiers())) {
                throw new RuntimeException("Method: '" + m + "' has a @Prob annotation - must not be private");
            }
            if (m.getParameterTypes().length > 0) {
                Params params = m.getAnnotation(Params.class);
                if (params == null) {
                    throw new RuntimeException("Misconfigured method declaration: Method '" + m + "' has " +
                            "a @Prob annotation, accepts arguments, but has not @Params annotation. " +
                            "Declare a @Params annotation that defines a name of an object pool from which" +
                            " to inject the parameters for the method");
                }
                if (params.value().length != m.getParameterTypes().length) {
                    throw new RuntimeException("Illegal number of elements in Params annotation; was " +
                            params.value().length + ", method needs " + m.getParameterTypes().length +
                            " parameters. Method signature=" + m);
                }
                for (String param : params.value()) {
                    if (!pools.containsKey(param)) {
                        throw new RuntimeException("Undefined pool name in Params annotation: '" + param + "', " +
                                "declared in method: " + m);
                    }
                }
            }
            count++;
            double[] probs = prob.value();
            for (int i = 0; i < probs.length; i++) {
                if (managers.size() <= i) {
                    managers.add(new ProbabilitiesManager());
                }
                ProbabilitiesManager manager = managers.get(i);
                manager.addMethod(m, probs[i]);
            }
        }
        if (count == 0) {
            throw new AssertionFailedError("No test method found");
        }
        return managers;
    }
    
    /**
     * Returns the LogStrategy that handles the produced log entries of this randomized test.
     */
    public LogStrategy getLogStrategy() {
        return logStrategy;
    }
    
    /**
     * This method is called by JUnit and fires the randomized test.
     */
    public final void test() {
        try {
            executedSteps = 0;
            while (executedSteps < steps) {
                if (invokeMethod()) {
                    executedSteps++;
                    onStep(executedSteps);
                }
            }
        } catch (TestFailedException e) {
            examineException(e);
            throw e;
        }
    }
    
    protected int getCurrentStep() {
        return executedSteps;
    }
    
    /**
     * Implement this to examine a thrown TestFailedException. For example, the log of the
     * exception can be traced.
     */
    protected void examineException(TestFailedException e) { }
    
    private boolean invokeMethod() {
        ProbabilitiesManager manager = probManagers.get(currentPhase);
        Method m = manager.chooseMethod(random);
        if (m == null) {
            if (m == null) {
                throw new RuntimeException("All probabilities are zero, currentPhase=" + currentPhase);
            }
        }
        Class[] types = m.getParameterTypes();
        Object[] args = new Object[0];
        Params params = null;
        if (types.length > 0) {
            params = m.getAnnotation(Params.class); //guaranteed to return non-null
            String[] poolNames = params.value();
            args = new Object[poolNames.length];
            for (int i = 0; i < args.length; i++) {
                List<Object> pool = pools.get(poolNames[i]); //guaranteed to return non-null
                if (pool.isEmpty()) {
                    return false;
                }
                args[i] = pool.get(random.nextInt(pool.size()));
            }
        }
        try {
            Object o = m.invoke(this, args);
            Creates creator = m.getAnnotation(Creates.class);
            if (creator != null) {
                try {
                    for (String poolName : creator.value()) {
                        pools.get(poolName).add(filterNewObject(poolName, o));
                    }
                } catch (PreconditionFailedException e) {
                    return false;
                }
            }
            if (args.length > 0) { //check invariants
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        continue; //ignore invariants for null
                    }
                    String type = params.value()[i];
                    checkInvariants(args[i], type);
                }
            }
            if (o != null && creator != null) {
                for (String poolName : creator.value()) {
                    checkInvariants(o, poolName);
                }
            }
            PooledObject[] pooledArgs = createPooledObjects(args, params != null ? params.value() : new String[0]);
            String[] pools = creator != null ? creator.value() : new String[0];
            logStrategy.appendLog(new MethodInvocationLog(m, pooledArgs, o, pools));
        } catch (IllegalAccessException e) {
            throw new Error("Could not invoke method - maybe a null was attempted to be used as a primitive?", e); //will never happen - already checked that method is public
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof PreconditionFailedException) {
                //ignore failed preconditions
                return false;
            }
            MethodInvocationLog methodLog = new MethodInvocationLog(m,
                    createPooledObjects(args, params != null ? params.value() : new String[0]), null, new String[0]);
            try {
                throw e.getCause();
            } catch (PostconditionFailedException post) {
                //this is a bug
                throw new TestFailedException("Failed postcondition while invoking method: '" + m + "' " +
                        "with args: " + Arrays.asList(args),
                        methodLog, post, logStrategy);
            } catch (InvariantFailedException invEx) {
                //user called invariant() or threw manually an InvariantFailedException
                throw new TestFailedException("Failed invariant while invoking method: '" + m + "' " +
                        "with args: " + Arrays.asList(args),
                        methodLog, invEx, logStrategy);
            }  catch (TestFailedException invEx) {
                //user called checkInvariants
                throw invEx;
            } catch (Throwable t) {
                //this is also a bug
                throw new TestFailedException("Exception thrown while invoking method: '" + m + "' " +
                        "with args: " + Arrays.asList(args) + ", for which" +
                        " no precondition failed", methodLog, t, logStrategy);
            }
        }
        return true;
    }
    
    private PooledObject[] createPooledObjects(Object[] args, String[] poolNames) {
        PooledObject[] pooled = new PooledObject[args.length];
        for (int i = 0; i < args.length; i++) {
            pooled[i] = new PooledObject(args[i], poolNames[i] );
        }
        return pooled;
    }
    
    /**
     * Runs all invariant testing methods (marked with &#064;Invariant) that are associated with a object
     * pool, providing as argument to the testing methods the specified object. This method is called
     * automatically after each random method execution, once for each method's parameters, and one time
     * for each addition to an object pool of the returned value. That is, if a method is marked with
     * &#064;Creates({"pool1", "pool2"}), the return value will be checked against the invariants of both
     * pool1 and pool2.
     *
     * Test implementations are free to call this method wherever it is appropriate, to test invariants
     * for objects for which they are not tested automatically (i.e., if the objects were not injected
     * as parameters or were not a return value, but were accessed with other means).
     */
    protected final void checkInvariants(Object o, String poolName) {
        if (!invariants.containsKey(poolName)) {
            throw new IllegalArgumentException("Invalid pool name defined: '" + poolName
                    + "', legal values=" + invariants.keySet());
        }
        for (Method invMethod : invariants.get(poolName)) {
            List<Object> pool = pools.get(poolName); //guaranteed to return non-null AND non-empty
            try {
                invMethod.invoke(this, new Object[] { o });
            } catch (InvocationTargetException e) {
                MethodInvocationLog methodLog = new MethodInvocationLog(invMethod,
                        new PooledObject[] { new PooledObject(o, poolName) }, null, new String[0]);
                try {
                    throw e.getCause();
                } catch (InvariantFailedException ex) {
                    throw new TestFailedException("Failed invariant while invoking method: '" + invMethod + "' " +
                            "with argument: " + o, methodLog, ex, logStrategy);
                } catch (Throwable t) {
                    throw new TestFailedException("Invariant-checking method: '" + invMethod +"' caused an error",
                            methodLog, t, logStrategy);
                }
            } catch (IllegalAccessException e) {
                throw new Error(e); //will never happen
            }
        }
    }
    
    /**
     * Tests a precondition. If the condition is false, a PreconditionFailedException is thrown.
     *
     * @param condition the precondition to test
     */
    public void precondition(boolean condition) throws PreconditionFailedException {
        if (!condition) {
            throw new PreconditionFailedException();
        }
    }
    
    /**
     * Tests a precondition. If the condition is false, a PreconditionFailedException is thrown.
     *
     * @param condition the precondition to test
     * @param message a message that describes the precondition tested
     */
    public void precondition(boolean condition, String message) throws PreconditionFailedException {
        if (!condition) {
            throw new PreconditionFailedException(message);
        }
    }
    
    /**
     * Tests a postcondition. If the condition is false, a PostconditionFailedException is thrown.
     *
     * @param condition the postcondition to test
     */
    public void postcondition(boolean condition) throws PostconditionFailedException {
        if (!condition) {
            throw new PostconditionFailedException();
        }
    }
    
    /**
     * Tests a postcondition. If the condition is false, a PostconditionFailedException is thrown.
     *
     * @param condition the postcondition to test
     * @param message a message that describes the postcondition tested
     */
    public void postcondition(boolean condition, String message) throws PostconditionFailedException {
        if (!condition) {
            throw new PostconditionFailedException(message);
        }
    }
    
    /**
     * Tests an invariant. If the invariant does not hold, an InvariantFailedException is thrown.
     *
     * @param condition the invariant to test
     */
    public void invariant(boolean condition) throws InvariantFailedException {
        if (!condition) {
            throw new InvariantFailedException();
        }
    }
    
    /**
     * Tests an invariant. If the invariant does not hold, an InvariantFailedException is thrown.
     *
     * @param condition the invariant to test
     * @param message a message that describes the invariant tested
     */
    public void invariant(boolean condition, String message) throws InvariantFailedException {
        if (!condition) {
            throw new InvariantFailedException(message);
        }
    }
    
    /**
     * Returns an object pool by name, or null if no such pool is defined (all pools are defined at
     * the construction time of this test, and may be empty but not null). The returned pool can be
     * modified in any way.
     *
     * @param name the name of the object pool to return
     * @return the corresponding object pool
     */
    protected List<Object> getPool(String name) {
        return pools.get(name);
    }
    
    /**
     * Returns an unmodifiable set of all object pool names that are defined in this test.
     */
    protected Set<String> getPoolNames() {
        return Collections.unmodifiableSet(pools.keySet());
    }
    
    /**
     * Called after every executed step. A step is regarded as executed when arguments are found to be injected
     * and no precondition fails.
     */
    protected void onStep(int executedSteps) { }
    
    /**
     * Called when an object is going to be added to a pool. Whatever is returned from this method
     * is finally added to the specified pool.
     */
    protected Object filterNewObject(String pool, Object object) throws PreconditionFailedException {
        return object;
    }
    
    /**
     * Changes the current phase of the randomized test. This controls the probabilities of the test methods,
     * as each method is annotated with an array of probabilities, one probability for each phase. Phase must be
     * >= 0, and may not be greater than the length of the longer array of probabilities defined in the random
     * methods.
     *
     * @param phase the phase to switch into
     */
    protected void setPhase(int phase) {
        if (phase < 0) {
            throw new IllegalArgumentException("Phase cannot be negative");
        }
        if (phase >= probManagers.size()) {
            throw new IllegalArgumentException("Specified non-existent phase index: "
                    + phase + ", total phases=" + probManagers.size());
        }
        currentPhase = phase;
    }
    
    /**
     * Returns the current phase of the test.
     */
    public int getPhase() {
        return currentPhase;
    }
    
    
    private static class ProbabilitiesManager {
        private double maxProb = 0.0;
        private final Map<Method, Double> probs = new LinkedHashMap<Method, Double>();
        
        void addMethod(Method m, double prob) {
            m.setAccessible(true);
            if (m == null) {
                throw new IllegalArgumentException("Null method");
            }
            if (prob < 0.0) {
                throw new IllegalArgumentException("Illegal probability: " + prob);
            }
            maxProb += prob;
            probs.put(m, prob);
        }
        
        public Method chooseMethod(Random random) {
            double next = random.nextDouble() * maxProb;
            double curr = 0.0;
            Method m = null;
            for (Entry<Method, Double> entry : probs.entrySet()) {
                m = entry.getKey();
                double prob = entry.getValue();
                curr += prob;
                if (curr > next) {
                    return m;
                }
            }
            return m;
        }
    }
}
