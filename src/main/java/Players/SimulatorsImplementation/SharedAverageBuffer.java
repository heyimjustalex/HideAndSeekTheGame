package Players.SimulatorsImplementation;
import Players.Simulators.Buffer;
import Players.Simulators.Measurement;
import java.util.ArrayList;
import java.util.List;

public class SharedAverageBuffer implements Buffer {

    private final List<Measurement> averages = new ArrayList<>();
    @Override
    public void addMeasurement(Measurement m) { 
        averages.add(m);
    }

    @Override
    public List<Measurement> readAllAndClean() {  
            List<Measurement> copyOfList = new ArrayList<>(averages);
            // Clear all averages
            averages.clear();
            System.out.println("MODIFIED COLLECTION "+ averages);
            return copyOfList;
    }
}
