package Game.Services.GrpcCalls;

import Game.GameClasses.GameState;
import Game.GameClasses.MessageType;
import Game.GameClasses.PlayerExtended;
import Game.GameClasses.Role;
import Game.Global.GlobalState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.Player.PlayerMessageRequest;
import proto.Player.PlayerMessageResponse;
import proto.PlayerServiceGrpc;

import java.util.concurrent.*;

public class GrpcCalls {
    public static Double calculateDistanceToNearestBasePoint(double pos_x, double pos_y) {

        double xb = pos_x;
        double yb = pos_y;

        double[][] baseCoords = {
                {4.0, 4.0},
                {4.0, 5.0},
                {5.0, 5.0},
                {5.0, 4.0}
        };
        double xa = baseCoords[0][0];
        double ya = baseCoords[0][1];

        double calculated_distance = Math.sqrt((Math.pow(xa - xb, 2) + Math.pow(ya - yb, 2)));

        for (int i = 1; i < baseCoords.length; i++) {
            xa = baseCoords[i][0];
            ya = baseCoords[i][1];
            double new_distance = Math.sqrt((Math.pow(xa - xb, 2) + Math.pow(ya - yb, 2)));
            calculated_distance = Math.min(calculated_distance, new_distance);
        }

        return calculated_distance;
    }

    public static void coordinatorCallAsync(String serverAddress) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);
        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();
        String myId = GlobalState.getStateObject().getMyPlayerId();
        final ConcurrentHashMap<String, Boolean> greetingElectionFutureProcessed = GlobalState.getStateObject().getGreetingElectionFutureProcessed();
        final ConcurrentHashMap<String, Boolean> electionFutureProcessed = GlobalState.getStateObject().getElectionFutureProcessed();
        final ScheduledFuture<?>[] timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();
        final ScheduledFuture<?>[] timeoutFutureHolderGreetingElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();

        PlayerMessageRequest request = PlayerMessageRequest
                .newBuilder()
                .setId(myPlayer.getId())
                .setPort(myPlayer.getPort().toString())
                .setAddress(myPlayer.getAddress())
                .setPosX(myPlayer.getPos_x().toString())
                .setPosY(myPlayer.getPos_y().toString())
                .setRole(myPlayer.getRole().name())

                .setPlayerState(myPlayer.getPlayerState().name())
                .setGameState(GameState.ELECTION_ENDED.toString())
                .setMessageType(String.valueOf(MessageType.COORDINATOR))
                .build();

        stub.election(request, new StreamObserver<PlayerMessageResponse>() {

            @Override
            public void onNext(PlayerMessageResponse response) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }


        });
    }

    public static void greetingCallAsync(String serverAddress) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);
        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();
        String myId = GlobalState.getStateObject().getMyPlayerId();
        final ConcurrentHashMap<String, Boolean> greetingElectionFutureProcessed = GlobalState.getStateObject().getGreetingElectionFutureProcessed();
        final ScheduledFuture<?>[] timeoutFutureHolderGreetingElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();

        PlayerMessageRequest request = PlayerMessageRequest
                .newBuilder()
                .setId(myPlayer.getId())
                .setPort(myPlayer.getPort().toString())
                .setAddress(myPlayer.getAddress())
                .setPosX(myPlayer.getPos_x().toString())
                .setPosY(myPlayer.getPos_y().toString())
                .setRole(myPlayer.getRole().name())
                .setPlayerState(myPlayer.getPlayerState().name())
                .setGameState(GlobalState.getStateObject().getGameState().name())
                .setMessageType(MessageType.GREETING.toString())
                .build();
        stub.election(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                switch (MessageType.valueOf(response.getMessageType())) {
                    case GREETING_OK:
                        // Response to the GREETING message
                        System.out.println("Player " + myId + ": Got a GREETING_OK message from " + response.getId());
                        GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
                        GameState requestGameState = GameState.valueOf(response.getGameState());

                        // If my current state is lower than the player's I greeted (MQTT message latency)
                        if (requestGameState.ordinal() > myCurrentGameState.ordinal()) {
                            System.out.println("Player " + myId + ": GREETING_OK message from: " + response.getId() + " changed my state to " + requestGameState);
                            GlobalState.getStateObject().setGameState(requestGameState);
                        }


                        // If the election messages have already been sent by the Player I greeted
                        if (requestGameState.equals(GameState.ELECTION_MESSAGES_SENT)) {
                            // This means that election messages have been sent, so you need to also send them to take part in election

                            double myDistance = GlobalState.getStateObject().getMyDistance();
                            double otherPlayerDistance = calculateDistanceToNearestBasePoint(Double.parseDouble(request.getPosX()), Double.parseDouble(request.getPosY()));
                            System.out.println("Player " + myId + ": GREETING_OK election message, if I don't get OK messages in 3s I will change to leader");
                            PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();

                            if (myDistance < otherPlayerDistance || (myDistance == otherPlayerDistance && myId.compareToIgnoreCase(request.getId()) > 0)) {
                                System.out.println("Player " + myId + ": I got ELECTION message with player distance lower than mine from " + request.getId() + " so I send him OK");
                                onNext(PlayerMessageResponse.newBuilder()
                                        .setMessageType(MessageType.ELECTION.toString())
                                        .setPosX(myPlayer.getPos_x().toString())
                                        .setPosY(myPlayer.getPos_y().toString())
                                        .setId(myId)
                                        .build());
                            }

                            if (greetingElectionFutureProcessed.putIfAbsent("ELECTION", true) == null) {
                                System.out.println("Player " + myId + ": ELECTION !FROM GREETING! message put to map, and I will become leader in 3 s");
                                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                                timeoutFutureHolderGreetingElection[0] = executor.schedule(() -> {
                                    GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);
                                    GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
                                    try {
                                        coordinatorCallAsync(serverAddress);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    System.out.println("Player " + myId + ": timeout occurred. State set to LEADER and sending COORDINATOR message ");
                                }, 3, TimeUnit.SECONDS);
                            }
                        }

                        break;
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
            }
        });


    }
}
