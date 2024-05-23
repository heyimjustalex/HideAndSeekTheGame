package Players.Smartwatch.SimulatorsImplementation;

import Players.Smartwatch.Simulators.Buffer;
import Players.Smartwatch.Simulators.Measurement;

import java.util.List;
import java.util.OptionalDouble;

public class AverageComputer implements Runnable{
    private Buffer measureamentsBuffer;
    private Buffer averagesBuffer;
    private String playerId;
    public AverageComputer(Buffer measureamentBuffer, Buffer averagesBuffer , String playerId){
        this.measureamentsBuffer = measureamentBuffer;
        this.averagesBuffer = averagesBuffer;
        this.playerId = playerId;
    }
    @Override
    public void run() {
        while (true){
            List<Measurement> measurements = measureamentsBuffer.readAllAndClean();
            OptionalDouble averageOrNone = measurements.stream().mapToDouble(Measurement::getValue).average();
            Double average = averageOrNone.isPresent() ? averageOrNone.getAsDouble() : 0;
            long timestamp = System.currentTimeMillis();
            Measurement computedAverageMeasurement = new Measurement(playerId,"HR",average,timestamp);
            this.averagesBuffer.addMeasurement(computedAverageMeasurement);
            System.out.println("AverageComputer: Timestamp of computation " +timestamp);
            System.out.println("AverageComputer: Computed average "+average);
        }
    }
}
