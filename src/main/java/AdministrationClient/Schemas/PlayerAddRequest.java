package AdministrationClient.Schemas;

public class PlayerAddRequest {

    public String id;
    public Integer port;
    public String address;

    public PlayerAddRequest(){}

    public PlayerAddRequest(String id, Integer port, String address) {
        this.id = id;
        this.port = port;
        this.address = address;
    }


}
