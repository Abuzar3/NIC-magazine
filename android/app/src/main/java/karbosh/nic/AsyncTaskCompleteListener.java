package karbosh.nic;

/**
 * Created by abuzar on 2/15/2015.
 */
public interface AsyncTaskCompleteListener<T> {
    public void onTaskComplete(T result);
}
