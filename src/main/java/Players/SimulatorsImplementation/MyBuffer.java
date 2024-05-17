package Players.SimulatorsImplementation;

import Players.Simulators.Buffer;
import Players.Simulators.Measurement;

import java.util.ArrayList;
import java.util.List;

public class MyBuffer implements Buffer {

    List<Measurement> measurements = new ArrayList<>();
    @Override
    public void addMeasurement(Measurement m) {
        measurements.add(m);
    }

    @Override
    public List<Measurement> readAllAndClean() {
        List<Measurement> copyOfList = new ArrayList<>(this.measurements);
        measurements.clear();
        return copyOfList;
    }
}
