package Game.GameClasses;


public class PlayerExtended {
    String id;
    Integer port;
    String address;
    Integer pos_x;
    Integer pos_y;
    Double distance;
    Role role;
    State state;

    public PlayerExtended(){

    }
    public PlayerExtended(String id, Integer port, String address, Integer pos_x, Integer pos_y){
        this.id = id;
        this.port = port;
        this.address = address;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.distance = this.calculateDistanceToNearestBasePoint();
        this.role = Role.HIDER;
        this.state = State.UNTAGGED;
    }

    private Double calculateDistanceToNearestBasePoint() {

        double xb = pos_x;
        double yb = pos_y;

        double[][] baseCoords =  {
                {4.0, 4.0},
                {4.0, 5.0},
                {5.0, 5.0},
                {5.0, 4.0}
        };
        double xa = baseCoords[0][0];
        double ya = baseCoords[0][1];

        double calculated_distance = Math.sqrt((Math.pow(xa-xb,2)+Math.pow(ya-yb,2)));

        for (int i = 1; i < baseCoords.length; i++) {
             xa = baseCoords[i][0];
             ya = baseCoords[i][1];
             double new_distance = Math.sqrt((Math.pow(xa-xb,2)+Math.pow(ya-yb,2)));
            calculated_distance  = Math.min(calculated_distance , new_distance);
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

}
