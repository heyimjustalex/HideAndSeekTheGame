package Game.HeartRate.SimulatorsImplementation;
import Game.HeartRate.Simulators.Buffer;
import Game.HeartRate.Simulators.Measurement;
import java.util.ArrayList;
import java.util.List;
public class SharedAverageBuffer implements Buffer {
    private final List<Measurement> averages = new ArrayList<>();
    @Override
    public synchronized void addMeasurement(Measurement m) {
//        System.out.println("SharedAverageBuffer: Adding averaged measurement to the collection");
        averages.add(m);
    }
    @Override
    public synchronized List<Measurement> readAllAndClean() {
        List<Measurement> copyOfList = new ArrayList<>(averages);
//        System.out.println("SharedAverageBuffer: Clearing collection: "+ averages);
        averages.clear();
//        System.out.println("SharedAverageBuffer: Collection cleared: "+ averages);
        return copyOfList;
    }
}
