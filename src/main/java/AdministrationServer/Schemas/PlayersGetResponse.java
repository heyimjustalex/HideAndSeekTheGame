package AdministrationServer.Schemas;


import AdministrationServer.Models.Player;

import java.util.List;

public class PlayerGetResponse {

    private String message;
    private List<Player> players;
    public PlayerGetResponse(List<Player> players, String message) {
        this.message = message;
        this.players = players;

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }




    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }







}
