package Players.SimulatorsImplementation;

import Players.Simulators.Buffer;
import Players.Simulators.Measurement;

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
            Measurement computedAverageMeasurement = new Measurement(playerId,"avg",average,System.currentTimeMillis());
            this.averagesBuffer.addMeasurement(computedAverageMeasurement);
            System.out.println("CURR TIME " +System.currentTimeMillis());
            System.out.println("CALCULATED AVG "+average);



        }

    }
}
