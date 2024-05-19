package AdministrationServer.Schemas;

import AdministrationServer.Models.Measurement;

import java.util.List;

public class MeasurementAddRequest{
    private List<Measurement> measurements;

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public MeasurementAddRequest() {

    }
    public MeasurementAddRequest(List<Measurement> measurements) {

    }


}
