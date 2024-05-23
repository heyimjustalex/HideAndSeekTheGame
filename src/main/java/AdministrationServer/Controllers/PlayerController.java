package AdministrationServer.Controllers;
import AdministrationServer.Schemas.*;
import AdministrationServer.Services.MeasurementService;
import AdministrationServer.Services.PlayerService;
import com.sun.jersey.api.core.InjectParam;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.ws.ServiceMode;

@Path("players")
public class PlayerController {
    @InjectParam
    private PlayerService playerService;
    @InjectParam
    private MeasurementService measurementService;

    @GET
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response getPlayers() {
        PlayersGetResponse playersGetResponse = playerService.getPlayers();
            return Response.status(Response.Status.OK)
                    .entity(playersGetResponse)
                    .build();
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
    public Response addMeasurements(MeasurementAddRequest request) {
        MeasurementAddResponse measurementAddResponse = this.measurementService.addMeasurements(request);
        System.out.println("REQUEST "+ request);
        System.out.println("RESPONSE " + measurementAddResponse);
        return Response.ok(measurementAddResponse).status(Response.Status.CREATED).build();
    }

    @Path("measurements")
    @GET
    @Consumes({"application/json", "application/xml"})
    @Produces({"application/json", "application/xml"})
    public Response getMeasurements(@QueryParam("t1") String t1, @QueryParam("t2") String t2, @QueryParam("n") Integer n, @QueryParam("playerId") String playerId) {
        try {
            // Case 1: Retrieve by specifying timestamps t1 and t2
            if (t1 != null && t2 != null && n==null && playerId==null) {
                MeasurementGetResponse measurementGetResponse = this.measurementService.getMeasurementsByTimestamp(t1, t2);
                return Response.ok(measurementGetResponse).status(Response.Status.OK).build();
            }
            // Case 2: Retrieve by n and playerId
            else if (n != null && playerId != null && t1==null && t2==null) {
                MeasurementGetResponse measurementGetResponse = this.measurementService.getNMeasurementsByPlayerId(playerId, n);
                return Response.ok(measurementGetResponse).status(Response.Status.OK).build();
            }
            // Case 3: Retrieve all measurements
            else if (t1 == null && t2 == null && n == null && playerId == null) {
                MeasurementGetResponse measurementGetResponse = this.measurementService.getMeasurements();
                return Response.ok(measurementGetResponse).status(Response.Status.OK).build();
            }
            // If none of the valid cases are met, return bad request
            else {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Invalid query parameters!\"}").build();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Bad request format!\"}").build();
        }
    }



}
