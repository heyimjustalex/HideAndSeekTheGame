package AdministrationServer.Services;

import AdministrationServer.Models.Player;
import AdministrationServer.Repositories.PlayerRepository;
import AdministrationServer.Schemas.PlayerAddRequest;
import AdministrationServer.Schemas.PlayerAddResponse;
import AdministrationServer.Schemas.PlayerDeleteRequest;
import AdministrationServer.Schemas.PlayerDeleteResponse;

import java.util.List;
import java.util.Random;


public class PlayerService {
    enum BAR {
        TOP_BAR,
        LEFT_BAR,
        RIGHT_BAR,
        BOTTOM_BAR
    }
    public Integer[] getRandomCoordinates() {
        BAR bar = getRandomBar();

        switch (bar) {
            case TOP_BAR:
                return getTopBarCoordinates();
            case LEFT_BAR:
                return getLeftBarCoordinates();
            case RIGHT_BAR:
                return getRightBarCoordinates();
            case BOTTOM_BAR:
                return getBottomBarCoordinates();
            default:
                throw new IllegalStateException("Unexpected value: " + bar);
            }
        }

        private BAR getRandomBar() {
            Random random = new Random();
            int index = random.nextInt(BAR.values().length);
            return BAR.values()[index];
        }

        private Integer[] getTopBarCoordinates() {
            Random random = new Random();
            Integer pos_x = random.nextInt(9);
            Integer pos_y = 9;
            Integer[] coordinates = new Integer[2];
            coordinates[0] = pos_x;
            coordinates[1] = pos_y;

            return coordinates;
        }

        private Integer[] getLeftBarCoordinates() {
            Random random = new Random();
            Integer pos_x = 0;
            Integer pos_y = random.nextInt(9);
            Integer[] coordinates = new Integer[2];
            coordinates[0] = pos_x;
            coordinates[1] = pos_y;
            return coordinates;
        }

        private Integer[] getRightBarCoordinates() {
            Random random = new Random();
            Integer pos_x = 9;
            Integer pos_y = random.nextInt(9);
            Integer[] coordinates = new Integer[2];
            coordinates[0] = pos_x;
            coordinates[1] = pos_y;
            return coordinates;
        }

        private Integer[] getBottomBarCoordinates() {
            Random random = new Random();
            Integer pos_x = random.nextInt(9);
            Integer pos_y = 0;
            Integer[] coordinates = new Integer[2];
            coordinates[0] = pos_x;
            coordinates[1] = pos_y;
            return coordinates;
        }

        private boolean checkIfOtherPlayersHaveSameCoordinates(Integer pos_x, Integer pos_y){

            List<Player> players = PlayerRepository.getInstance().getPlayers();
            Boolean areCoordinatesTheSame = false;
            for (Player player:players){
                Integer other_pos_x = player.getPos_x();
                Integer other_pos_y = player.getPos_y();

                if(pos_x.equals(other_pos_x) && pos_y.equals(other_pos_y)){
                    areCoordinatesTheSame = true;
                    break;
                }
            }
            return areCoordinatesTheSame;

        }

    public synchronized PlayerAddResponse addPlayer(PlayerAddRequest playerAddRequest) {
        Player existingPlayer = PlayerRepository.getInstance().getPlayerById(playerAddRequest.id.toLowerCase());
        if (existingPlayer != null) {
            return null;
        }
        Boolean hasTheSameCoordinatesAsOtherPlayer = true;
        Integer[] coordinates = new Integer[2];

        while (hasTheSameCoordinatesAsOtherPlayer){
            coordinates = getRandomCoordinates();
            hasTheSameCoordinatesAsOtherPlayer = checkIfOtherPlayersHaveSameCoordinates(coordinates[0], coordinates[1]);
        }

        PlayerRepository.getInstance().addPlayer(new Player(playerAddRequest.id.toLowerCase(), playerAddRequest.port,playerAddRequest.address,coordinates[0],coordinates[1] ));
        List<Player> players = PlayerRepository.getInstance().getPlayers();
        String message = "Player has been created";

        return new PlayerAddResponse(players, message);

    }

    public synchronized PlayerDeleteResponse deletePlayer(PlayerDeleteRequest request) {
        Player deletedPlayer = PlayerRepository.getInstance().deletePlayerById(request.id.toLowerCase());

        if (deletedPlayer != null) {
            String message = "Player has been deleted";
            return new PlayerDeleteResponse(deletedPlayer, message);

        }
        return null;
    }

}