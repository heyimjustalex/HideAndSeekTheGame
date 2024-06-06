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

import java.util.Objects;
import java.util.concurrent.*;

public class GrpcCalls {
    static boolean coordinatorHasBeenCalled = false;

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

    public static void greetingCallAsync(String serverAddress) {

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);
        String myId = GlobalState.getStateObject().getMyPlayerId();

        PlayerMessageRequest request = createGreetingRequest();
        System.out.println("GRPCalls, greetingCallAsync: Player: " + request.getId() + " current GameState: " + request.getGameState());
        stub.greeting(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {

                if (MessageType.valueOf(response.getMessageType()) == MessageType.GREETING_OK) {
                    // Response to the GREETING_OK message
                    System.out.println("GRPCalls, greetingCallAsync: Player: " + myId + ": GREETING_OK message from Player: " + response.getId());
                    GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
                    GameState responseGameState = GameState.valueOf(response.getGameState());

                    // If my current state is lower than the player's I greeted (MQTT message latency)
                    if (responseGameState.ordinal() > myCurrentGameState.ordinal()) {
                        System.out.println("GRPCalls, greetingCallAsync: Player: " + myId + ": GREETING_OK message from Player: " + response.getId() + " changed my state to higher -> " + responseGameState);
                        GlobalState.getStateObject().setGameState(responseGameState);
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

    public static void electionCallAsync(String serverAddress) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);
        String myId = GlobalState.getStateObject().getMyPlayerId();
        final ScheduledFuture<?>[] timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();
        final ScheduledFuture<?>[] timeoutFutureHolderGreetingElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();
        final ConcurrentHashMap<String, Boolean> electionFutureProcessed = GlobalState.getStateObject().getElectionFutureProcessed();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        PlayerMessageRequest request = createElectionRequest();

        //This is needed for edge case when the person with lowest distance joins after election ended
        if (GlobalState.getStateObject().getGameState().ordinal() < GameState.ELECTION_MESSAGES_SENT.ordinal()) {
            GlobalState.getStateObject().setGameState(GameState.ELECTION_MESSAGES_SENT);

            Runnable electionWonTask = () -> {
                GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);

                try {
                    coordinatorCallAsync(serverAddress);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("GRPCalls, electionCallAsync: Player " + myId + ": timeout occurred. State set to SEEKER and sending COORDINATOR message ");

            };
            if (electionFutureProcessed.putIfAbsent("ELECTION", true) == null) {
                System.out.println("GRPCalls, electionCallAsync: Player " + myId + ": ELECTION message put to map, and I will become SEEKER in 12 s");
                timeoutFutureHolderGreetingElection[0] = executor.schedule(electionWonTask, 12, TimeUnit.SECONDS);
            }
        }

        stub.election(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                // If you get ELECTION_OK messageType it means that somebody has lower distance, and
                // You can cancel becoming SEEKER and set your role as hider
                if (MessageType.valueOf(response.getMessageType()) == MessageType.ELECTION_OK) {

                    if (timeoutFutureHolderElection[0] != null) {
                        System.out.println("GRPCalls, electionCallAsync: Player: " + myId + " I CANCEL LEADER ELECTION, Role set to HIDER because i got OK message from Player: " + response.getId());
                        timeoutFutureHolderElection[0].cancel(true);
                    }

                    GlobalState.getStateObject().setMyPlayerRole(Role.HIDER);
//                    GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
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

    public static void electionSelfCall() throws InterruptedException {

        String myId = GlobalState.getStateObject().getMyPlayerId();
        final ScheduledFuture<?>[] timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();

        final ConcurrentHashMap<String, Boolean> electionFutureProcessed = GlobalState.getStateObject().getElectionFutureProcessed();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

//        GlobalState.getStateObject().setGameState(GameState.ELECTION_MESSAGES_SENT);

        Runnable electionWonTask = () -> {
            GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);

            try {
                for (PlayerExtended player : GlobalState.getStateObject().getPlayers()) {
                    if (!Objects.equals(player.getId(), myId)) {
                        coordinatorCallAsync(player.getAddress() + ":" + player.getPort());
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Couldnt send coordinator messages! " + e);
            }
        };

        if (electionFutureProcessed.putIfAbsent("ELECTION", true) == null) {
            System.out.println("GRPCalls, electionCallAsync: Player " + myId + ": ELECTION message put to map, and I will become SEEKER in 12 s");
            timeoutFutureHolderElection[0] = executor.schedule(electionWonTask, 12, TimeUnit.SECONDS);
        }


    }

    public static void coordinatorCallAsync(String serverAddress) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);
        GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);

        // COORDINATOR message type sent when SEEKER is chosen
        PlayerMessageRequest request = createCoordinatorRequest();


        stub.coordinator(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                System.out.println("GRPCalls, coordinatorCallAsync: Player: " + request.getId() + " Sent to Player: " + response.getId());
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

        if (!coordinatorHasBeenCalled) {

            GlobalState.getStateObject().printPlayersInformation();
            coordinatorHasBeenCalled = true;
        }
    }
}
