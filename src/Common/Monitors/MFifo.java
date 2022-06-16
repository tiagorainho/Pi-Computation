package Common.Monitors;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Common.Entities.EPriorityQueue;

public class MFifo<E> extends EPriorityQueue<E> {
    
    protected final ReentrantLock rl;
    private final Condition cNotFull;
    private final Condition cNotEmpty;
    private boolean blockPuts;

    public MFifo(int size) {
        super(size, true);
        this.rl = new ReentrantLock();
        this.cNotEmpty = rl.newCondition();
        this.cNotFull = rl.newCondition();
        this.blockPuts = false;
    }

    public MFifo() {
        super(Integer.MAX_VALUE, true);
        this.rl = new ReentrantLock();
        this.cNotEmpty = rl.newCondition();
        this.cNotFull = rl.newCondition();
        this.blockPuts = false;
    }

    public boolean isEmpty() {
        return super.isEmpty();
    }

    public boolean isFull() {
        return super.isFull();
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

    public void put(E obj) {
        try {
            rl.lock();
            while ( isFull() )
                cNotFull.await();

            super.put(obj, 0);
            cNotEmpty.signal();
        } catch ( InterruptedException ex ) {}
        finally {
            rl.unlock();
        }
    }

    public boolean isBlocked() {
        return this.blockPuts;
    }

    public void blockPuts() {
        this.blockPuts = true;
    }

    public void unblockPuts() {
        this.blockPuts = false;
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

