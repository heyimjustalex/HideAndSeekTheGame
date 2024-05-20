package Players;

import Players.Simulators.Buffer;
import Players.Simulators.HRSimulator;
import Players.Simulators.Simulator;
import Players.SimulatorsImplementation.AverageComputer;
import Players.SimulatorsImplementation.AverageSender;
import Players.SimulatorsImplementation.SharedAverageBuffer;
import Players.SimulatorsImplementation.SharedMeasurementBuffer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter playerId");
        String playerId = scanner.nextLine();

        Buffer measurementBuffer = new SharedMeasurementBuffer();
        Buffer averageBuffer = new SharedAverageBuffer();
        Simulator simulator = new HRSimulator(playerId,measurementBuffer);

        Thread averageComputerThread = new Thread(new AverageComputer(measurementBuffer,averageBuffer,playerId));
        Thread averageSenderThread = new Thread(new AverageSender(measurementBuffer,playerId));

        averageSenderThread.start();

        averageComputerThread.start();
        simulator.start();

        averageComputerThread.join();
        simulator.join();


    }
}
