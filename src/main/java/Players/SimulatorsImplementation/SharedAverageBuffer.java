package Players.SimulatorsImplementation;
import Players.Simulators.Buffer;
import Players.Simulators.Measurement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedAverageBuffer implements Buffer {
    private final List<Measurement> averages = new ArrayList<>();

    @Override
    public void addMeasurement(Measurement m) {
        synchronized (this){
                averages.add(m);
        }
    }

    @Override
    public List<Measurement> readAllAndClean() {

    synchronized (this){
        List<Measurement> copyOfList = new ArrayList<>(averages);
        System.out.println("MODIFIED COLLECTION "+ averages);
        averages.clear();

        return copyOfList;
    }




    }
}
