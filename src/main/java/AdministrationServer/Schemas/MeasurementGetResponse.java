package AdministrationServer.Schemas;
import AdministrationServer.Models.MeasurementConverted;
import java.util.List;

public class MeasurementGetResponse {
    private List<MeasurementConverted> measurements;

    public List<MeasurementConverted> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<MeasurementConverted> measurements) {
        this.measurements = measurements;
    }

    public MeasurementGetResponse() {

    }
    public MeasurementGetResponse(List<MeasurementConverted> measurements) {
        this.measurements = measurements;
    }


}
