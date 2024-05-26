package AdministrationClient.Models;

public class Measurement {
    private String id;
    private String type;
    private double value;
    private String timestamp;

    public Measurement() {
    }

    public Measurement(String id, String type, double value, String timestamp) {
        this.value = value;
        this.timestamp = timestamp;
        this.id = id;
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String type) {
        this.id = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Measurement{id:'" + id + "', type:'" + type + "', timestamp:'" + timestamp + "', value:'" + value + "'" + '}';
    }

}
