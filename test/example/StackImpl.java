package example;

import java.util.LinkedList;

public class StackImpl implements Stack {
    private final int capacity;
    private final LinkedList<Integer> stack = new LinkedList<Integer>();
    
    public StackImpl(int capacity) {
        this.capacity = capacity;
    }
    
    public void push(int value) throws StackFullException {
        if (isFull()) {
            throw new StackFullException();
        }
        stack.addLast(value);
    }
    
    public int pop() throws StackEmptyException {
        if (isEmpty()) {
            throw new StackEmptyException();
        }
        return stack.removeLast();
    }
    
    public int capacity() {
        return capacity;
    }
    
    public int size() {
        return stack.size();
    }
    
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    
    public boolean isFull() {
        return stack.size() == capacity;
    }
    
    public int peek() throws StackEmptyException {
        if (isEmpty()) {
            throw new StackEmptyException();
        }
        return stack.getLast();
    }
}
