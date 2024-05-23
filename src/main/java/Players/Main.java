package Players;

import Players.Broker.SubscriberHandler;
import Players.Smartwatch.Simulators.Buffer;
import Players.Smartwatch.Simulators.HRSimulator;
import Players.Smartwatch.Simulators.Simulator;
import Players.Smartwatch.SimulatorsImplementation.AverageComputer;
import Players.Smartwatch.SimulatorsImplementation.AverageSender;
import Players.Smartwatch.SimulatorsImplementation.SharedAverageBuffer;
import Players.Smartwatch.SimulatorsImplementation.SharedMeasurementBuffer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static  boolean sendPlayerAddRequest(String playerId, String port, String address, String endpointUrl) {
        Map<String, String> map = new HashMap<>();
        map.put("id", playerId);
        map.put("port",port);
        map.put("address",address);

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        String json = gson.toJson(map);

        try{
            Client client = Client.create();
            WebResource webResource = client.resource(endpointUrl);

            ClientResponse response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
                    .post(ClientResponse.class, json);

            if (response.getStatus() == 201) {
                System.out.println("Player added successfully.");
                return true;
            } else {
                System.out.println("Player adding failed: player exists: " + response.getStatus());
            }

            response.close();}
        catch (Exception e)
        {
            System.out.println("Administration Server is unavailable "+e);
        }
        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        String playerId="";
        String endpointUrlAddPlayers = "http://localhost:1337/players/add";
//        boolean playerWasAdded = false;
//        while (!playerWasAdded){
//            Scanner scanner = new Scanner(System.in);
//            System.out.println("Enter playerId");
//            playerId = scanner.nextLine();
//            String port = "2222";
//            String address="localhost";
//           playerWasAdded= sendPlayerAddRequest(playerId,port,address,endpointUrlAddPlayers);
//        }
//
//        System.out.println("Main Thread PID: " + Thread.currentThread().getId());

        Buffer measurementBuffer = new SharedMeasurementBuffer();
        Buffer averageBuffer = new SharedAverageBuffer();
        Simulator simulator = new HRSimulator(playerId,measurementBuffer);

        Thread averageComputerThread = new Thread(new AverageComputer(measurementBuffer,averageBuffer,playerId));
        Thread averageSenderThread = new Thread(new AverageSender(averageBuffer));

        averageSenderThread.start();
        averageComputerThread.start();
        simulator.start();

        try {
            SubscriberHandler.handleSubscription();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        averageComputerThread.join();
        simulator.join();


    }
}
