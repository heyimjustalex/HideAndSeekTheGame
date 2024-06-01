package Game.Services;

import Game.GameClasses.*;
import Game.Global.GlobalState;
import io.grpc.stub.StreamObserver;
import proto.Player.PlayerMessageRequest;
import proto.Player.PlayerMessageResponse;
import proto.PlayerServiceGrpc.PlayerServiceImplBase;

import java.util.concurrent.*;

import static Game.GameClasses.MessageType.GREETING_OK;


public class PlayerServiceImpl extends PlayerServiceImplBase {

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

    @Override
    public void election(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {
//        PlayerExtended playerExtended = new PlayerExtended(request.getId(), request.getPort(), request.getAddress(), request.getPosX(), request.getPosY(), request.getRole(), request.getPlayerState());
//        GameState requestGameState = GameState.valueOf(request.getGameState());
        String myId = GlobalState.getStateObject().getMyPlayerId();
        final ConcurrentHashMap<String, Boolean> messageElectionGreetingProcessed = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, Boolean> messageElectionProcessed = new ConcurrentHashMap<>();
        final ScheduledFuture<?>[] timeoutFutureHolder = new ScheduledFuture<?>[1];
        final ScheduledFuture<?>[] timeoutFutureHolderGreetingElection = new ScheduledFuture<?>[1];
        switch (MessageType.valueOf(request.getMessageType())) {
            case GREETING -> {
                // I got a greeting message
                System.out.println(myId + " Got a GREETING message from " + request.getId());
                PlayerExtended playerExtended = new PlayerExtended(request.getId(), request.getPort(), request.getAddress(), request.getPosX(), request.getPosY(), request.getRole(), request.getPlayerState());
                GlobalState.getStateObject().addPlayer(playerExtended);

                GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
                PlayerState myCurrentPlayerState = GlobalState.getStateObject().getMyPlayer().getPlayerState();

                PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();

                responseObserver.onNext(PlayerMessageResponse.newBuilder()
                        .setId(myId)
                        .setPosX(myPlayer.getPos_x().toString())
                        .setPosY(myPlayer.getPos_y().toString())
                        .setGameState(myCurrentGameState.toString())
                        .setPlayerState(myCurrentPlayerState.toString())
                        .setMessageType(GREETING_OK.toString())
                        .build());

            }
            case GREETING_OK -> {
                System.out.println(myId + " Got a GREETING_OK message from " + request.getId());
                GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
                GameState requestGameState = GameState.valueOf(request.getGameState());

                if (requestGameState.ordinal() > myCurrentGameState.ordinal()) {
                    System.out.println(myId + " GREETING_OK message from: " + request.getId() + " changed my state to " + requestGameState);
                    GlobalState.getStateObject().setGameState(requestGameState);
                }

                if (requestGameState.equals(GameState.ELECTION_MESSAGES_SENT)) {
                    // This means that election messages have been sent, so you need to send them by examining state
                    // you got from GREETING_OK response to take part in
                    double myDistance = GlobalState.getStateObject().getMyDistance();
                    double otherPlayerDistance = calculateDistanceToNearestBasePoint(Double.parseDouble(request.getPosX()), Double.parseDouble(request.getPosY()));
                    System.out.println(myId + " GREETING_OK election message, if I don't get OK messages in 3s I will change to leader");
                    PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();

                    if (myDistance > otherPlayerDistance || (myDistance == otherPlayerDistance && myId.compareToIgnoreCase(request.getId()) > 0)) {
                        System.out.println(myId + " I got ELECTION message with player distance lower than mine from " + request.getId() + " so I send him OK");
                        responseObserver.onNext(PlayerMessageResponse.newBuilder()
                                .setMessageType(MessageType.ELECTION.toString())
                                .setPosX(myPlayer.getPos_x().toString())
                                .setPosY(myPlayer.getPos_y().toString())
                                .setId(myId)
                                .build());
                    }

                    if (messageElectionGreetingProcessed.putIfAbsent("ELECTION", true) == null) {
                        System.out.println(myId + " ELECTION !FROM GREETING! message put to map, and I will become leader in 3 s");
                        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                        timeoutFutureHolderGreetingElection[0] = executor.schedule(() -> {
                            GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);
                            GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
                            responseObserver.onNext(PlayerMessageResponse.newBuilder()
                                    .setMessageType(String.valueOf(MessageType.COORDINATOR))
                                    .setId(myId)
                                    .setGameState(GameState.ELECTION_ENDED.toString())
                                    .build());
                            System.out.println(myId + " timeout occurred. State set to LEADER and sending COORDINATOR message ");
                        }, 3, TimeUnit.SECONDS);
                    }
                }


            }
            case ELECTION -> {
                System.out.println(myId + " Got an ELECTION message from " + request.getId());
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
                GameState requestGameState = GameState.valueOf(request.getGameState());

                // If I didn't set my state to election, I do that by getting the newest state from other players


                if (requestGameState.equals(GameState.ELECTION_STARTED) && (myCurrentGameState.equals(GameState.BEFORE_ELECTION))) {
                    GlobalState.getStateObject().setGameState(GameState.ELECTION_STARTED);
                }


                double myDistance = GlobalState.getStateObject().getMyDistance();
                double otherPlayerDistance = calculateDistanceToNearestBasePoint(Double.parseDouble(request.getPosX()), Double.parseDouble(request.getPosY()));
                System.out.println(myId + " if I don't get OK messages in 3s I will change to leader");

                // Change my role to LEADER after 2 seconds
                if (messageElectionProcessed.putIfAbsent("ELECTION", true) == null) {
                    System.out.println(myId + " ELECTION message put to map, and I will become leader in 3 s");
                    timeoutFutureHolder[0] = executor.schedule(() -> {
                        GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);
                        GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
                        responseObserver.onNext(PlayerMessageResponse.newBuilder()
                                .setMessageType(String.valueOf(MessageType.COORDINATOR))
                                .setId(myId)
                                .setGameState(GameState.ELECTION_ENDED.toString())
                                .build());
                        System.out.println(myId + " timeout occurred. State set to LEADER and sending COORDINATOR message ");
                    }, 3, TimeUnit.SECONDS);
                }

                if (myDistance > otherPlayerDistance || (myDistance == otherPlayerDistance && myId.compareToIgnoreCase(request.getId()) > 0)) {
                    System.out.println(myId + " I got ELECTION message with player distance lower than mine from " + request.getId() + " so I send him OK");
                    responseObserver.onNext(PlayerMessageResponse.newBuilder()
                            .setMessageType(MessageType.OK.toString())
                            .build());

                }

            }

            case OK -> {
                System.out.println(myId + " timeout occurred. State set to HIDER because i got OK message from player " + request.getId());
                if (timeoutFutureHolder[0] != null) {
                    timeoutFutureHolder[0].cancel(true);
                }
                if (timeoutFutureHolderGreetingElection[0] != null) {
                    timeoutFutureHolderGreetingElection[0].cancel(true);
                }
                GlobalState.getStateObject().setMyPlayerRole(Role.HIDER);
            }

            case COORDINATOR -> {
                System.out.println(myId + " got COORDINATOR message from " + request.getId() + " setting: gameState: ELECTION_ENDED, playerState:HIDER");
                GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
                GlobalState.getStateObject().setMyPlayerRole(Role.HIDER);
            }


//            if (myCurrentGameState.ordinal() > GameState.BEFORE_ELECTION.ordinal()) {}
        }

//        System.out.println("PlayerServiceImpl greeting: " + GlobalState.getStateObject().getPlayers());
//        responseObserver.onNext(PlayerMessageResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

}
