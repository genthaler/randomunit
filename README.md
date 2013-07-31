# RandomUnit Tutorial

A randomized unit testing tool

A work by Andreou Dimitris

Email: andreou csd dot uoc dot gr

Homepage: <http://www.csd.uoc.gr/~andreou>

## 1. Tutorial Introduction

### 1.1. Unit-Testing In General

Unit testing is a proved and ubiquitous practice throughout the software industry. In the community of Java developers, JUnit is a very popular testing framework, with wide support from tools and IDEs. As of itself, it is very basic - just providing the necessary machinery to easily set up tests, making assertions, and running suites. Fortunately, it can be extended to fill more specialized needs (for instance, see JMock framework).

Yet, the problem with unit testing in general is that it can depend heavily on the exact data that are used in the test. The programmer creates some objects, with very static (hard-coded) relations and method invocations. If the test pass, she hopes that all participants (objects, methods) in the test work ok. But this is the most important and arguable assumption about unit-testing: how can the "no assertion failures" result from a single test fixture be expanded to mean "every participant works fine generally", and not only in the specific fixture? How much confidence can one gain about the functionality of a module, given that some specific scenarios actually work? Well, as long as a unit test is not a formal proof, this problem is inherent in unit-testing. The only attainable goal is to gain as much confidence as practically possible.

This typically means writing more tests, so to make less room for obvious bugs, but the more subtle bugs can be easily missed even with a sizeable number of tests. Many bugs are reported on the field, and more tests are written after-the-fact, to expose these bugs and eliminate them.

Randomized tests can be of help in this procedure. Given that single test methods typically run very fast, one would be willing to run thousands (and more) of such test methods, if that would help her find more bugs and gain more confidence on the software under testing. Creating thousands of test by hand is rather ...impractical though, to put it so. What is really missing is a tool to automatically create tests and run them. This framework, RandomUnit, attempts to successfully meet these ends.

### 1.2. RandomUnit

RandomUnit's approach is to rely on the concepts of "Design by Contract" (DbC), ie preconditions, postconditions, and invariants. RandomUnit assumes that a programmer is able to translate the specified pre/post-conditions and invariants of a class, in java code (as assertions). The translation must be exact, or else false alarms can occur (when for example, a postcondition is coded as more strict than its specification), or bugs be hidden (by analogous mismatches).

How can a framework call random methods in a class? Methods take parameters, and parameters have specific semantics assosiated with them. For instance, consider a method that takes a single int parameter, and expects it (as a precondition) to be either 0, 1, or 2. A framework cannot simply go and try random int values, obviously. On the other hand, it doesn't know that this method expects such an int parameter, while the other method accepts any int as valid. These semantics needs to be captured. A very flexible solution is the creation of named object pools, which contain objects with similar semantics (for example, one pool can hold all instances on the primary class that is being tested, another pool for Integers that can be applied to certain methods, etc). Imagine an ecosystem of objects that are divided into groups (pools), and at each step a random action is taken, that involves some objects from these. This is the actual RandomUnit's model for randomized testing, which actually enables integration as well as unit testing.

So, for a high-level view, a randomized test has a bunch of methods with associated probabilities, and each of them checks preconditions, test, checks postconditions, and/or create objects, or test invariants for objects. The test then picks randomly methods and invokes them, until it successfully invokes a given number of operations (typically thousands). If a postcondition or an invariant fail, or an unexpected exception is thrown, a bug is identified. (Note that preconditions are expected to fail as the parameters are random). Yet, to really expose a bug, rather than just proving that "there is a bug out there somewhere", a logging facility is provided, that logs each method invocation appropriately.

### 1.3 Requirements

RandomUnit requires JUnit version 3.8.1 or above, and JDK 1.5 (tiger) or above.

## 2. Creating and Running a Randomized Test

(Note: this section assumes you are able to run JUnit tests. If you can't, you should check JUnit documentation, or use some tool or IDE that automates the task).

In order to explore the aspects of RandomUnit, we will walk through a simple example. Lets say we need to test our new Stack implementation, defined as:

    Stack.java
    
    interface Stack {
        void push(int value) throws StackFullException;
        int pop() throws StackEmptyException;
        int capacity();
        int size();
        boolean isEmpty();
        boolean isFull();
        int peek() throws StackEmptyException;
    } 

At first, we shall need to define a subclass or randomunit.RandomizedTestCase. Create a file named StackTest.java with the following code:

    StackTest.java
        
    import randomunit.RandomizedTestCase;
    import randomunit.SimpleLogStrategy;
    
    public class StackTest extends RandomizedTestCase {
        public StackTest(String testName) {
            super(testName, 1000, new SimpleLogStrategy(5));
        }
    } 

This creates a normal JUnit test case. The second and the third arguments in the super() call need explanation. We just declared that we want the test to execute 1000 random actions, and use a special logging strategy. Logging is a crucial component if one needs to trace the cause of a bug. Also, log contents are printed by default, when a failure occurs, so it is important to choose an adequate LogStrategy implementation. LogStrategy definition is shown here:

    randomunit.LogStrategy
    
    interface LogStrategy {
        void appendLog(MethodInvocationLog log);
        String dump();
    } 

randomunit.MethodInvocationLog is a class that has the corresponding java.lang.reflect.Method instance of the method of the test that was executed, along with actual parameters and returned value. Available LogStrategy implementations are discussed later. Please note though that RandomUnit's "logging" concept is independent of general purpose logging frameworks, like log4j. Integration is possible through the LogStrategy interface.

Back to the example. We are ready to start creating test methods. Lets assume that we have implemented Stack in a class named StackImpl, as follows:

    StackImpl.java
    
    public class StackImpl implements Stack { 
        public StackImpl(int capacity) { ... }
            ... //implementation methods omitted
    } 

Lets create a method that creates Stack instances with random capacities:

    StackTest.java
    
    import randomunit.Creates; 
    import randomunit.Prob; 
    import randomunit.RandomizedTestCase; 
    import randomunit.SimpleLogStrategy;
    
    public class StackTest extends RandomizedTestCase {
        public StackTest(String testName) {
            super(testName, 1000, new SimpleLogStrategy(5));
        }
    
        @Prob(1)
        @Creates("stacks")
        public Stack randomNewStack() {
            int capacity = this.random.nextInt(10);
            return new StackImpl(capacity);
        }
    } 

The @Prob annotation declares that this method should be executed randomly. It also specifies the probability which is used when selecting a method to execute. The probabilities are relative, in the sense that if we have two methods with equal probability (not necessarily 0.5), they should run 50%-50%, and so on. (More can be expressed with a @Prob annotation, a topic to be discussed later). The @Creates annotation declares that when this method runs, whatever is the return value, store it in an object pool named "stacks". The method itself creates stacks of random capacity, in the range [0, 10). Variable random is actually an inherited Random instance used by the randomized test, declared with protected access, and can be used to create random numbers. It is always seeded at a constant number, so the tests (and their exact failures) are reproducable.

In this trivial example, a stack doesn't really care about its contents: any int will do. But since this should rarely be the case in practice, the framework could not simply create random integers and use them - we need to create ourselves the ints that we will use:

    StackTest.java
    
    import randomunit.Creates; 
    import randomunit.Prob; 
    import randomunit.RandomizedTestCase; 
    import randomunit.SimpleLogStrategy;
    
    public class StackTest extends RandomizedTestCase {
        public StackTest(String testName) {
            super(testName, 1000, new SimpleLogStrategy(5));
        }
    
        @Prob(1)
        @Creates("stacks")
        public Stack randomNewStack() {
            int capacity = this.random.nextInt(10);
            return new StackImpl(capacity);
        }
    
        @Prob(1)
        @Creates("ints")
        public int randomNewInt() {
            return random.nextInt(1000);
        }
    } 

These were just simple factory methods. Lets move on to the interesting part and do some testing. We will start by pushing some integers in stacks and see if anything is broken:

    StackTest.java 
    
    import randomunit.Creates; 
    import randomunit.Prob; 
    import randomunit.Params; 
    import randomunit.RandomizedTestCase; 
    import randomunit.SimpleLogStrategy;
    
    public class StackTest extends RandomizedTestCase {
        public StackTest(String testName) {
            super(testName, 1000, new SimpleLogStrategy(5));
        }
        
        @Prob(1)
        @Creates("stacks")
        public Stack randomNewStack() {
            int capacity = this.random.nextInt(10);
            return new StackImpl(capacity);
        }
        
        @Prob(1)
        @Creates("ints")
            public int randomNewInt() {
            return random.nextInt(1000);
        }
        
        @Prob(2)
        @Params({"stacks", "ints"})
        public void randomTestPush(Stack stack, int value) {
            precondition(!stack.isFull());
            
            int previousSize = stack.size();
            stack.push(value);
            
            postcondition(!stack.isEmpty());
            postcondition(stack.size() == previousSize + 1);
            postcondition(stack.peek() == value);
        }
    }

Much is going on here. Lets start with the @Params annotation. It specifies a list of names of object pools, from which to pick random elements and use it as parameters for this method. So, this is actually a form of dependency-injection: the method declares what objects it needs to do its testing, and the framework provides them at runtime. As you may have guessed, these objects will be some from the ones we create with the two creator methods. Note that if a non-existent object pools is specified (ie, there is no creator method that inserts elements to such a pool), an exception is thrown that explains the fact; this is helpful to prevent errors by typos in the annotations.

So, with the parameter injection mechanism, the test itself has hardly any reason to store state; everything is provided as method parameters, which makes the coding cleaner and more readable. Also, please note that the parameter types do not matter, as long as the randomly selected object can be injected in the parameter; for example, we could also use "Object" as the type of the first parameter, and "Integer", "Number", "Comparable" etc for the second object.

The method implementation has to check its parameters at the start of its testing; remember that the objects are chosen randomly from their container pools, so the could just not be applicable to the specific test method. We declare the preconditions that must hold for us to actually perform a test. If a precondition fails, the method is skipped like it was never happened. If we wanted to test that when a stack is full, attempt to push something throws an exception, then the precondition of the test would be the exact opposite: precondition(stack.isFull()); So we would essentially filter out non-full stacks for this test.

Then, we proceed with the actual testing. We push a value, after we record its current size. If an exception occurs here, we have found a bug - a valid, non-full stack that does not accepts an element. Then, we test the postconditions that we would like to have. The stack cannot be empty after a push, the stack grows in size by one, and when we peek the top element, we get back the value that we just pushed. If any of these fails, we found another bug.

Note that we do not call any assertXXX method of JUnit. We could have (an assertion error would be treated as a bug), but using precondition() and postcondition() communicates the purpose of the assertion in a much better and natural way.

What if we wanted to create objects based on other, existing objects? No trouble at all - just combine @Creates with @Params, and custom method parameters. The following would create stacks with capacity equal to some other stack:

    StackTest.java 
    
    @Prob(1) 
    @Creates("stacks") 
    @Params("stacks") 
    public Stack randomNewStack(Stack stack) {
        return new StackImpl(stack.capacity()); 
    }

In this example, this would hardly provide any value, but imagine how one could tap this expressiveness to create elaborate inter-dependent objects. Note: obviously, at least on creator method must take zero arguments and have positive probability, and every object pool eventually should be able to obtain objects. If a method with arguments is picked for execution, and some object pool on which it depends is empty, the method execution is skipped.

Another interesting point is that we can add a created object into many pools at once. @Creates actually takes an array of pool names. This could be done as following:

    StackTest.java 
    @Prob(1) @Creates( { "stacks", "fancyStacks" } ) 
    @Params("stacks") 
    public Stack randomNewStack(Stack stack) { 
        return new StackImpl(stack.capacity()); 
    }

Another aspect we would like to check is invariants. We could simply create a method that accepts a Stack and tests its invariants, and remember to call it in everymethod that we use a stack, but this would be error-prone (the call could be omitted) and needlessly tiresome. We can mark special methods that check invariants so the framework can do the cumbersome work for us:

    import randomunit.Invariant;
    import randomunit.Creates;
    import randomunit.Prob;
    import randomunit.Params;
    import randomunit.RandomizedTestCase;
    import randomunit.SimpleLogStrategy;
    
    public class StackTest extends RandomizedTestCase {
        public StackTest(String testName) {
            super(testName, 1000, new SimpleLogStrategy(5));
        }
    
        @Prob(1)
        @Creates("stacks")
        public Stack randomNewStack() {
            int capacity = this.random.nextInt(10);
            return new StackImpl(capacity);
        }
    
        @Prob(1)
        @Creates("ints")
        public int randomNewInt() {
            return random.nextInt(1000);
        }
    
        @Prob(2)
        @Params({"stacks", "ints"})
        public void randomTestPush(Stack stack, int value) {
            precondition(!stack.isFull());
    
            int previousSize = stack.size();
            stack.push(value);
    
            postcondition(!stack.isEmpty());
            postcondition(stack.size() == previousSize + 1);
            postcondition(stack.peek() == value);
        }
    
        @Invariant("stacks")
        public void checkStackInvariant(Stack stack) {
            invariant(stack.isEmpty() ^ stack.size() > 0);
            invariant(stack.isFull() ^ stack.size() < stack.capacity());
            if (!stack.isEmpty()) {
                stack.peek(); //should not throw exception
            }
        }
    } 

This enforces the invariants of the stack, namely: - It can be either empty, or its size must be greater than zero (exclusive or) - It can be either full, or its size must be less than its capacity (exclusive or) - If it is not empty, we can peek without getting an exception

@Invariant annotation accepts a single object pool name, and the annotated method must take exactly one corresponding parameter. We can define as many @Invariant methods as we like, even for the same object pool. All invariant checking methods that apply to an object pool are checked against: - Every object that is added to its pool - Every object that has just been used as an injected parameter in a random test method (after the method's completion).

These rules are sufficient to assure that every object has tested invariants before uses of it. Of course, this would not be the case if the objects that collaborate in test are not taken as injected parameters. As expected, a failed invariant indicates a bug, and an explaining exception is thrown.

What would happen if we take a handle on an arbitrary object, neither taken from the parameter list of the method nor returned by the method? We might want to apply invariant checks against such an object, and these checks cannot happen automatically (it's an arbitrary object, not relevant to this method execution, from the framework's point of view). A simple utility is provided, that can be invoked like this:

    StackTest.java 
    @Prob(1) 
    @Params("stacks") 
    public void randomStackFriend(Stack stack) { 
        Stack friend = findMyFriendStack(stack); 
        checkInvariants(friend, "stacks"); 
    }

In the example, we first create a reference to another Stack instance (somehow, it doesn't matter), and then we call the checkInvariants inherited method, providing as arguments the object and the pool name with the desired associated invariants checks. This results in the invocation of every @Invariant("stacks") method that we have defined against the provided object.

## 3. Special Topics

### 3.1 Logging

The logging component of the framework is an essential aid in the investigation of the causes on failed assertions (i.e., bugs). The LogStrategy.dump() method is used by the failure exception (randomunit.TestFailedException) to print out information about what had happened before the error occured. The framework comes with two implementations of LogStrategy: randomunit.SimpleLogStrategy and randomunit.DetailedLogStrategy.

a) SimpleLogStrategy maintains a single buffer of method invocation entries, with a constant size. For instance, it can be used to always remember the 10 most recent random test invocations. While simple, the problem with this log is that it will print irrelevant method invocations too - methods that had nothing to do with the objects that failed in a specific method. Yet, its memory requirement is far easier to understand.

b) DetailedLogStrategy maintains a similar bounded buffer for every pooled object, per pool. When a random method (i.e., with a @Prob annotation) completes normally, a log is appended at all objects that are used as parameters, and the object that is returned, if the method happens to be a creator method. Note that the buffers are associated with a (poolName, object) entry, so that, for example, an Integer with value 1 would have a different buffer for every pool it is contained. This essential means that this log strategy is able to provide the recent history for every object that collaborated in a failed test method.

By default, method invocation logs are printed in the form: "methodName(arg1, arg2...)-->returnValue", for example: "checkStackInvariant(Stack@1ff7a1e)-->null" (methods that return void are printed like they returned null), or "randomNextInt()-->235".

### 3.2. Phases

You may have noted that the methods are selected randomly according to their declared probability - but not discrimination is made for creator methods and only-testing methods. What if you wanted to have a starting phase in the test, at which you would only create collaborators, and then concentrate the test only in testing these objects? This can be also expressed in the framework by using phases. All probabilities that have been shown to this point are defined for the phase 0, the default phase. @Prob actually declares an array of doubles - all this time we were just declaring an array with only a single element. We can reform our test to firstly create stacks and ints, in a 70%-30% ratio, and only then test the objects like this: import randomunit.Invariant; import randomunit.Creates; import randomunit.Prob; import randomunit.Params; import randomunit.RandomizedTestCase; import randomunit.SimpleLogStrategy;

    public class StackTest extends RandomizedTestCase {
        public StackTest(String testName) {
            super(testName, 1000, new SimpleLogStrategy(5));
        }
    
        @Prob( { 70, 0 } )
        @Creates("stacks")
        public Stack randomNewStack() {
            int capacity = this.random.nextInt(10);
            return new StackImpl(capacity);
        }
    
        @Prob( { 30, 0 } )
        @Creates("ints")
        public int randomNewInt() {
            return random.nextInt(1000);
        }
    
        @Prob( { 0, 1 } )
        @Params({"stacks", "ints"})
        public void randomTestPush(Stack stack, int value) {
            precondition(!stack.isFull());
    
            int previousSize = stack.size();
            stack.push(value);
    
            postcondition(!stack.isEmpty());
            postcondition(stack.size() == previousSize + 1);
            postcondition(stack.peek() == value);
        }
    
        @Invariant("stacks")
        public void checkStackInvariant(Stack stack) {
            invariant(stack.isEmpty() ^ stack.size() > 0);
            invariant(stack.isFull() ^ stack.size() < stack.capacity());
            if (!stack.isEmpty()) {
                stack.peek(); //should not throw exception
            }
        }
    } 

For the first phase, only the two creator methods have positive probabilities, and during the second phase, only the randomTestPush has a positive probability (so it will be always selected).

This is not enough, though. We need to tell the framework to switch phases at some specific time. A nice place is to override the callback method which is defined (as protected) in RandomizedTestCase:

protected void onStep(int executedSteps) { } 
So, we can say for example, that when executedSteps == 30, switch phase to 1. We can do it like this:

    protected void onStep(int executedSteps) {
        if (executedSteps == 30) {
            //switch to phase 1
            this.setPhase(1);
        }
    } 

The method setPhase(int) is naturally defined in RandomizedTestCase.

### 3.3. Further Customization

Some more useful methods are defined, which can be overrided to further customize a test.

You can centrally control (or monitor, or whatever) which objects are added into pools by overriding the definition of this method:

    protected Object filterNewObject(String pool, Object o) throws PreconditionFailedException {
        return o;
    }

For instance, you can exclude odd integers by this code:

    protected Object filterNewObject(String pool, Object o) {
        if (o instanceof Integer) {
            Integer i = (Integer)o;
            precondition(i % 2 == 0);
        }
    
        return o;
    } 

Or you may replace the created object by another arbitrary object.

Another method you can override is this:

    protected void examineException(TestFailedException e) {
    } 

This is called just before an error report is generated. You could programmatically examine the log that is included in the exception. Or, if you need to set a breakpoint in a debugger, this would be a good candidate!

You can have full control over the object pools too. These methods are provided:

    protected Set<String> getPoolNames();
    
    protected List<Object> getPool(String name); 

So, you could even load the pools with predefined objects. Yet, a compromise is being made: you can't create arbitrary pools; pools are only created when a method tagged with @Creates is found. Were the creation of pools allowed arbitrarily, a typo error in the declaration of @Params could be only found during runtime - in this version, these types of errors are reported at the construction of the test. A work-around is to declare a creator method with @Prob(0), and preload the pools with objects in the setUp() method of JUnit.

## 4. Best Practices

RandomUnit is not intended as a replacement for writing deterministic tests. Simple corner cases should be always be tested in the usual manner (you can place testXXX methods in the subclass of RandomizedTestCase and they will be picked up by JUnit as expected). Randomized test are useful when the objects to be tested has complex behaviour that depends on input parameters and on object's internal state. It is difficult to manually create objects in a sufficient number of states to perform the same test for all of them; RandomUnit comes handy in such cases.

You should carefully think about what probabilities to use. You should not create too many objects, if you do not want to end up testing only their starting states. (Fewer objects mean each one has more probability to be selected again and again). Also, the number of rounds to execute is a parameter that needs to be configured carefully. It should be a sufficiently high number, but too high means more CPU time (and possible more objects), without any added value. The complexity of the tested objects should really be the driving factor for this.

A note on naming: methods are inspected with reflection both by JUnit and by RandomUnit. So, a method with a @Prob annotation which begins with the prefix "test" and takes no parameters will be considered twice. This should be rarely intended, and should be avoided. The prefix "random" could be used as a prefix for random test methods, although not needed (such methods are identified by the @Prob annotation).

## 5. Contribution

Feedback is mostly welcomed, as well as contributions in the form of more tutorials, more examples, ideas to make this project more useful, etc...

