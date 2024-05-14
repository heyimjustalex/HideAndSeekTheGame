package AdministrationClient.Schemas;


import AdministrationClient.Models.Player;

public class PlayerAddResponse {
    private Player player;
    private String message;

    public PlayerAddResponse(Player player, String message) {
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
