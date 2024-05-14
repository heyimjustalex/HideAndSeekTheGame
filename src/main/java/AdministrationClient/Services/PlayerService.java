package AdministrationClient.Services;

import AdministrationClient.Models.Player;
import AdministrationClient.Repositories.PlayerRepository;
import AdministrationClient.Schemas.PlayerAddRequest;
import AdministrationClient.Schemas.PlayerAddResponse;
import AdministrationClient.Schemas.PlayerDeleteRequest;
import AdministrationClient.Schemas.PlayerDeleteResponse;


public class PlayerService {
    public synchronized PlayerAddResponse addPlayer(PlayerAddRequest playerAddRequest) {
        Player existingPlayer = PlayerRepository.getInstance().getPlayerById(playerAddRequest.id);
        if (existingPlayer != null) {
            // If player exists return null
            return null;
        }
        Player createdPlayer = PlayerRepository.getInstance().addPlayer(new Player(playerAddRequest.id, playerAddRequest.port,playerAddRequest.address));
        String message = "Player has been created";

        return new PlayerAddResponse(createdPlayer, message);

    }

    public synchronized PlayerDeleteResponse deletePlayer(PlayerDeleteRequest request) {
        Player deletedPlayer = PlayerRepository.getInstance().deletePlayerById(request.id);

        if (deletedPlayer != null) {
            String message = "Player has been deleted";
            return new PlayerDeleteResponse(deletedPlayer, message);

        }
        return null;
    }

}