package nl.whizzkit.oracdc;

public class ShutdownThread extends Thread {

    private IShutdownThread shutdownThread;

    ShutdownThread(IShutdownThread shutdownThread) {
        this.shutdownThread = shutdownThread;
    }

    @Override
    public void run() {
        this.shutdownThread.shutdown();
    }
}