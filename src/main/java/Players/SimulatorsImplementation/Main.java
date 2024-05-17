package Players.SimulatorsImplementation;

import Players.Simulators.Buffer;
import Players.Simulators.HRSimulator;
import Players.Simulators.Simulator;
import Players.SimulatorsImplementation.MyBuffer;

public class Main {
    public static void main(String[] args){
        Buffer buffer = new MyBuffer();
        Simulator simulator = new HRSimulator(buffer);
        simulator.run();

    }
}
