package AdministrationServer.Schemas;

import AdministrationServer.Models.Player;

import java.util.List;

public class PlayersGetResponse {
    private List<Player> players;

    public PlayersGetResponse(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

}
