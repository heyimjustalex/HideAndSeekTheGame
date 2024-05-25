package Game.Global;

import Game.GameClasses.GameState;
import Game.Models.Message;
import Game.GameClasses.PlayerExtended;
import Game.Models.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static Game.GameClasses.GameState.BEFORE_ELECTION;
import static Game.GameClasses.GameState.ELECTION_STARTED;

public class GlobalState {
    GameState gameState;
    String playerId;
    List<Message> mqttMessagesSent;
    List<PlayerExtended> players;
    private static GlobalState instance;

    private GlobalState() {
        // Available GAME states
        // BEFORE_ELECTION
        // ELECTION_STARTED
        // ELECTION_ENDED
        // GAME_ENDED
        gameState = BEFORE_ELECTION;
        mqttMessagesSent = new ArrayList<>();
        players = new ArrayList<>();

    }

    synchronized static public GlobalState getStateObject() {
        if (instance == null)
            instance = new GlobalState();
        return instance;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public String getMyPlayerId() {
        return playerId;
    }

    public void setMyPlayerId(String playerId) {
        this.playerId = playerId;
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

    public synchronized void setPlayer(PlayerExtended playerOverwrite) {
        ListIterator<PlayerExtended> iterator = this.players.listIterator();
        while (iterator.hasNext()) {
            PlayerExtended pe = iterator.next();
            if (pe.getId().equals(playerOverwrite.getId())) {
                iterator.set(playerOverwrite);
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
        return this.players;
    }

    public synchronized GameState waitUntilElectionStarts() throws InterruptedException {
        System.out.println("GlobalState: waitUntilElectionStarts: gameState " + BEFORE_ELECTION);
        while (this.gameState.equals(BEFORE_ELECTION)) {
            wait();
        }
        System.out.println("GlobalState: waitUntilElectionStarts: Changed game state to " + this.gameState);
        return this.gameState;
    }

    public synchronized void messageAdd(Message message) {
        System.out.println("BufferGameState: " + " consumed message " + message.getValue());
        if (message.getType().equals("gameState") && message.getValue().equals("ELECTION_STARTED") && this.gameState.equals(BEFORE_ELECTION)) {
            this.gameState = ELECTION_STARTED;
        }
        mqttMessagesSent.add(message);
        notifyAll();
    }


}
