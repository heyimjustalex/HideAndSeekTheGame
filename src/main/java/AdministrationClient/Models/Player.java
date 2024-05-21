package AdministrationServer.Models;
public class Player {
    String id;
    Integer port;
    String address;
    Integer pos_x;
    Integer pos_y;
    public Player(){

    }
    public Player(String id, Integer port, String address, Integer pos_x, Integer pos_y){
        this.id = id;
        this.port = port;
        this.address = address;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
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

}
