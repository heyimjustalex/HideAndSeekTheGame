package Game;

import Game.Broker.SubscriberHandler;
import Game.GameClasses.PlayerExtended;
import Game.Global.GlobalState;
import Game.HeartRate.Simulators.Buffer;
import Game.HeartRate.Simulators.HRSimulator;
import Game.HeartRate.Simulators.Simulator;
import Game.HeartRate.SimulatorsImplementation.AverageComputer;
import Game.HeartRate.SimulatorsImplementation.AverageSender;
import Game.HeartRate.SimulatorsImplementation.SharedAverageBuffer;
import Game.HeartRate.SimulatorsImplementation.SharedMeasurementBuffer;
import Game.Services.GrpcCalls.GrpcCalls;
import Game.Services.PlayerServiceImpl;
import Game.Utilities.HTTPUtilities;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void greetPlayers() throws InterruptedException {
        for (PlayerExtended playerExtended : GlobalState.getStateObject().getPlayers()) {
            if (GlobalState.getStateObject().getMyPlayerId().equals(playerExtended.getId())) {
                continue;
            }
            String serverAddress = playerExtended.getAddress() + ":" + playerExtended.getPort();
            GrpcCalls.greetingCallAsync(serverAddress);
        }
    }

    public static Thread launchSubscriptionHandleThread() {
        try {

            Thread subscriptionThread = new Thread(() -> {
                try {
                    SubscriberHandler.handleSubscription();
                } catch (Exception e) {
                    System.out.println("LaunchSubscriptionHandleThread: " + e);
                }
            });

            return subscriptionThread;
        } catch (Exception e) {
            System.out.println("subscriptionThread: Exception: " + e);
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        String playerId = "";
        String port = "";
        String endpointUrlAddPlayers = "http://localhost:1337/players/add";
        boolean playerWasAdded = false;
        while (!playerWasAdded) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter playerId");
            playerId = scanner.nextLine();
            System.out.println("Enter port");
            port = scanner.nextLine();
            String address = "localhost";
            playerWasAdded = HTTPUtilities.httpPOSTPlayer(playerId, port, address, endpointUrlAddPlayers);

            // MUST HAVE FOR PROPER STATE WORKING AND SETTING PLAYERS
            GlobalState.getStateObject().setMyPlayerId(playerId);
        }
//        System.out.println(GlobalState.getStateObject().getPlayers());

//        System.out.println("Main Thread PID: " + Thread.currentThread().getId());

        Buffer measurementBuffer = new SharedMeasurementBuffer();
        Buffer averageBuffer = new SharedAverageBuffer();
        Simulator simulator = new HRSimulator(playerId, measurementBuffer);


        // Average Computing Thread
        Thread averageComputerThread = new Thread(new AverageComputer(measurementBuffer, averageBuffer, playerId));
        // Simulation average sender Thread
        Thread averageSenderThread = new Thread(new AverageSender(averageBuffer));

        averageSenderThread.start();
        averageComputerThread.start();
        simulator.start();

        Thread mqttHandlerThread = launchSubscriptionHandleThread();
        if (mqttHandlerThread != null) {
            mqttHandlerThread.start();

        }

        // GRPC
        io.grpc.Server server = ServerBuilder.forPort(Integer.parseInt(port)).addService(new PlayerServiceImpl()).build();
        server.start();
        System.out.println("Main: PlayerServiceImpl - Grpc server started at port: " + port);

        try {
            greetPlayers();
        } catch (Exception e) {
            System.out.println(e);
        }

        // wait() until gameState changed to any except for BEFORE_ELECTION
        GlobalState.getStateObject().waitUntilElectionStarts();
        System.out.println("Main: Election started");

        // Wait for threads to end

        averageComputerThread.join();
        simulator.join();
        server.awaitTermination();
        mqttHandlerThread.join();

    }
}
