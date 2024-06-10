package Game.ConcurrentCollections;

import java.util.Timer;
import java.util.TimerTask;

public class CustomScheduledFuture {
    private final Timer timer;
    private TimerTask timerTask;
    private volatile boolean isCancelled;
    private volatile Thread runningThread;

    public CustomScheduledFuture() {
        this.timer = new Timer();
        this.isCancelled = false;
    }

    public synchronized void schedule(Runnable task, long delayMillis) {

        if (isCancelled) {
            throw new IllegalStateException("Task has been cancelled");
        }

        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (CustomScheduledFuture.this) {
                    if (!isCancelled) {
                        runningThread = new Thread(task);
                        runningThread.start();
                    }
                }
            }
        };

        timer.schedule(timerTask, delayMillis);
    }

    public synchronized void cancel(boolean mayInterruptIfRunning) {
        if (timerTask != null && !isCancelled) {
            isCancelled = true;
            timerTask.cancel();
            timer.purge();

            if (mayInterruptIfRunning && runningThread != null) {
                runningThread.interrupt();
            }

        }
    }
}