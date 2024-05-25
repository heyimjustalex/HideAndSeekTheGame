package Game.HeartRate.SimulatorsImplementation;

import Game.HeartRate.Simulators.Buffer;
import Game.HeartRate.Simulators.Measurement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;


public class AverageSender implements Runnable{
    private Buffer averageBuffer;
    public AverageSender(Buffer averageBuffer){
        this.averageBuffer = averageBuffer;
    }
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            List<Measurement> averages = averageBuffer.readAllAndClean();
            if (!averages.isEmpty()) {
//                System.out.println("Average sender: Sending the computed averages: "+ averages);
                sendAverages(averages);
            }

        }
    }

    private void sendAverages(List<Measurement> averages) {
        Map<String, List<Measurement>> map = new HashMap<>();
        map.put("measurements", averages);

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        String json = gson.toJson(map);

        String endpointUrl = "http://localhost:1337/players/measurements"; // Replace with your endpoint URL

        try{


        Client client = Client.create();
        WebResource webResource = client.resource(endpointUrl);

        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, json);

        if (response.getStatus() == 201) {
//            System.out.println("Average sender: Averages sent successfully.");
        } else {
//            System.out.println("Average sender: Failed to send averages. Status code: " + response.getStatus());
        }

        response.close();}
        catch (Exception e)
        {
//            System.out.println("Average sender: Administration Server is unavailable "+e);
        }
    }}

