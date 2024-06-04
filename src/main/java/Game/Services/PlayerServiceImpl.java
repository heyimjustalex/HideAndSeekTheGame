package Game.Services;

import Game.GameClasses.*;
import Game.Global.GlobalState;
import Game.Utilities.Other;
import io.grpc.stub.StreamObserver;
import proto.Player.PlayerMessageRequest;
import proto.Player.PlayerMessageResponse;
import proto.PlayerServiceGrpc.PlayerServiceImplBase;

import java.util.concurrent.*;

import static Game.GameClasses.MessageType.COORDINATOR;
import static Game.GameClasses.MessageType.GREETING_OK;

public class PlayerServiceImpl extends PlayerServiceImplBase {
    String myId = GlobalState.getStateObject().getMyPlayerId();

    private PlayerMessageResponse createGreetingOkMessage() {
        // I'm adding the player I got
        PlayerState myCurrentPlayerState = GlobalState.getStateObject().getMyPlayer().getPlayerState();
        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();
        GameState myCurrentGameState = GlobalState.getStateObject().getGameState();

        return PlayerMessageResponse.newBuilder()
                .setId(myId)
                .setPosX(myPlayer.getPos_x().toString())
                .setPosY(myPlayer.getPos_y().toString())
                .setGameState(myCurrentGameState.toString())
                .setPlayerState(myCurrentPlayerState.toString())
                .setMessageType(GREETING_OK.toString())
                .build();
    }

    @Override
    public void greeting(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {
        PlayerExtended playerExtended = new PlayerExtended(request.getId(), request.getPort(), request.getAddress(), request.getPosX(), request.getPosY(), request.getRole(), request.getPlayerState());
        GlobalState.getStateObject().addPlayer(playerExtended);

        if (MessageType.valueOf(request.getMessageType()) == MessageType.GREETING) {
            // I got a greeting message
            System.out.println("PlayerServiceImpl, greeting -> Player " + myId + ": GREETING message from player " + request.getId());

            
            // Responding with built message
            responseObserver.onNext(createGreetingOkMessage());
            responseObserver.onCompleted();
        }
    }


    @Override
    public void election(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {
        GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
        final ConcurrentHashMap<String, Boolean> electionFutureProcessed = GlobalState.getStateObject().getElectionFutureProcessed();
        final ScheduledFuture<?>[] timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();

        if (MessageType.valueOf(request.getMessageType()) == MessageType.ELECTION) {
            System.out.println("PlayerServiceImpl: Player: " + myId + ": Got an ELECTION message from Player: " + request.getId());
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            GameState requestGameState = GameState.valueOf(request.getGameState());

            // If I didn't set my state to election, I do that by getting the newest state from other players

            if (requestGameState.equals(GameState.ELECTION_STARTED) && (myCurrentGameState.equals(GameState.BEFORE_ELECTION))) {
                GlobalState.getStateObject().setGameState(GameState.ELECTION_STARTED);
            }


            double myDistance = GlobalState.getStateObject().getMyDistance();
            double otherPlayerDistance = Other.calculateDistanceToNearestBasePoint(Double.parseDouble(request.getPosX()), Double.parseDouble(request.getPosY()));
//            System.out.println("PlayerServiceImpl: Player: " + myId + ": if I don't get OK messages in 12s I will change to SEEKER");


            // Define the task to be scheduled
            Runnable electionTask = () -> {
                System.out.println("PlayerServiceImpl: Player " + myId + " I have become the SEEKER");
                GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);
                GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
                responseObserver.onNext(PlayerMessageResponse.newBuilder()
                        .setMessageType(String.valueOf(COORDINATOR))
                        .setId(myId)
                        .setGameState(GameState.ELECTION_ENDED.toString())
                        .build());
                System.out.println("PlayerServiceImpl: Player: " + myId + " timeout occurred. State set to SEEKER and sending COORDINATOR message ");
            };

            // Schedule the task if not already processed
            if (electionFutureProcessed.putIfAbsent("ELECTION", true) == null) {
                System.out.println("PlayerServiceImpl: Player: " + myId + " ELECTION message put to map, and I will become SEEKER in 12s");
                timeoutFutureHolderElection[0] = executor.schedule(electionTask, 12, TimeUnit.SECONDS);
            }

            if (myDistance > otherPlayerDistance || (myDistance == otherPlayerDistance && request.getId().compareToIgnoreCase(myId) > 0)) {
                System.out.println("PlayerServiceImpl: Player " + myId + " I got ELECTION message with player distance higher than mine from Player: " + request.getId() + " so I send him OK");
                responseObserver.onNext(PlayerMessageResponse.newBuilder()
                        .setMessageType(MessageType.ELECTION_OK.toString())
                        .setId(myId)
                        .build());

            } else {
                System.out.println("This is debug message for failure comparison");
                System.out.println("mydistance impl " + myDistance);
                System.out.println("his distance " + otherPlayerDistance);
                System.out.println("COMPARISON " + (request.getId().compareToIgnoreCase(myId) > 0));
//                responseObserver.onNext(PlayerMessageResponse.newBuilder()
//                        .setMessageType(MessageType.ELECTION_NOT_OK.toString())
//                        .setId(myId)
//                        .build());
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public void coordinator(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {
        if (MessageType.valueOf(request.getMessageType()) == COORDINATOR) {
            System.out.println("PlayerServiceImpl: " + myId + " got COORDINATOR message from " + request.getId() + " setting: gameState: ELECTION_ENDED, playerState:HIDER");
            GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
            GlobalState.getStateObject().setMyPlayerRole(Role.HIDER);
        }
        responseObserver.onCompleted();

    }

}
