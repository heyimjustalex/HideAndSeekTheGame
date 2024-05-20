package AdministrationServer.Services;

import AdministrationServer.Models.Measurement;
import AdministrationServer.Models.MeasurementConverted;
import AdministrationServer.Repositories.MeasurementRepository;
import AdministrationServer.Schemas.MeasurementAddRequest;
import AdministrationServer.Schemas.MeasurementAddResponse;
import AdministrationServer.Schemas.MeasurementGetResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MeasurementService {

    public MeasurementAddResponse addMeasurements(MeasurementAddRequest request){

        List<MeasurementConverted> allMeasurements = MeasurementRepository.getInstance().addMeasurements(request.getMeasurements());

        return new MeasurementAddResponse(allMeasurements);

    }
    public MeasurementGetResponse getMeasurements(){

        List<MeasurementConverted> allMeasurements = MeasurementRepository.getInstance().getMeasurements();
        System.out.println("RETR MEASUREMETNS "+ allMeasurements);

        return new MeasurementGetResponse(allMeasurements);

    }

    public MeasurementGetResponse getMeasurementsByTimestamp(String t1, String t2){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd:HH:mm:ss:SSS", Locale.ITALY);

            LocalDateTime t1Converted = LocalDateTime.parse(t1,formatter);
            LocalDateTime t2Converted = LocalDateTime.parse(t2,formatter);


        System.out.println(t1Converted);
        System.out.println(t2Converted);

        List<MeasurementConverted> allMeasurements = MeasurementRepository.getInstance().getMeasurementsByTimestamp(t1Converted,t2Converted);
        System.out.println("RETR MEASUREMETNS "+ allMeasurements);
//        return new MeasurementGetResponse();
        return new MeasurementGetResponse(allMeasurements);

    }
}
