package Game.Services.GrpcCalls;

import Game.ConcurrentCollections.CustomConcurrentHashMap;
import Game.ConcurrentCollections.CustomScheduledFuture;
import Game.GameClasses.*;
import Game.Global.GlobalState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.Player.PlayerMessageRequest;
import proto.Player.PlayerMessageResponse;
import proto.PlayerServiceGrpc;

import java.util.List;
import java.util.Objects;

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
        CustomScheduledFuture timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();
        PlayerMessageRequest request = createGreetingRequest();
        System.out.println("GRPCalls, greetingCallAsync: Player: " + request.getId() + " current GameState: " + request.getGameState());
        final CustomConcurrentHashMap<String, Boolean> electionFutureProcessed = GlobalState.getStateObject().getElectionFutureProcessed();
        stub.greeting(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {

                GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
                GameState responseGameState = GameState.valueOf(response.getGameState());

                if (MessageType.valueOf(response.getMessageType()) == MessageType.GREETING_OK) {
                    // Response to the GREETING_OK message
                    System.out.println("GRPCalls, greetingCallAsync: Player: " + myId + ": GREETING_OK message from Player: " + response.getId());

                    // Edge case for setting SEEKER after ELECTION ENDED
                    if (responseGameState.equals(GameState.ELECTION_ENDED) && response.getRole().equals(Role.SEEKER.toString())) {
                        System.out.println("GRPCalls, greetingCallAsync: Player: " + myId + "I set this Player " + "to SEEKER, bcs ELECTION_ENDED");
                        GlobalState.getStateObject().setChosenPlayerToSeeker(response.getId());
                    }
                    // If my current state is lower than the player's I greeted (MQTT message latency)
                    if (responseGameState.ordinal() > myCurrentGameState.ordinal()) {
                        System.out.println("GRPCalls, greetingCallAsync: Player: " + myId + ": GREETING_OK message from Player: " + response.getId() + " changed my state to higher -> " + responseGameState);
                        GlobalState.getStateObject().setGameState(responseGameState);
                    }
                    // Cancel in case other party has election going on and his priority is higher
                    if (electionFutureProcessed.get("ELECTION") != null) {
                        if (timeoutFutureHolderElection != null) {
                            System.out.println("GRPCalls, greetingCallAsync: Player: " + myId + " I CANCEL LEADER ELECTION, Role set to HIDER because i got OK message from Player: " + response.getId());
                            timeoutFutureHolderElection.cancel(true);
                        }
                    }
                } else {
                    // Late join for ELECTION edge case

                    if (responseGameState.ordinal() > myCurrentGameState.ordinal()) {
                        System.out.println("GRPCalls, greetingCallAsync: Player: " + myId + ": ACK message from Player: " + response.getId() + " changed my state to higher -> " + "ELECTION_STARTED");
                        GlobalState.getStateObject().setGameState(GameState.ELECTION_STARTED);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("ERROR" + t.getMessage());
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
        CustomScheduledFuture timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();

        final CustomConcurrentHashMap<String, Boolean> electionFutureProcessed = GlobalState.getStateObject().getElectionFutureProcessed();
        PlayerMessageRequest request = createElectionRequest();

        stub.election(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                // If you get ELECTION_OK messageType it means that somebody has lower distance, and
                // You can cancel becoming SEEKER and set your role as hider
                if (MessageType.valueOf(response.getMessageType()) == MessageType.ELECTION_OK) {

                    if (timeoutFutureHolderElection != null) {
                        System.out.println("GRPCalls, electionCallAsync: Player: " + myId + " I CANCEL LEADER ELECTION, Role set to HIDER because i got OK message from Player: " + response.getId());
                        timeoutFutureHolderElection.cancel(true);
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

    public static void electionSelfCall() throws InterruptedException {

        String myId = GlobalState.getStateObject().getMyPlayerId();
        CustomScheduledFuture timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();

        final CustomConcurrentHashMap<String, Boolean> electionFutureProcessed = GlobalState.getStateObject().getElectionFutureProcessed();


        Runnable electionWonTask = () -> {
            GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);
            // Edge case for 1 player in the game who performs election
            GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);

            try {
                for (PlayerExtended player : GlobalState.getStateObject().getPlayers()) {
                    if (!Objects.equals(player.getId(), myId)) {
                        coordinatorCallAsync(player.getAddress() + ":" + player.getPort());
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Couldn't send coordinator messages! " + e);
            }
        };


        //This is needed for edge case when the person with lowest distance joins after election ended
        if (GlobalState.getStateObject().getGameState().ordinal() < GameState.ELECTION_MESSAGES_SENT.ordinal()) {
            if (electionFutureProcessed.putIfAbsent("ELECTION", true) == null) {
                System.out.println("GRPCalls, electionSelfCallAsync: Player " + myId + ": ELECTION message put to map, and I will become SEEKER in 12 s");
                timeoutFutureHolderElection.schedule(electionWonTask, 12000);
            }
        }

    }

    public static void coordinatorCallAsync(String serverAddress) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);
        GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);
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

    private static PlayerMessageRequest createResourceRequest() {
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
                .setMessageType(String.valueOf(MessageType.REQUEST_RESOURCE))
                .setTimestamp(String.valueOf(GlobalState.getStateObject().getMyTimestampResourceRequestsSent()))
                .build();
    }

    private static PlayerMessageRequest createResourceResponseRequest() {
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
                .setMessageType(String.valueOf(MessageType.RESOURCE_GRANTED))
                .build();
    }

    private static PlayerMessageRequest createSeekerTaggingRequest() {
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
                .setMessageType(String.valueOf(MessageType.SEEKER_TAGGING))
                .build();
    }

    private static PlayerMessageRequest createSeekerAskingRequest() {
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
                .setMessageType(String.valueOf(MessageType.SEEKER_ASKING))
                .build();
    }


    public static void requestResourceCallAsync(String serverAddress) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

        // REQUEST_RESOURCE type request
        PlayerMessageRequest request = createResourceRequest();
        System.out.println("GRPCalls, requestResourceCallAsync: Player: " + request.getId() + " set my PlayerState: WAITING_FOR_LOCK");
        stub.requestResource(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                MessageType messageTypeFromResponse = MessageType.valueOf(response.getMessageType());


                if (messageTypeFromResponse.equals(MessageType.RESOURCE_GRANTED)) {
                    System.out.println("GRPCalls, requestResourceCallAsync: Player: " + request.getId() + " response got from player: " + response.getId() + " with MESSAGE_TYPE RESOURCE_GRANTED");
                    Integer howManyResponsesReceived = GlobalState.getStateObject().increaseHowManyResourceGrantedResponsesGot();
                    List<PlayerExtended> listOfPlayersISentResourceRequestTo = GlobalState.getStateObject().getCopyOfPlayersISendResourceRequestsTo();
                    // If I got all the requests
                    if (listOfPlayersISentResourceRequestTo.size() == howManyResponsesReceived) {
                        GlobalState.getStateObject().setMyPlayerState(PlayerState.GOING_TO_BASE);
                    }
                } else if (messageTypeFromResponse.equals(MessageType.RESOURCE_NOT_GRANTED)) {
                    System.out.println("GRPCalls, requestResourceCallAsync: Player: " + request.getId() + " RESOURCE_NOT_GRANTED response got from player: " + response.getId());
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("requestResourceCallAsync" + t.getMessage());
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
            }
        });

    }

    public static void requestResourceResponseCallAsync(String serverAddress) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

        // REQUEST_RESOURCE type request
        PlayerMessageRequest request = createResourceResponseRequest();

        System.out.println("GRPCalls, requestResourceResponseCallAsync: Player: " + request.getId() + " sending to " + serverAddress);
        stub.responseResource(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                System.out.println("GRPCalls, requestResourceResponseCallAsync: Player: " + request.getId() + " allowed to go Player " + response.getId());
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

    public static void seekerAskingRequestCallAsync(String serverAddress) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

        // REQUEST_RESOURCE type request
        PlayerMessageRequest request = createSeekerAskingRequest();

//        System.out.println("GRPCalls, seekerAskingRequestCallAsync: Player: " + request.getId() + " sending ASKING_SEEKER to " + serverAddress);
        stub.seeker(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {
                PlayerState responsePlayerState = PlayerState.valueOf(response.getPlayerState());
                System.out.println("GRPCalls, seekerAskingRequestCallAsync: Player: " + request.getId() + " to Player " + response.getId() + " state " + responsePlayerState);

                if (responsePlayerState == PlayerState.WINNER || responsePlayerState == PlayerState.TAGGED) {
                    GlobalState.getStateObject().removePlayerFromTagListByPlayerId(response.getId());
                    GlobalState.getStateObject().addToFinalMapOfPlayers(response.getId(), responsePlayerState);
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

    public static void seekerTaggingRequestCallAsync(String serverAddress) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

        // REQUEST_RESOURCE type request
        PlayerMessageRequest request = createSeekerTaggingRequest();

//        System.out.println("GRPCalls, seekerTaggingRequestCallAsync: Player: " + request.getId() + " sending SEEKER_TAGGING to " + serverAddress);
        stub.seeker(request, new StreamObserver<PlayerMessageResponse>() {
            @Override
            public void onNext(PlayerMessageResponse response) {

                PlayerState responsePlayerState = PlayerState.valueOf(response.getPlayerState());
                System.out.println("GRPCalls, seekerTaggingRequestCallAsync: Player: " + request.getId() + " -> Player " + response.getId() + " state is: " + responsePlayerState);
                GlobalState.getStateObject().removePlayerFromTagListByPlayerId(response.getId());

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
