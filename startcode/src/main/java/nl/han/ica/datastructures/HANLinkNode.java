package nl.han.ica.datastructures;

public class HANLinkNode<T> {
    private T value;
    private HANLinkNode<T> next;

    public HANLinkNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public HANLinkNode<T> getNext() {
        return this.next;
    }

    public void setNext(HANLinkNode<T> listNode) {
        this.next = listNode;
    }
}
