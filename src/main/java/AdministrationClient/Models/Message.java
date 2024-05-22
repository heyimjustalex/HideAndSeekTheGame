package AdministrationClient.Models;
public class MQTTMessage {
    String type;
    String value;

    public MQTTMessage(){}
    public MQTTMessage(String type, String value) {
        this.type=type;
        this.value=value;

    }
}
