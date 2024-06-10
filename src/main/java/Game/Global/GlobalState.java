package Game.Global;

import Game.GameClasses.GameState;
import Game.GameClasses.PlayerExtended;
import Game.GameClasses.PlayerState;
import Game.GameClasses.Role;
import Game.Models.Message;
import Game.Models.Player;
import Game.Services.GrpcCalls.GrpcCalls;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;

import static Game.GameClasses.GameState.*;

public class GlobalState {
    private static GlobalState instance;
    Long myTimestampResourceRequestsSent = null;
    ConcurrentHashMap<String, Boolean> electionFutureProcessed = new ConcurrentHashMap<>();
    ScheduledFuture<?>[] timeoutFutureHolderElection = new ScheduledFuture<?>[1];

    // This is for Ricart-Agrawala queue when I temporarily deny access to the resource
    BlockingQueue<PlayerExtended> playersThatRequireAccessToResource = new LinkedBlockingQueue<PlayerExtended>();


    Integer howManyResourceGrantedResponsesGot = 0;
    Integer howManyRequestResourceISent = 0;
    GameState gameState;
    PlayerState myPlayerState;
    String playerId;
    double myDistance;
    List<Message> mqttMessagesSent;
    List<PlayerExtended> players;
    List<PlayerExtended> playersToTag;

    Map<String, String> finalMapOfPlayers;
    Role myRole = Role.HIDER;

    private GlobalState() {
        // Available GAME states
        // BEFORE_ELECTION,
        // ELECTION_STARTED,
        // ELECTION_MESSAGES_SENT,
        // ELECTION_ENDED,
        // GAME_ENDED

        gameState = BEFORE_ELECTION;
        mqttMessagesSent = new ArrayList<>();
        players = new ArrayList<>();
        playersToTag = new ArrayList<>();
        finalMapOfPlayers = new HashMap<>();
        myPlayerState = PlayerState.AFTER_ELECTION;

    }

    synchronized static public GlobalState getStateObject() {
        if (instance == null)
            instance = new GlobalState();
        return instance;
    }

    public synchronized void addToFinalMapOfPlayers(String playerId, PlayerState playerState) {

        finalMapOfPlayers.put(playerId, String.valueOf(playerState));

    }

    public synchronized void setPlayersToTag(List<PlayerExtended> playersToTag) {
        this.playersToTag = playersToTag;
    }

    public synchronized Integer getHowManyRequestResourceISent() {
        return howManyRequestResourceISent;
    }

    public synchronized void setHowManyRequestResourceISent(Integer howManyRequestResourceISent) {
        this.howManyRequestResourceISent = howManyRequestResourceISent;
    }

    public synchronized List<PlayerExtended> getCopyOfPlayersISendResourceRequestsTo() {
        List<PlayerExtended> copiedPlayers = this.getPlayers();
        copiedPlayers.removeIf(player -> player.getId().equals(playerId) || player.getRole().equals(Role.SEEKER));
        return copiedPlayers;
    }

    public synchronized Integer increaseHowManyResourceGrantedResponsesGot() {

        this.howManyResourceGrantedResponsesGot += 1;
        return this.howManyResourceGrantedResponsesGot;
    }

    public synchronized Long getMyTimestampResourceRequestsSent() {

        if (this.myTimestampResourceRequestsSent == null) {
            this.myTimestampResourceRequestsSent = System.currentTimeMillis();
        }

        return this.myTimestampResourceRequestsSent;
    }

    synchronized public void setMyPlayerRole(Role role) {
        if (role != this.myRole) {
            System.out.println("GlobalState, setMyPlayerRole: Player: " + this.playerId + ": OldRole: " + this.myRole + " newRole: " + role);
            for (PlayerExtended player : this.players) {
                if (player.getId().equals(this.playerId)) {
                    player.setRole(role);
                    break;
                }
            }
        }
    }

    synchronized public PlayerState getMyPlayerState() {
        return this.myPlayerState;
    }

    synchronized public void setMyPlayerState(PlayerState playerState) {
        if (this.myPlayerState != playerState && playerState.ordinal() >= this.myPlayerState.ordinal()) {
            System.out.println("GlobalState, setMyPlayerState: Player: " + this.playerId + ": OldState: " + this.myPlayerState + " newState: " + playerState);
            for (PlayerExtended player : this.players) {
                if (player.getId().equals(this.playerId)) {
                    myPlayerState = playerState;
                    player.setPlayerState(playerState);
                }
            }
        }
        notifyAll();
    }

    public void addPlayerRequestingResourceToResourceQueue(PlayerExtended player) {
        this.playersThatRequireAccessToResource.add(player);
    }


    synchronized public void tryGoingToBase() throws InterruptedException {
        while (this.myPlayerState.equals(PlayerState.WAITING_FOR_LOCK) || this.myPlayerState.equals(PlayerState.AFTER_ELECTION)) {
            System.out.println("GlobalState, tryGoingToBase: I wait for my state until it's not AFTER_ELECTION or WAITING_FOR_LOCK");
            wait();
        }

        if (myPlayerState == PlayerState.GOING_TO_BASE) {
            double timeToReachBaseInSeconds = myDistance / 2;
            long timeToReachBaseInMilliseconds = (long) (timeToReachBaseInSeconds * 1000) + 10000;
            System.out.println("Player: " + playerId + " going to base. Needed time in seconds: " + timeToReachBaseInMilliseconds / 1000.0 + " Start time " + System.currentTimeMillis());
            Thread.sleep(timeToReachBaseInMilliseconds);
            setMyPlayerState(PlayerState.WINNER);
            addToFinalMapOfPlayers(playerId, PlayerState.WINNER);
            System.out.println("Player: " + playerId + " has just entered the base. End time " + System.currentTimeMillis());

        }

        if (myPlayerState == PlayerState.TAGGED || myPlayerState == PlayerState.WINNER) {
            while (!playersThatRequireAccessToResource.isEmpty()) {
                PlayerExtended playerWaitingForResource = playersThatRequireAccessToResource.poll();
                GrpcCalls.requestResourceResponseCallAsync(playerWaitingForResource.getAddress() + ":" + playerWaitingForResource.getPort());

            }
            setGameState(GAME_ENDED);
            System.out.println(this.finalMapOfPlayers);
        }
    }


    private synchronized PlayerExtended findSeekerInList(List<PlayerExtended> players) {
        for (PlayerExtended player : players) {
            if (player.getRole() == Role.SEEKER) {
                return player;
            }
        }
        return null;
    }


    private PlayerExtended findClosestPlayer(List<PlayerExtended> players, PlayerExtended seeker) {
        double minDistance = Double.MAX_VALUE;
        PlayerExtended minPlayer = null;
        for (PlayerExtended player : players) {
            if (player.getRole() != Role.SEEKER) {

                double calculatedDistance = Math.sqrt((Math.pow(player.getPos_x() - seeker.getPos_x(), 2) + Math.pow(player.getPos_y() - seeker.getPos_y(), 2)));
                if (calculatedDistance < minDistance) {
                    minPlayer = player;
                }
            }

        }
        return minPlayer;

    }

    public synchronized void removePlayerFromTagListByPlayerId(String playerId) {
        this.playersToTag.removeIf(player -> player.getId().equals(playerId) || player.getRole() == Role.SEEKER);
//        System.out.println("removePlayerFromTagListByPlayerId " + playerId + " List after removal: " + this.playersToTag);
    }


    public void tryCatchingHiders() throws InterruptedException {

        this.setPlayersToTag(getPlayers());

        PlayerExtended seekerPlayer = findSeekerInList(playersToTag);

        removePlayerFromTagListByPlayerId(playerId);

        Thread.sleep(3000);

        while (true) {
            if (playersToTag.isEmpty()) {
                break;
            }
            // In case someone greeted and added myPlayer
            removePlayerFromTagListByPlayerId(playerId);

            for (PlayerExtended player : playersToTag) {
                GrpcCalls.seekerAskingRequestCallAsync(player.getAddress() + ":" + player.getPort());
            }
            Thread.sleep(3000);

            PlayerExtended playerClosestToSeeker = this.findClosestPlayer(playersToTag, seekerPlayer);
            if (seekerPlayer != null && playerClosestToSeeker != null) {
                double calculatedDistance = Math.sqrt((Math.pow(playerClosestToSeeker.getPos_x() - seekerPlayer.getPos_x(), 2) + Math.pow(playerClosestToSeeker.getPos_y() - seekerPlayer.getPos_y(), 2)));
                double timeToReachOtherPlayer = calculatedDistance / 2;
                long timeToReachOtherPlayerInMilliseconds = (long) (timeToReachOtherPlayer * 1000);
                System.out.println("Seeker: " + playerId + " going to catch Player: " + playerClosestToSeeker.getId() + " Needed time in seconds: " + timeToReachOtherPlayerInMilliseconds / 1000.0 + " Start time " + System.currentTimeMillis());
                Thread.sleep(timeToReachOtherPlayerInMilliseconds);
                GrpcCalls.seekerTaggingRequestCallAsync(playerClosestToSeeker.getAddress() + ":" + playerClosestToSeeker.getPort());
                System.out.println("Player: " + playerId + " has just reached closest Player: " + playerClosestToSeeker.getId() + " spot " + System.currentTimeMillis());
            }
        }
        System.out.println("GlobalState, tryCatchingHiders: No more players to catch, ending game! ");
        this.setGameState(GAME_ENDED);
        System.out.println(this.finalMapOfPlayers);
    }


    public GameState getGameState() {
        return gameState;
    }


    public synchronized void setGameState(GameState gameState) {
        if (this.gameState != gameState && gameState.ordinal() > this.gameState.ordinal()) {
            System.out.println("GlobalState, setMyGameState: Player: " + this.playerId + ": OldGameState: " + this.gameState + " newGameState: " + gameState);
            this.gameState = gameState;

        }
        notifyAll();
    }

    public String getMyPlayerId() {
        return playerId;
    }

    public void setMyPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public synchronized void calculateMyDistance() {
        for (PlayerExtended player : this.players) {
            if (player.getId().equals(this.playerId)) {
                this.myDistance = player.getDistance();
            }
        }
    }

    public synchronized double getMyDistance() {
        return myDistance;
    }

    public synchronized List<Message> getMqttMessagesSent() {
        return mqttMessagesSent;
    }

    public synchronized void addPlayers(List<Player> playersFromAdminServer) {
        for (Player p : playersFromAdminServer) {
            boolean exists = false;
            for (PlayerExtended pe : players) {
                if (pe.getId().equals(p.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                players.add(new PlayerExtended(p));
            }
        }
    }

    public synchronized void addPlayersToSeekerTagList(List<PlayerExtended> playersFromGreeting) {
        for (PlayerExtended p : playersFromGreeting) {
            boolean exists = false;
            for (PlayerExtended pe : playersToTag) {
                if (pe.getId().equals(p.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                playersToTag.add(p);
            }
        }
    }

    public synchronized void setPlayer(PlayerExtended playerOverwrite) {
        ListIterator<PlayerExtended> iterator = this.players.listIterator();
        while (iterator.hasNext()) {
            PlayerExtended pe = iterator.next();
            if (pe.getId().equals(playerOverwrite.getId())) {
                iterator.set(playerOverwrite);
            }
        }
    }

    public synchronized void setChosenPlayerToSeeker(String playerId) {
        for (PlayerExtended player : players) {
            if (Objects.equals(player.getId(), playerId)) {
                System.out.println("GlobalState, setChosenPlayerToSeeker: " + playerId + " has been set to SEEKER");
                player.setRole(Role.SEEKER);
            }
        }
    }

    public synchronized PlayerExtended getMyPlayer() {
        for (PlayerExtended pe : this.players) {
            if (pe.getId().equals(this.playerId)) {
                return pe;
            }
        }
        return null;
    }

    public synchronized void addPlayer(PlayerExtended playerExtended) {
        boolean alreadyExists = false;
        for (PlayerExtended pe : this.players) {
            if (playerExtended.getId().equals(pe.getId())) {
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            this.players.add(playerExtended);
        }
    }


    public synchronized List<PlayerExtended> getPlayers() {
        return new ArrayList<>(this.players);
    }

    public synchronized GameState waitUntilElectionStarts() throws InterruptedException {
        System.out.println("GlobalState: waitUntilElectionStarts");
        while (this.gameState.equals(BEFORE_ELECTION)) {
            wait();
        }
        System.out.println("GlobalState: waitUntilElectionStarts: Changed game state to " + this.gameState);
        return this.gameState;
    }

    public synchronized GameState waitUntilElectionEnds() throws InterruptedException {
        System.out.println("GlobalState: waitUntilElectionEnds");
        while (!this.gameState.equals(ELECTION_ENDED)) {
            wait();
        }
        System.out.println("GlobalState: waitUntilElectionEnds: Changed game state to " + this.gameState);

        // Copy the list of players I will send resource requests to
//        this.setCopyOfPlayersISendResourceRequestsTo();

        // Print for no coordinator message edge case for 2 players
        this.printPlayersInformation();

        return this.gameState;
    }

    public synchronized void messageAdd(Message message) {
        System.out.println("BufferGameState:" + " consumed message " + message.getType() + ": " + message.getValue());
        if (message.getType().equals("gameState") && message.getValue().equals("ELECTION_STARTED") && this.gameState.equals(BEFORE_ELECTION)) {
            setGameState(ELECTION_STARTED);
        }
        mqttMessagesSent.add(message);
        notifyAll();
    }

    public synchronized ConcurrentHashMap<String, Boolean> getElectionFutureProcessed() {
        return electionFutureProcessed;
    }

    public synchronized ScheduledFuture<?>[] getTimeoutFutureHolderElection() {
        return timeoutFutureHolderElection;
    }

    public void printPlayersInformation() {
        System.out.println("Players information: ");
        for (PlayerExtended player : players) {
            System.out.println(player.getId() + " -> " + player.getAddress() + ":" + player.getPort() + " | PlayerRole:" + player.getRole() + " | PlayerState: " + player.getPlayerState() + " | Distance: " + player.getDistance() + " | pos_x: " + player.getPos_x() + "| pos_y: " + player.getPos_y());
        }
    }

}
