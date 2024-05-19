package AdministrationServer.Controllers;

import AdministrationServer.Models.Measurement;
import AdministrationServer.Schemas.*;
import AdministrationServer.Services.PlayerService;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

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
    @Path("measurements")
    @POST
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response addMeasurement(MeasurementAddRequest request) {
        List<Measurement> measurements = request.getMeasurements();
        System.out.println(measurements);
        System.out.println(measurements.get(0).getValue());

        return Response.ok().status(Response.Status.NO_CONTENT).build();
    }

}
