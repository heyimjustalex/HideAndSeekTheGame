package Game;

import Game.Broker.SubscriberHandler;
import Game.GameClasses.GameState;
import Game.GameClasses.PlayerExtended;
import Game.GameClasses.PlayerState;
import Game.GameClasses.Role;
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
import java.util.List;
import java.util.Objects;
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

    public static void requestSharedResource() throws InterruptedException {
        List<PlayerExtended> playersISendResourceRequestsTo = GlobalState.getStateObject().getPlayers();
        GlobalState.getStateObject().setHowManyRequestResourceISent(playersISendResourceRequestsTo.size() - 2);
        String myPlayerId = GlobalState.getStateObject().getMyPlayerId();
        for (PlayerExtended playerExtended : playersISendResourceRequestsTo) {
            // You need to check it because somebody might join after you constructed collection
            if (!Objects.equals(playerExtended.getId(), myPlayerId) && playerExtended.getRole() != Role.SEEKER) {
                String serverAddress = playerExtended.getAddress() + ":" + playerExtended.getPort();
                GrpcCalls.requestResourceCallAsync(serverAddress);
            }

        }
    }


    public static void electLeader() throws InterruptedException {
        boolean atLeastOnceCalledElection = false;
        for (PlayerExtended playerExtended : GlobalState.getStateObject().getPlayers()) {

            boolean myPriorityIsHigherThanHis = (GlobalState.getStateObject().getMyDistance() < playerExtended.getDistance()) ||
                    (GlobalState.getStateObject().getMyDistance() == playerExtended.getDistance()
                            && GlobalState.getStateObject().getMyPlayerId().compareToIgnoreCase(playerExtended.getId()) > 0);

            if (GlobalState.getStateObject().getMyPlayerId().equals(playerExtended.getId()) || myPriorityIsHigherThanHis) {
                continue;
            }
            String serverAddress = playerExtended.getAddress() + ":" + playerExtended.getPort();
            atLeastOnceCalledElection = true;
            GrpcCalls.electionCallAsync(serverAddress);
        }
//         nobody is better than me and election messages have been sent by others so I have no chance to elect myself as a leader
        if (!atLeastOnceCalledElection) {
            GrpcCalls.electionSelfCall();
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
            GlobalState.getStateObject().calculateMyDistance();
            System.out.println("I'm Player " + playerId + " Distance to Base: " + GlobalState.getStateObject().getMyDistance());
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
        System.out.println("Main: I greeted and changed my state so I start election ");
        GameState myCurrentGameState = GlobalState.getStateObject().getGameState();

        if (myCurrentGameState.equals(GameState.ELECTION_STARTED) || myCurrentGameState.equals(GameState.ELECTION_MESSAGES_SENT)) {
            System.out.println("Main: sending election messages");
            try {
                electLeader();
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Main: election has ended, there is no need to take part I am a HIDER");
        }

        GlobalState.getStateObject().waitUntilElectionEnds();
        // Wait for threads to end
//        Thread.sleep(20000);
        System.out.println("Main: Election has ended, printing players information ");
        GlobalState.getStateObject().printPlayersInformation();
        if (!GlobalState.getStateObject().getMyPlayer().getRole().equals(Role.SEEKER)) {
            System.out.println("Main: I'm not SEEKER, I'm requesting resource");

            try {
                GlobalState.getStateObject().setMyPlayerState(PlayerState.WAITING_FOR_LOCK);
                requestSharedResource();
            } catch (Exception e) {
                System.out.println(e);
            }

            GlobalState.getStateObject().tryGoingToBase();
        } else {
            System.out.println("Main: I'm SEEKER, I'm trying to tag HIDERS ");
            GlobalState.getStateObject().tryCatchingHiders();
        }

        averageComputerThread.join();
        simulator.join();
        server.awaitTermination();
        if (mqttHandlerThread != null) {
            mqttHandlerThread.join();

        }

    }
}
