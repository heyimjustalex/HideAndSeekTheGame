package Players.SimulatorsImplementation;

import Players.Simulators.Buffer;
import Players.Simulators.Measurement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;


public class AverageSender implements Runnable{

    private Buffer averageBuffer;
    private String playerId;
    private Client jerseyClient;
    private Gson gson;
    public AverageSender(Buffer averageBuffer, String playerId){
        this.averageBuffer = averageBuffer;
        this.playerId = playerId;
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
                System.out.println("SENDING"+ averages);
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

        Client client = Client.create();
        WebResource webResource = client.resource(endpointUrl);

        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, json);

        if (response.getStatus() == 200) {
            System.out.println("Averages sent successfully.");
        } else {
            System.out.println("Failed to send averages. Status code: " + response.getStatus());
        }

        response.close();
    }}

