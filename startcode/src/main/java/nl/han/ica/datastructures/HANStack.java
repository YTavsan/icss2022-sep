package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T>{
    IHANLinkedList<T> linkedList;

    public HANStack(){
        linkedList = new HANLinkedList<T>();
    }

    @Override
    public void push(T value) {
        linkedList.addFirst(value);
    }

    @Override
    public T pop() {
        T topItem = linkedList.getFirst();
        linkedList.delete(0);
        return topItem;
    }

    @Override
    public T peek() {
        return linkedList.getFirst();
    }
}
