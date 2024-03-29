package Common.Interfaces;

public interface IServiceIterator<E> {

    public boolean hasNext();

    public E next(int weight);
    
}
