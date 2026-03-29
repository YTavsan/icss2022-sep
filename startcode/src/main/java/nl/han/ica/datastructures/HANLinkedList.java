package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private HANLinkNode<T> node;

    @Override
    public void addFirst(T value) {
        if (node == null) {
            node = new HANLinkNode<>(value);
        } else {
            HANLinkNode<T> newFirst = new HANLinkNode<>(value);
            newFirst.setNext(node);
            node = newFirst;
        }
    }

    @Override
    public void clear() {
        node = null;
    }

    @Override
    public void insert(int index, T value) {
        if (index > getSize()) {
            return;
        }

        if (index == 0 || node == null) {
            addFirst(value);
            return;
        }

        HANLinkNode<T> current = node;

        for (int i = 0; i < index - 1; i++) {
            current = current.getNext();
        }
        HANLinkNode<T> toInsert = new HANLinkNode<>(value);
        toInsert.setNext(current.getNext());
        current.setNext(toInsert);
    }

    @Override
    public void delete(int pos) {
        if (pos > getSize()) {
            return;
        }
        if (pos == 0) {
            removeFirst();
            return;
        }
        HANLinkNode<T> current = node;

        for (int i = 0; i < pos - 1; i++) {
            current = current.getNext();
        }
        current.setNext(current.getNext().getNext());
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || getSize() == 0) {
            return null;
        }
        HANLinkNode<T> current = node;

        for (int i = 0; i < pos; i++) {
            current = current.getNext();
        }
        return current.getValue();
    }

    @Override
    public void removeFirst() {
        if (node != null) {
            node = node.getNext();
        }
    }

    @Override
    public T getFirst() {
        return node.getValue();
    }

    @Override
    public int getSize() {
        int size = 0;
        HANLinkNode<T> currentNode = node;

        while (currentNode != null) {
            size++;
            currentNode = currentNode.getNext();
        }
        return size;
    }
}
