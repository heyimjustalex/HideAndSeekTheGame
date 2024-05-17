package AdministrationServer.Controllers;

import AdministrationServer.Schemas.PlayerAddRequest;
import AdministrationServer.Schemas.PlayerAddResponse;
import AdministrationServer.Schemas.PlayerDeleteRequest;
import AdministrationServer.Schemas.PlayerDeleteResponse;
import AdministrationServer.Services.PlayerService;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("players")
public class PlayerController {
    private PlayerService playerService;
    public PlayerController() {
        this.playerService = new PlayerService();
    }

    @Path("add")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response addPlayer(PlayerAddRequest request) {
        PlayerAddResponse playerAddResponse = playerService.addPlayer(request);
        System.out.println(playerAddResponse);
        if(playerAddResponse==null){
            // Adding unsuccessful returning null because user already exists
           return Response.status(Response.Status.CONFLICT)
                   .entity("{\"message\": \"Player with ID " + request.id + " already exists\"}")
                    .build();
        }

        return Response.ok(playerAddResponse).status(Response.Status.CREATED).build();
    }
    @Path("delete")
    @DELETE
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response deletePlayer(PlayerDeleteRequest request) {
        PlayerDeleteResponse  playerDeleteResponse = playerService.deletePlayer(request);
        if (playerDeleteResponse==null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Player with ID " + request.id + " not found\"}")
                    .build();
        }
        return Response.ok(playerDeleteResponse).status(Response.Status.NO_CONTENT).build();
    }


}
