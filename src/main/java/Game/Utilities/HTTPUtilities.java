package Game.Utilities;

import Game.Global.GlobalState;
import Game.Models.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPUtilities {
    public static boolean httpPOSTPlayer(String playerId, String port, String address, String endpointUrl) {

        Map<String, String> map = new HashMap<>();
        map.put("id", playerId);
        map.put("port", port);
        map.put("address", address);

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        String json = gson.toJson(map);

        try {

            Client client = Client.create();
            WebResource webResource = client.resource(endpointUrl);

            ClientResponse response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
                    .post(ClientResponse.class, json);

            if (response.getStatus() == 201) {

                String jsonResponse = response.getEntity(String.class);
                JsonElement jsonElement = gson.fromJson(jsonResponse, JsonElement.class);
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                if (jsonObject.has("players")) {
                    Type playerListType = new TypeToken<List<Player>>() {
                    }.getType();
                    List<Player> players = gson.fromJson(jsonObject.get("players"), playerListType);
                    if (players.isEmpty()) {
                        System.out.println("HTTPUtilities: No players returned from admin server!\n");
                        return false;
                    } else {
                        System.out.println("HTTPUtilities: Players: ");
                        GlobalState.getStateObject().addPlayers(players);
                        System.out.println(players);

                        return true;

                    }

                } else {
                    System.out.println("HTTPUtilities: No players found in the response.");
                    return false;
                }
            } else {
                System.out.println("HTTPUtilities: Failed to get players. Status code: " + response.getStatus());

            }

            response.close();
            return false;
        } catch (Exception e) {

            System.out.println("HTTPUtilities: Administration Server is unavailable " + e);
            return false;
        }
    }


}

