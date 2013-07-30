package example;

public interface Stack {
    void push(int value) throws StackFullException;
    int pop() throws StackEmptyException;
    int capacity();
    int size();
    boolean isEmpty();
    boolean isFull();
    int peek() throws StackEmptyException;
}