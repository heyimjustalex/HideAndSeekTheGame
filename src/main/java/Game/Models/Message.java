package Game.Models;
public class Message {
    String type;
    String value;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Message(){}
    public Message(String type, String value) {
        this.type=type;
        this.value=value;
    }
}
