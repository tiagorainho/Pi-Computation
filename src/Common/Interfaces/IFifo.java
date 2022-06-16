
package Common.Interfaces;

public interface IFifo<E> extends IConsumer<E> {


    public boolean isEmpty();

    public boolean isFull();
}
