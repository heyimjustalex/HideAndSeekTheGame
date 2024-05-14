package AdministrationClient.Models;


public class Player {
    String id;
    Integer port;
    String address;

    public Player(){

    }

    public Player(String id, Integer port, String address){
        this.id = id;
        this.port = port;
        this.address = address;

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




}
