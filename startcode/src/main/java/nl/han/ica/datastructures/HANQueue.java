package nl.han.ica.datastructures;

public class HANQueue<T> implements IHANQueue<T>{
    private IHANLinkedList<T> linkedList;

    @Override
    public void clear() {
        linkedList.clear();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void enqueue(T value) {
        linkedList.addFirst(value);
    }

    @Override
    public T dequeue() {
        linkedList.removeFirst();
        return null;
    }

    @Override
    public T peek() {
        return null;
    }

    @Override
    public int getSize() {
        return 0;
    }
}
