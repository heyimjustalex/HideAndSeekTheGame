package AdministrationServer.Controllers;

import AdministrationServer.Models.Measurement;
import AdministrationServer.Repositories.MeasurementRepository;
import AdministrationServer.Schemas.*;
import AdministrationServer.Services.MeasurementService;
import AdministrationServer.Services.PlayerService;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;

@Path("players")
public class PlayerController {
    private PlayerService playerService;
    private MeasurementService measurementService;
    public PlayerController() {
        this.playerService = new PlayerService();
        this.measurementService = new MeasurementService();
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
    public Response getMeasurements(@QueryParam("t1") String t1, @QueryParam("t2") String t2) {
        if (t1 == null || t2 == null) {
        MeasurementGetResponse measurementGetResponse = this.measurementService.getMeasurements();
        System.out.println(measurementGetResponse.getMeasurements());
        return Response.ok(measurementGetResponse).status(Response.Status.OK).build();
        }
        else {
            try{

                MeasurementGetResponse measurementGetResponse = this.measurementService.getMeasurementsByTimestamp(t1,t2);
                return Response.ok(measurementGetResponse).status(Response.Status.OK).build();
            }
            catch (Exception e)
            {
                System.out.println("BAD DATE FORMAT"+e.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Bad timestamp format!\"}").build();

            }


        }
    }





}
