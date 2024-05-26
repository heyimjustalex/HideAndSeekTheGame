package AdministrationServer.Repositories;

import AdministrationServer.Models.Measurement;
import AdministrationServer.Models.MeasurementConverted;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)

public class MeasurementRepository {
    private static MeasurementRepository instance;
    @XmlElement(name = "measurements")

    private List<MeasurementConverted> measurements;

    private MeasurementRepository() {
        this.measurements = new ArrayList<>();
    }

    public synchronized static MeasurementRepository getInstance() {
        if (instance == null)
            instance = new MeasurementRepository();
        return instance;
    }

    public synchronized List<MeasurementConverted> getMeasurements() {
        return new ArrayList<>(this.measurements);
    }

    public synchronized List<MeasurementConverted> addMeasurements(List<Measurement> newMeasurements) {
        List<MeasurementConverted> measurementsConverted = new ArrayList<>();
        for (Measurement measurement : newMeasurements) {
            measurementsConverted.add(new MeasurementConverted(measurement));
        }
        this.measurements.addAll(measurementsConverted);
        return measurements;
    }

    public List<MeasurementConverted> getNMeasurementsByClientId(String clientId, Integer n) {
        List<MeasurementConverted> measurementsCopy = getMeasurements();
        measurementsCopy = Lists.reverse(measurementsCopy);
        List<MeasurementConverted> outputCollection = new ArrayList<>();

        Integer iterator = 0;
        for (MeasurementConverted measurement : measurementsCopy) {
            if (measurement.getId().equalsIgnoreCase(clientId)) {
                outputCollection.add(measurement);
            }
            if (iterator >= n - 1) {
                break;
            }
            iterator++;
        }
        return outputCollection;
    }

    public List<MeasurementConverted> getMeasurementsByTimestamp(LocalDateTime timestampT1, LocalDateTime timestampT2) {
        List<MeasurementConverted> measurementsCopy = getMeasurements();
        List<MeasurementConverted> outputCollection = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd:HH:mm:ss:SSS", Locale.ITALY);


        for (MeasurementConverted measurement : measurementsCopy) {
            LocalDateTime measurementTimestamp = LocalDateTime.parse(measurement.getTimestamp(), formatter);

            if (measurementTimestamp.compareTo(timestampT1) >= 0 && measurementTimestamp.compareTo(timestampT2) <= 0) {
                outputCollection.add(measurement);
            }
        }

        return outputCollection;
    }


}
