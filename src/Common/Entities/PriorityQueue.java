package Common.Entities;

import java.util.ArrayList;
import java.util.List;

public class PriorityQueue<E> {

    protected List<Node<E>> queue;
    protected final int maxSize;
    protected final boolean descendentOrder;

    public PriorityQueue(int size, boolean descendent) {
        queue = new ArrayList<>();
        this.maxSize = size;
        this.descendentOrder = descendent;
    }

    public PriorityQueue(int size) {
        this(size, true);
    }

    public PriorityQueue(boolean descendent) {
        this(Integer.MAX_VALUE, descendent);
    }

    public PriorityQueue() {
        queue = new ArrayList<>();
        this.maxSize = Integer.MAX_VALUE;
        this.descendentOrder = true;
    }

    public E getHead() {
        if(this.queue.size() == 0)
            return null;
        int idx = (this.descendentOrder)? this.queue.size()-1: 0;
        return this.queue.get(idx).element;
    }

    public E getTail() {
        if(this.queue.size() == 0)
            return null;
        int idx = (this.descendentOrder)? 0: this.queue.size()-1;
        return this.queue.get(idx).element;
    }

    public List<E> getPriorityQueue() {
        List<E> elements = new ArrayList<>(this.queue.size());
        if(this.descendentOrder)
            for(int i=this.queue.size()-1; i>=0; i--)
                elements.add(this.queue.get(i).element);
        else
            for(Node<E> n: this.queue)
                elements.add(n.element);
        return elements;
    }

    public E pop() {
        int idx = (this.descendentOrder)? this.queue.size()-1: 0;
        E value = this.queue.remove(idx).element;
        return value;
    }

    public E remove(int idx) {
        E value = this.queue.remove(this.queue.size()-idx-1).element;
        return value;
    }

    private void addToQueue(E obj, double priority) {
        for(int i=0;i<this.queue.size();i++) {
            if(this.queue.get(i).priority <= priority) {
                this.queue.add(i, new Node<E>(obj, priority));
                return;
            }
        }

        // add to end of the queue
        this.queue.add(this.queue.size(), new Node<E>(obj, priority));
    }

    public void put(E obj, double priority) {
        if(this.queue.size() == 0)
            this.queue.add(new Node<E>(obj, priority));
        else
            this.addToQueue(obj, priority);
    }

    protected boolean isEmpty() {
        return this.queue.size() == 0;
    }

    protected boolean isFull() {
        return this.queue.size() >= this.maxSize;
    }

    private class Node<T> {

        private double priority;
        private T element;

        public Node(T element, double priority) {
            this.priority = priority;
            this.element = element;
        }

    }

    @Override
    public String toString() {
        String pq = "";
        if(this.descendentOrder)
            for(int i=this.queue.size()-1; i>=0; i--)
                pq += "\n" +  this.queue.get(i).priority + ":" + this.queue.get(i).element + ", ";
        else
            for(Node<E> n: this.queue)
                pq += n.priority + ": " + n.element;
        return "[Priority Queue -> {" + pq + "}\n]";
    }
    
}


