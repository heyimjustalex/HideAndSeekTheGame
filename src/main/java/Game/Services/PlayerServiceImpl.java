package Game.Services;

import Game.GameClasses.*;
import Game.Global.GlobalState;
import Game.Utilities.Other;
import io.grpc.stub.StreamObserver;
import proto.Player.PlayerMessageRequest;
import proto.Player.PlayerMessageResponse;
import proto.PlayerServiceGrpc.PlayerServiceImplBase;

import java.util.Objects;
import java.util.concurrent.*;

import static Game.GameClasses.MessageType.*;
import static Game.Services.GrpcCalls.GrpcCalls.coordinatorCallAsync;

public class PlayerServiceImpl extends PlayerServiceImplBase {
    String myId = GlobalState.getStateObject().getMyPlayerId();

    private PlayerMessageResponse createGreetingOkMessage() {
        // I'm adding the player I got
        PlayerState myCurrentPlayerState = GlobalState.getStateObject().getMyPlayer().getPlayerState();
        Role myCurrentPlayerRole = GlobalState.getStateObject().getMyPlayer().getRole();
        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();
        GameState myCurrentGameState = GlobalState.getStateObject().getGameState();

        return PlayerMessageResponse.newBuilder()
                .setId(myId)
                .setPosX(myPlayer.getPos_x().toString())
                .setPosY(myPlayer.getPos_y().toString())
                .setGameState(myCurrentGameState.toString())
                .setPlayerState(myCurrentPlayerState.toString())
                .setRole(myCurrentPlayerRole.toString())
                .setMessageType(GREETING_OK.toString())
                .build();
    }

    private PlayerMessageResponse createGreetingNotOkMessage() {
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
                .setMessageType(GREETING.toString())
                .build();
    }

    @Override
    public void greeting(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {
        PlayerExtended playerExtended = new PlayerExtended(request.getId(), request.getPort(), request.getAddress(), request.getPosX(), request.getPosY(), request.getRole(), request.getPlayerState());
        GlobalState.getStateObject().addPlayer(playerExtended);
        // This is the list of players that will matter when somebody joins after election
        // Right after election list is overwritten by copy of players list
//        GlobalState.getStateObject().addPlayerToCopyOfPlayersISendResourceRequestsTo(playerExtended);


        if (MessageType.valueOf(request.getMessageType()) == MessageType.GREETING) {
            // I got a greeting message
            GameState myCurrentGameState = GlobalState.getStateObject().getGameState();
            System.out.println("PlayerServiceImpl, greeting -> Player " + myId + ": GREETING message from player " + request.getId() + " myCurrentGameState: " + myCurrentGameState.name());


            // If the messages have already been sent
            if (myCurrentGameState == GameState.ELECTION_MESSAGES_SENT || myCurrentGameState == GameState.ELECTION_STARTED) {
                double myDistance = GlobalState.getStateObject().getMyDistance();
                double otherPlayerDistance = Other.calculateDistanceToNearestBasePoint(Double.parseDouble(request.getPosX()), Double.parseDouble(request.getPosY()));
                // If the distance of the greeting player is lower, then cancel my SEEKER election
                if (myDistance > otherPlayerDistance || (myDistance == otherPlayerDistance && request.getId().compareToIgnoreCase(myId) > 0)) {
                    System.out.println("PlayerServiceImpl: Player " + myId + " I got GREETING and I'm in ELECTION_MESSAGES_SENT " + request.getId() + " so I cancel my LEADER");

                    final ScheduledFuture<?>[] timeoutFutureHolderElection = GlobalState.getStateObject().getTimeoutFutureHolderElection();
                    if (timeoutFutureHolderElection[0] != null) {
                        timeoutFutureHolderElection[0].cancel(true);
                    }
                    responseObserver.onNext(createGreetingNotOkMessage());
                    responseObserver.onCompleted();
                }
            } else {
                responseObserver.onNext(createGreetingOkMessage());
                responseObserver.onCompleted();
            }

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
            } else if (requestGameState.equals(GameState.ELECTION_MESSAGES_SENT) && (myCurrentGameState.equals(GameState.ELECTION_STARTED))) {
                GlobalState.getStateObject().setGameState(GameState.ELECTION_MESSAGES_SENT);
            }

            double myDistance = GlobalState.getStateObject().getMyDistance();
            double otherPlayerDistance = Other.calculateDistanceToNearestBasePoint(Double.parseDouble(request.getPosX()), Double.parseDouble(request.getPosY()));

// Define the task to be scheduled - you have to because in case this has the highest, he wont send any election messages from grpccalls, so he has no chance to start seeker processs
            Runnable electionTask = () -> {
                System.out.println("PlayerServiceImpl: Player " + myId + " I have become the SEEKER");
                GlobalState.getStateObject().setMyPlayerRole(Role.SEEKER);

                try {
                    for (PlayerExtended player : GlobalState.getStateObject().getPlayers()) {
                        if (!Objects.equals(player.getId(), myId)) {
                            coordinatorCallAsync(player.getAddress() + ":" + player.getPort());
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Couldn't send coordinator messages! " + e);
                }

                System.out.println("PlayerServiceImpl: Player: " + myId + " timeout occurred. State set to SEEKER and sending COORDINATOR message ");
            };

            // Schedule the task if not already processed
            if (electionFutureProcessed.putIfAbsent("ELECTION", true) == null) {
                System.out.println("PlayerServiceImpl: Player: " + myId + " ELECTION message put to map, and I will become SEEKER in 12s");
                timeoutFutureHolderElection[0] = executor.schedule(electionTask, 12, TimeUnit.SECONDS);
            }

            if (myDistance < otherPlayerDistance || (myDistance == otherPlayerDistance && myId.compareToIgnoreCase(request.getId()) > 0)) {
                System.out.println("PlayerServiceImpl: Player: " + myId + " I got ELECTION message with lower priority than mine from Player: " + request.getId() + " so I send him OK");
                responseObserver.onNext(PlayerMessageResponse.newBuilder()
                        .setMessageType(MessageType.ELECTION_OK.toString())
                        .setId(myId)
                        .build());
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public void coordinator(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {
        if (MessageType.valueOf(request.getMessageType()) == COORDINATOR) {
            System.out.println("PlayerServiceImpl: " + myId + " got COORDINATOR message from " + request.getId() + " setting: gameState: ELECTION_ENDED, playerState:HIDER ");
            GlobalState.getStateObject().setMyPlayerRole(Role.HIDER);
            GlobalState.getStateObject().setChosenPlayerToSeeker(request.getId());
            GlobalState.getStateObject().setGameState(GameState.ELECTION_ENDED);
            GlobalState.getStateObject().printPlayersInformation();
        }
        responseObserver.onNext(PlayerMessageResponse.newBuilder().setId(myId).build());
        responseObserver.onCompleted();
    }

    @Override
    public void requestResource(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {
        if (MessageType.valueOf(request.getMessageType()) == REQUEST_RESOURCE) {

            PlayerState myPlayerState = GlobalState.getStateObject().getMyPlayerState();
            Long myTimestamp = GlobalState.getStateObject().getMyTimestampResourceRequestsSent();
            // This line is to test case when timestamps are equal
//            myTimestamp = Long.parseLong(request.getTimestamp());
            System.out.println("PlayerServiceImpl, requestResource: MyTimestamp: " + myTimestamp + " Player: " + request.getId() + " timestamp: " + request.getTimestamp());

            boolean myPriorityIsLower = myPlayerState.equals(PlayerState.WAITING_FOR_LOCK) && (myTimestamp > Long.parseLong(request.getTimestamp())
                    || (myTimestamp == Long.parseLong(request.getTimestamp()) && myId.compareToIgnoreCase(request.getId()) > 0));

            // If i dont care about accessing home base or or other party has lower timestamp, then grant access
            if (myPlayerState.equals(PlayerState.AFTER_ELECTION)
                    || myPlayerState.equals(PlayerState.TAGGED)
                    || myPlayerState.equals(PlayerState.WINNER)
                    || myPriorityIsLower) {
                System.out.println("PlayerServiceImpl, requestResource: Sending RESOURCE_GRANTED to " + request.getId());

                responseObserver.onNext(
                        PlayerMessageResponse
                                .newBuilder()
                                .setId(myId)
                                .setMessageType(String.valueOf(RESOURCE_GRANTED))
                                .build());
            }
            // I cannot grant access and I put it in the queue
            else {
                System.out.println("PlayerServiceImpl, requestResource: Sending RESOURCE_NOT_GRANTED to " + request.getId());
                responseObserver.onNext(
                        PlayerMessageResponse
                                .newBuilder()
                                .setId(myId)
                                .setMessageType(String.valueOf(RESOURCE_NOT_GRANTED))
                                .build());
                // Add the request for the resource to the Queue
                System.out.println("PlayerServiceImpl, requestResource: Putting REQUEST_NOT_GRANTED in queue  | of this Player:-> " + request.getId());
                PlayerExtended playerRequestingResource = new PlayerExtended(request.getId(), request.getPort(), request.getAddress(), request.getPosX(), request.getPosY(), request.getRole(), request.getPlayerState());
                GlobalState.getStateObject().addPlayerRequestingResourceToResourceQueue(playerRequestingResource);

            }
            responseObserver.onCompleted();

        }
    }

    @Override
    public void responseResource(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {
        if (MessageType.valueOf(request.getMessageType()) == RESOURCE_GRANTED) {

            Integer howManyResponsesReceived = GlobalState.getStateObject().increaseHowManyResourceGrantedResponsesGot();
            Integer howManyResponsesISent = GlobalState.getStateObject().getHowManyRequestResourceISent();

            // If I got all of the requests
            if (Objects.equals(howManyResponsesISent, howManyResponsesReceived)) {
                GlobalState.getStateObject().setMyPlayerState(PlayerState.GOING_TO_BASE);
            }
            // Send ACK
            responseObserver.onNext(PlayerMessageResponse.newBuilder()
                    .setId(myId)
                    .setMessageType(ACK.toString())
                    .build());
            responseObserver.onCompleted();
        }
    }


    @Override
    public void seeker(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver) {

        MessageType requestMessageType = MessageType.valueOf(request.getMessageType());
        if (requestMessageType == SEEKER_ASKING) {
            responseObserver.onNext(PlayerMessageResponse.newBuilder()
                    .setId(myId)
                    .setPlayerState(GlobalState.getStateObject().getMyPlayerState().toString())
                    .setMessageType(ACK.toString())
                    .build());

        } else if (requestMessageType == SEEKER_TAGGING) {
            PlayerState myPlayerState = GlobalState.getStateObject().getMyPlayerState();
            if (myPlayerState == PlayerState.AFTER_ELECTION || myPlayerState == PlayerState.WAITING_FOR_LOCK) {
                GlobalState.getStateObject().setMyPlayerState(PlayerState.TAGGED);
            }
            responseObserver.onNext(PlayerMessageResponse.newBuilder()
                    .setId(myId)
                    .setPlayerState(GlobalState.getStateObject().getMyPlayerState().toString())
                    .setMessageType(ACK.toString())
                    .build());

        }
        responseObserver.onCompleted();
    }


}
