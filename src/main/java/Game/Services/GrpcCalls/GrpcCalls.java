package Game.Services.GrpcCalls;

import Game.GameClasses.GameState;
import Game.GameClasses.MessageType;
import Game.GameClasses.PlayerExtended;
import Game.GameClasses.Role;
import Game.Global.GlobalState;
import Game.Utilities.Other;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.Player.PlayerMessageRequest;
import proto.Player.PlayerMessageResponse;
import proto.PlayerServiceGrpc;

import java.util.concurrent.*;

public class GrpcCalls {
    private static PlayerMessageRequest createCoordinatorRequest() {

        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();

        return PlayerMessageRequest
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

    }

    private static PlayerMessageRequest createElectionRequest() {
        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();
        GameState gameState = GlobalState.getStateObject().getGameState();
        System.out.println("GRPCalls, electionCallAsync: Player " + myPlayer.getId() + ", gameState " + gameState);
        return PlayerMessageRequest
                .newBuilder()
                .setId(myPlayer.getId())
                .setPort(myPlayer.getPort().toString())
                .setAddress(myPlayer.getAddress())
                .setPosX(myPlayer.getPos_x().toString())
                .setPosY(myPlayer.getPos_y().toString())
                .setRole(myPlayer.getRole().name())
                .setPlayerState(myPlayer.getPlayerState().name())
                .setGameState(gameState.toString())
                .setMessageType(String.valueOf(MessageType.ELECTION))
                .build();
    }

    private static PlayerMessageRequest createGreetingRequest() {
        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();

        return PlayerMessageRequest
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
    }

    public static void coordinatorCallAsync(String serverAddress) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

        // COORDINATOR message type sent when SEEKER is chosen
        PlayerMessageRequest request = createCoordinatorRequest();
        stub.election(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                System.out.println("GRPCalls, coordinatorCallAsync: I got COORDINATOR message from " + response.getId());
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

    public static void electionCallAsync(String serverAddress) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);
        String myId = GlobalState.getStateObject().getMyPlayerId();
        final ScheduledFuture<?>[] timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();
        final ScheduledFuture<?>[] timeoutFutureHolderGreetingElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();
        PlayerMessageRequest request = createElectionRequest();
        stub.election(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                // If you get ELECTION_OK messageType it means that somebody has lower distance and
                // You can cancel becoming SEEKER and set your role as hider
                if (MessageType.valueOf(response.getMessageType()) == MessageType.ELECTION_OK) {
                    System.out.println("GRPCalls, electionCallAsync: Player " + myId + " State set to HIDER because i got OK message from player " + response.getId());
                    if (timeoutFutureHolderElection[0] != null) {
                        timeoutFutureHolderElection[0].cancel(true);
                    }
                    if (timeoutFutureHolderGreetingElection[0] != null) {
                        timeoutFutureHolderGreetingElection[0].cancel(true);
                    }
                    GlobalState.getStateObject().setMyPlayerRole(Role.HIDER);
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

    public static void greetingCallAsync(String serverAddress) {

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

        String myId = GlobalState.getStateObject().getMyPlayerId();
        final ConcurrentHashMap<String, Boolean> greetingElectionFutureProcessed = GlobalState.getStateObject().getGreetingElectionFutureProcessed();
        final ScheduledFuture<?>[] timeoutFutureHolderGreetingElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        PlayerMessageRequest request = createGreetingRequest();

        stub.election(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {

                if (MessageType.valueOf(response.getMessageType()) == MessageType.GREETING_OK) {
                    // Response to the GREETING_OK message
                    System.out.println("GRPCalls, greetingCallAsync: Player " + myId + ": Got a GREETING_OK message from " + response.getId());
                    GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
                    GameState requestGameState = GameState.valueOf(response.getGameState());

                    // If my current state is lower than the player's I greeted (MQTT message latency)
                    if (requestGameState.ordinal() > myCurrentGameState.ordinal()) {
                        System.out.println("GRPCalls, greetingCallAsync: Player " + myId + ": GREETING_OK message from: " + response.getId() + " changed my state to " + requestGameState);
                        GlobalState.getStateObject().setGameState(requestGameState);
                    }

                    // If the election messages have already been sent by the Player I greeted
                    if (requestGameState.equals(GameState.ELECTION_MESSAGES_SENT)) {
                        // This means that election messages have been sent, so you need to also send them to take part in election

                        double myDistance = GlobalState.getStateObject().getMyDistance();
                        double otherPlayerDistance = Other.calculateDistanceToNearestBasePoint(Double.parseDouble(response.getPosX()), Double.parseDouble(response.getPosY()));
                        System.out.println("GRPCalls, greetingCallAsync: Player " + myId + ": GREETING_OK election message, if I don't get OK messages in 3s I will change to SEEKER");
                        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();

                        if (myDistance < otherPlayerDistance || (myDistance == otherPlayerDistance && myId.compareToIgnoreCase(response.getId()) > 0)) {
                            System.out.println("GRPCalls, greetingCallAsync: Player " + myId + ": I got ELECTION message with player distance lower than mine (" + myDistance + ") from " + response.getId() + " (" + otherPlayerDistance + ") " + " so I send him OK");
                            try {
                                electionCallAsync(serverAddress);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        // Create election won - this player becomes the SEEKER
                        Runnable electionWonTask = () -> {
                            GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);
                            GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
                            try {
                                coordinatorCallAsync(serverAddress);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            System.out.println("GRPCalls, greetingCallAsync: Player " + myId + ": timeout occurred. State set to SEEKER and sending COORDINATOR message ");

                        };

                        if (greetingElectionFutureProcessed.putIfAbsent("ELECTION", true) == null) {
                            System.out.println("GRPCalls, greetingCallAsync: Player " + myId + ": ELECTION !FROM GREETING! message put to map, and I will become SEEKER in 3 s");
                            timeoutFutureHolderGreetingElection[0] = executor.schedule(electionWonTask, 12, TimeUnit.SECONDS);

                        }
                    }
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
