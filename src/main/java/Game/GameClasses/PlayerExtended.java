package Game.GameClasses;


import Game.Models.Player;

public class PlayerExtended {
    String id;
    Integer port;
    String address;
    Integer pos_x;
    Integer pos_y;
    Double distance;
    Role role;
    PlayerState playerState;

    public PlayerExtended() {

    }

    public PlayerExtended(String id, String port, String address, String pos_x, String pos_y, String role, String state) {
        this.id = id;
        this.port = Integer.valueOf(port);
        this.address = address;
        this.pos_x = Integer.valueOf(pos_x);
        this.pos_y = Integer.valueOf(pos_y);
        this.distance = this.calculateDistanceToNearestBasePoint();
        this.role = Role.valueOf(role);
        this.playerState = PlayerState.valueOf(state);
    }

    public PlayerExtended(String id, Integer port, String address, Integer pos_x, Integer pos_y, Role role, PlayerState playerState) {
        this.id = id;
        this.port = port;
        this.address = address;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.distance = this.calculateDistanceToNearestBasePoint();
        this.role = role;
        this.playerState = playerState;
    }

    public PlayerExtended(String id, Integer port, String address, Integer pos_x, Integer pos_y) {
        this.id = id;
        this.port = port;
        this.address = address;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.distance = this.calculateDistanceToNearestBasePoint();
        this.role = Role.HIDER;
        this.playerState = PlayerState.AFTER_ELECTION;
    }

    public PlayerExtended(Player p) {
        this.id = p.getId();
        this.port = p.getPort();
        this.address = p.getAddress();
        this.pos_x = p.getPos_x();
        this.pos_y = p.getPos_y();
        this.distance = this.calculateDistanceToNearestBasePoint();
        this.role = Role.HIDER;
        this.playerState = PlayerState.AFTER_ELECTION;
    }

    private Double calculateDistanceToNearestBasePoint() {

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPos_x() {
        return pos_x;
    }

    public void setPos_x(Integer pos_x) {
        this.pos_x = pos_x;
    }

    public Integer getPos_y() {
        return pos_y;
    }

    public void setPos_y(Integer pos_y) {
        this.pos_y = pos_y;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerState playerState) {
//        if (this.playerState != playerState && playerState.ordinal() > this.playerState.ordinal()) {
//        System.out.println("PlayerExtended: Setting new playerState: " + playerState);
        this.playerState = playerState;

//        }
    }

}
