package AdministrationServer.Services;
import AdministrationServer.Models.MeasurementConverted;
import AdministrationServer.Repositories.MeasurementRepository;
import AdministrationServer.Schemas.MeasurementAddRequest;
import AdministrationServer.Schemas.MeasurementAddResponse;
import AdministrationServer.Schemas.MeasurementGetResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MeasurementService {

    public MeasurementAddResponse addMeasurements(MeasurementAddRequest request)
    {
        List<MeasurementConverted> allMeasurements = MeasurementRepository.getInstance().addMeasurements(request.getMeasurements());
        return new MeasurementAddResponse(allMeasurements);
    }
    public MeasurementGetResponse getMeasurements(){
        List<MeasurementConverted> allMeasurements = MeasurementRepository.getInstance().getMeasurements();
        System.out.println("MeasurementService: Retrieving measurements data: "+ allMeasurements);
        return new MeasurementGetResponse(allMeasurements);

    }

    public MeasurementGetResponse getMeasurementsByTimestamp(String t1, String t2){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd:HH:mm:ss:SSS", Locale.ITALY);

        LocalDateTime t1Converted = LocalDateTime.parse(t1,formatter);
        LocalDateTime t2Converted = LocalDateTime.parse(t2,formatter);


        System.out.println("MeasurementService: Retrieving measurements data from t1: "+t1Converted+" to t2: " +t2Converted);

        List<MeasurementConverted> allMeasurements = MeasurementRepository.getInstance().getMeasurementsByTimestamp(t1Converted,t2Converted);
        System.out.println("MeasurementService: Retrieved data from t1 to t2: "+ allMeasurements);

        return new MeasurementGetResponse(allMeasurements);

    }

    public MeasurementGetResponse getNMeasurementsByPlayerId(String playerId, Integer n){
        List<MeasurementConverted> allMeasurements = MeasurementRepository.getInstance().getNMeasurementsByClientId(playerId,n);
        System.out.println("MeasurementService: Retrieved "+n+" average data samples from "+playerId+" "+allMeasurements);

        return new MeasurementGetResponse(allMeasurements);

    }
}
