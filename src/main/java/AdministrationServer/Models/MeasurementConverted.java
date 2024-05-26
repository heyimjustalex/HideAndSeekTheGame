package AdministrationServer.Models;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MeasurementConverted {
    private String id;
    private String type;
    private double value;
    private String timestamp;

    public MeasurementConverted() {
    }

    public MeasurementConverted(String id, String type, double value, Instant timestamp) {
        ZonedDateTime zonedDateTime = timestamp.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd:HH:mm:ss:SSS");
        String formattedDateTime = zonedDateTime.format(formatter);

        this.value = value;
        this.timestamp = formattedDateTime;
        this.id = id;
        this.type = type;
    }

    public MeasurementConverted(Measurement measurement) {
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(measurement.getTimestamp()).atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd:HH:mm:ss:SSS");
        String formattedDateTime = zonedDateTime.format(formatter);
        this.value = measurement.getValue();
        this.timestamp = formattedDateTime;
        this.id = measurement.getId();
        this.type = measurement.getType();
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

}
