package Players.Smartwatch.SimulatorsImplementation;
import Players.Smartwatch.Simulators.Buffer;
import Players.Smartwatch.Simulators.Measurement;
import java.util.ArrayList;
import java.util.List;
public class SharedMeasurementBuffer implements Buffer {
    private final List<Measurement> measurements = new ArrayList<>();
    private static final int BUFFER_SIZE = 8;
    @Override
    public synchronized void addMeasurement(Measurement m) {
        measurements.add(m);
        if (measurements.size() >= BUFFER_SIZE)
        {
            notifyAll(); // Notify waiting threads buffer is full
        }
    }
    @Override
    public synchronized List<Measurement> readAllAndClean() {
        while (measurements.size() < BUFFER_SIZE) {
            try {
                System.out.println("SharedMeasurementBuffer: Waiting for the accurate size of the collection: "+ BUFFER_SIZE);
                // Wait releases the lock, so that's why addMeasurement can be invoked
                wait();
            }
            catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("SharedMeasurementBuffer: Thread interrupted while awaiting");
            }
        }

        List<Measurement> copyOfList = new ArrayList<>(measurements);
        // Overlap factor 50%, so we modify collection
        measurements.subList(0, BUFFER_SIZE / 2).clear();
        System.out.println("SharedMeasurementBuffer: Modified original collection: "+ measurements);
        System.out.println("SharedMeasurementBuffer: Returning the copy "+ copyOfList);
        return copyOfList;

    }
}
