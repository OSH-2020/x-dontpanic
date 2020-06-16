package client;

public class SynItem {

    private int status;

    public SynItem(int s) {
        status = s;
    }

    public synchronized int getStatus() {
        return status;
    }

    public synchronized void setStatus(int s) {
        status = s;
        notifyAll();
    }

    public synchronized int waitChange(int oldValue) {
        while (status == oldValue) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("interrupted");
                return status;
            }
        }
        return status;
    }

}
