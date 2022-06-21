package Common.Monitors;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Common.Entities.PriorityQueue;

public class MPriorityQueue<E> extends PriorityQueue<E> {
    
    protected final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;

    public MPriorityQueue(int size, boolean descendent) {
        super(size, descendent);
        this.rl = new ReentrantLock();
        this.cNotEmpty = rl.newCondition();
        this.cNotFull = rl.newCondition();
    }

    public MPriorityQueue(int size) {
        this(size, true);
    }

    public MPriorityQueue() {
        this(Integer.MAX_VALUE, true);
    }

    public boolean isEmpty() {
        return super.isEmpty();
    }

    public boolean isFull() {
        return super.isFull();
    }

    public E getHead() {
        try{
            rl.lock();
            return super.getHead();
        }
        finally {
            rl.unlock();
        }
    }

    public E getTail() {
        try{
            rl.lock();
            return super.getTail();
        }
        finally {
            rl.unlock();
        }
    }

    public List<E> getPriorityQueue() {
        try{
            rl.lock();
            return super.getPriorityQueue();
        }
        finally {
            rl.unlock();
        }
    }

    public E pop() {
        try{
            rl.lock();
            try {
                while (isEmpty())
                    cNotEmpty.await();
            } catch( InterruptedException ex ) {}

            E value = super.pop();
            cNotFull.signal();
            return value;
        }
        finally {
            rl.unlock();
        }
    }

    public E remove(int idx){
        try{
            rl.lock();
            E value = super.remove(idx);
            return value;
        }
        finally {
            rl.unlock();
        }
    }

    public void put(E obj, double priority) {
        try {
            rl.lock();
            while ( isFull() )
                cNotFull.await();

            super.put(obj, priority);
            cNotEmpty.signal();
        } catch ( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
    }

    @Override
    public String toString() {
        String res = "";
        try {
            rl.lock();
            res = super.toString();
        } catch (Exception ex) {}
        finally {
            rl.unlock();
        }
        return res;
    }
    
}
