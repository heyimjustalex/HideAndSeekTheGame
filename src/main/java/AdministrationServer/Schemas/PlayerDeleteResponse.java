package AdministrationServer.Schemas;


import AdministrationServer.Models.Player;

public class PlayerDeleteResponse {


    private Player player;
    private String message;

    public PlayerDeleteResponse(Player player, String message) {
       this.player = player;
        this.message = message;

    }
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }




}
