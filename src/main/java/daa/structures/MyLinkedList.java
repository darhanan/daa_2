package daa.structures;

import daa.metrics.Metrics;

public class MyLinkedList implements IntList {
    private static final class Node {
        int value;
        Node prev;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public void add(int x) {
        linkLast(x);
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        if (index == size) {
            linkLast(x);
        } else {
            linkBefore(x, node(index));
        }
    }

    @Override
    public int remove(int index) {
        checkIndex(index);
        Node target = node(index);
        unlink(target);
        return target.value;
    }

    @Override
    public int get(int index) {
        checkIndex(index);
        return node(index).value;
    }

    @Override
    public boolean contains(int x) {
        for (Node cur = head; cur != null; cur = cur.next) {
            metrics.step();
            metrics.compare();
            if (cur.value == x) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int[] toArray() {
        int[] copy = new int[size];
        int i = 0;
        for (Node cur = head; cur != null; cur = cur.next) {
            copy[i++] = cur.value;
        }
        return copy;
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    private Node node(int index) {
        Node cur;
        if (index < (size >> 1)) {
            cur = head;
            metrics.step();
            for (int i = 0; i < index; i++) {
                cur = cur.next;
                metrics.step();
            }
        } else {
            cur = tail;
            metrics.step();
            for (int i = size - 1; i > index; i--) {
                cur = cur.prev;
                metrics.step();
            }
        }
        return cur;
    }

    private void linkLast(int x) {
        Node created = new Node(x);
        created.prev = tail;
        metrics.move();
        if (tail == null) {
            head = created;
        } else {
            tail.next = created;
        }
        metrics.move();
        tail = created;
        metrics.move();
        size++;
    }

    private void linkBefore(int x, Node succ) {
        Node pred = succ.prev;
        Node created = new Node(x);
        created.prev = pred;
        created.next = succ;
        metrics.moves(2);
        succ.prev = created;
        metrics.move();
        if (pred == null) {
            head = created;
        } else {
            pred.next = created;
        }
        metrics.move();
        size++;
    }

    private void unlink(Node target) {
        Node pred = target.prev;
        Node succ = target.next;
        if (pred == null) {
            head = succ;
        } else {
            pred.next = succ;
        }
        metrics.move();
        if (succ == null) {
            tail = pred;
        } else {
            succ.prev = pred;
        }
        metrics.move();
        size--;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
    }
}
