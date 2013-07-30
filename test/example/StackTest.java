package example;

import example.Stack;
import example.StackImpl;
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