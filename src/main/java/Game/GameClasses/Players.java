package Game.GameClasses;

import java.util.ArrayList;
import java.util.List;

public class Players {
    static Players instance;
    List<PlayerExtended> players;

    private Players() {
        this.players = new ArrayList<>();
    }

    synchronized static public Players getPlayersObject() {
        if (instance == null)
            instance = new Players();
        return instance;
    }

    synchronized void addPlayer(PlayerExtended player) {
        this.players.add(player);
    }

    synchronized void modifyPlayerRole(String playerId, Role role) {
        for (PlayerExtended player : this.players) {
            if (player.id.equals(playerId)) {
                player.role = role;
            }
        }
    }

    synchronized void modifyPlayerState(String playerId, PlayerState playerState) {
        for (PlayerExtended player : this.players) {
            if (player.id.equals(playerId)) {
                player.playerState = playerState;
            }
        }

    }

}
