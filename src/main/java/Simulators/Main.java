package Simulators;

public class Main {
    public static void main(String[] args){
        Buffer buffer = new MyBuffer();
        Simulator simulator = new HRSimulator(buffer);
        simulator.run();

    }
}
