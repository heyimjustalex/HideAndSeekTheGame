package Game.GameClasses;

import Game.Buffer.GameState;

import java.util.ArrayList;
import java.util.List;

public class Players {
    List<PlayerExtended> players;
    static Players instance;
    private Players(){
        this.players = new ArrayList<>();
    }
    synchronized static public Players getPlayersObject(){
        if(instance==null)
            instance = new Players();
        return instance;
    }
    synchronized void addPlayer(PlayerExtended player){
        this.players.add(player);
    }
    synchronized void modifyPlayerRole(String playerId, Role role){
        for (PlayerExtended player : this.players){
            if (player.id.equals(playerId)){
                player.role = role;
            }
        }
    }
    synchronized void modifyPlayerState(String playerId,State state)
    {
        for (PlayerExtended player : this.players){
            if (player.id.equals(playerId)){
                player.state = state;
            }
        }

    }

}
