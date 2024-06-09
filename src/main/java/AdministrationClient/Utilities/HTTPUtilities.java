package AdministrationClient.Utilities;

import AdministrationClient.Models.Measurement;
import AdministrationClient.Models.Message;
import AdministrationClient.Models.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.Type;
import java.util.List;

public class HTTPUtilities {

    public static void calculateAndPrintAverage(List<Measurement> measurements) {
        double total = 0.0;

        for (Measurement measurement : measurements) {
            total += measurement.getValue();
        }

        double average = total / measurements.size();
        System.out.println("Averages of " + measurements.size() + " averages: ");
        System.out.println(average);
        System.out.println("\n");
    }

    public static void httpGetPlayers(String endpointUrl) {
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(endpointUrl);

            ClientResponse response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
                    .get(ClientResponse.class);

            if (response.getStatus() == 200) {
                String jsonResponse = response.getEntity(String.class);
                Gson gson = new GsonBuilder().serializeNulls().create();

                JsonElement jsonElement = gson.fromJson(jsonResponse, JsonElement.class);
                JsonObject jsonObject = jsonElement.getAsJsonObject();


                if (jsonObject.has("players")) {
                    Type playerListType = new TypeToken<List<Player>>() {
                    }.getType();
                    List<Player> players = gson.fromJson(jsonObject.get("players"), playerListType);
                    if (players.isEmpty()) {
                        System.out.println("\nNo players found in game!\n");
                    } else {
                        System.out.println("\nPlayers: ");
                        for (Player player : players) {
                            System.out.println(player.toString());
                        }

                        System.out.println("\n");

                    }

                } else {
                    System.out.println("No players found in the response.");
                }
            } else {
                System.out.println("Failed to get players. Status code: " + response.getStatus());
            }

            response.close();
        } catch (Exception e) {
            System.out.println("Administration Server is unavailable " + e);
        }
    }


    public static void httpGetNMeasurementsByPlayerId(String endpointUrl, String playerId, String n) {
        try {

            Client client = Client.create();
            WebResource webResource = client.resource(endpointUrl);

            ClientResponse response = webResource
                    .queryParam("playerId", playerId)
                    .queryParam("n", n)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(ClientResponse.class);

            if (response.getStatus() == 200) {
                String jsonResponse = response.getEntity(String.class);
                Gson gson = new GsonBuilder().serializeNulls().create();

                JsonElement jsonElement = gson.fromJson(jsonResponse, JsonElement.class);
                JsonObject jsonObject = jsonElement.getAsJsonObject();


                if (jsonObject.has("measurements")) {
                    Type measurementListType = new TypeToken<List<Measurement>>() {
                    }.getType();
                    List<Measurement> measurements = gson.fromJson(jsonObject.get("measurements"), measurementListType);
                    if (measurements.isEmpty()) {
                        System.out.println("\nNo measurements found for this playerId!\n");
                    } else {
                        System.out.println("\nMeasurements: ");
                        for (Measurement measurement : measurements) {
                            System.out.println(measurement.toString());
                        }
                        calculateAndPrintAverage(measurements);
                        System.out.println("\n");

                    }

                } else {
                    System.out.println("No measurements found in the response.");
                }
            } else {
                System.out.println("Failed to get measurements. Status code: " + response.getStatus());
            }

            response.close();
        } catch (Exception e) {
            System.out.println("Administration Server is unavailable " + e);
        }
    }

    public static void httpGetNMeasurementsByTimestamps(String endpointUrl, String t1, String t2) {
        try {

            Client client = Client.create();
            WebResource webResource = client.resource(endpointUrl);

            ClientResponse response = webResource
                    .queryParam("t1", t1)
                    .queryParam("t2", t2)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(ClientResponse.class);

            if (response.getStatus() == 200) {
                String jsonResponse = response.getEntity(String.class);
                Gson gson = new GsonBuilder().serializeNulls().create();

                JsonElement jsonElement = gson.fromJson(jsonResponse, JsonElement.class);
                JsonObject jsonObject = jsonElement.getAsJsonObject();


                if (jsonObject.has("measurements")) {
                    Type measurementListType = new TypeToken<List<Measurement>>() {
                    }.getType();
                    List<Measurement> measurements = gson.fromJson(jsonObject.get("measurements"), measurementListType);
                    if (measurements.isEmpty()) {
                        System.out.println("\nNo measurements found for this timestamp!\n");
                    } else {
                        System.out.println("\nMeasurements: ");
                        for (Measurement measurement : measurements) {
                            System.out.println(measurement.toString());
                        }
                        System.out.println("\n");
                        calculateAndPrintAverage(measurements);


                    }

                } else {
                    System.out.println("No measurements found in the response.");
                }
            } else {
                System.out.println("Failed to get measurements. Status code: " + response.getStatus());
            }

            response.close();
        } catch (Exception e) {
            System.out.println("Administration Server is unavailable " + e);
        }
    }

    public static void broadcastMqttMessage(String messageType, String messageValue, boolean isRetained) throws MqttException {
        Message message = new Message(messageType, messageValue);
        String topic = "/broadcast";
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        int qos = 2;
        MqttClient client = new MqttClient(broker, clientId);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        System.out.println(clientId + " Publisher: Connecting Broker " + broker);
        client.connect(connOpts);
        System.out.println(clientId + " Publisher: Connected");


        try {

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();

            String jsonedMessage = gson.toJson(message);

            MqttMessage mqttMessage = new MqttMessage(jsonedMessage.getBytes());

            //Set the QoS on the Message and Retained
            mqttMessage.setQos(qos);
//            mqttMessage.setRetained(isRetained);


            System.out.println(clientId + " Publisher: Publishing message: " + mqttMessage + " ...");
            client.publish(topic, mqttMessage);
            System.out.println(clientId + " Publisher: Message published");


        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

}

