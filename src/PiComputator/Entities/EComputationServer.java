package PiComputator.Entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Common.Entities.SingletonLogger;
import Common.Enums.EColor;
import Common.Monitors.MPriorityQueue;

public class EComputationServer {

    public final int maxFifoSize = 3;
    public final int maxPiNumberOfIterations = 15;
    MPriorityQueue<StoredRequest> priorityQueue;
    private final ReentrantLock lock;
    private final SingletonLogger logger;

    public EComputationServer() {
        this.logger = SingletonLogger.getInstance();
        this.priorityQueue = new MPriorityQueue<>(this.maxFifoSize);
        this.lock = new ReentrantLock();
        for(int i=0;i<1;i++) {
            final int id = i;
            new Thread(() -> this.runComputationThread(id)).start();
        }
    }

    public Double computePI(Integer iterations, Integer deadline) {
        if(this.priorityQueue.isFull())
            return null;

        // add request to a priority queue based on the deadline
        Condition cond = this.lock.newCondition();
        StoredRequest request = new StoredRequest(cond, iterations);
        this.priorityQueue.put(request, deadline);

        // wait for the computation
        try {
            this.lock.lock();
            cond.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.lock.unlock();
        }
        return request.getPI();
    }

    
    public void runComputationThread(int id) {
        this.logger.log(String.format("Computation Thread %d: started", id), EColor.GREEN);
        while(true) {
            // get a new request
            StoredRequest request = this.priorityQueue.pop();
            this.logger.log(String.format("Computing PI on Thread %d", id));

            // wait for the iterations time
            try {
                Thread.sleep(request.getIterations() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // save the pi value on the request
            BigDecimal piValue = BigDecimal.valueOf(Math.PI);
            BigDecimal piValueRightDecimalPlaces = piValue.setScale(request.getIterations(), RoundingMode.HALF_UP);
            request.setPI(piValueRightDecimalPlaces.doubleValue());

            this.logger.log(String.format("Computation Thread %d - Computed: %s", id, piValueRightDecimalPlaces.toString()));
            
            // signal the condition for the requestPI to continue its work
            try {
                this.lock.lock();
                request.condition.signal();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                this.lock.unlock();
            }
            
            
        }
    }

    private class StoredRequest {

        private final Condition condition;
        private final Integer iterations;
        private Double PI;

        public StoredRequest(Condition condition, Integer iterations) {
            this.condition = condition;
            this.iterations = iterations;
        }

        public void setPI(Double PI) { this.PI = PI; }

        public Double getPI() { return this.PI; }
        public Integer getIterations() { return this.iterations; }

    }
    
}
