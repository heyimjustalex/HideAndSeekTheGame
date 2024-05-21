package Players.SimulatorsImplementation;

import Players.Simulators.Buffer;
import Players.Simulators.Measurement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedMeasurementBuffer implements Buffer {
    private final List<Measurement> measurements = new ArrayList<>();
    private static final int BUFFER_SIZE = 8;

    @Override
    public void addMeasurement(Measurement m) {
        synchronized (this){
            measurements.add(m);
            if (measurements.size() >= BUFFER_SIZE) {
                notifyAll(); // Notify waiting threads buffer is full
                    }
        }
    }
    @Override
    public List<Measurement> readAllAndClean() {
        synchronized (this){
            while (measurements.size() < BUFFER_SIZE) {
                try {
                    System.out.println("Waiting for the accurate size of: "+ BUFFER_SIZE);
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread interrupted while awaiting");
                }
            }


            List<Measurement> copyOfList = new ArrayList<>(measurements);
            // Overlap factor 50%, so we modify collection
            measurements.subList(0, BUFFER_SIZE / 2).clear();
            System.out.println("MODIFIED COLLECTION "+ measurements);
            return copyOfList;
        }

    }
}
