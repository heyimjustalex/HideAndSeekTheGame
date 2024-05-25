package Game.Services;
import Game.Global.GlobalState;
import Game.GameClasses.PlayerExtended;
import io.grpc.stub.StreamObserver;
import proto.PlayerServiceGrpc.PlayerServiceImplBase;
import proto.Player.*;
public class PlayerServiceImpl extends PlayerServiceImplBase{

    @Override
    public void greeting(PlayerMessageRequest request, StreamObserver<PlayerMessageResponse> responseObserver){
        PlayerExtended playerExtended = new PlayerExtended(request.getId(),request.getPort(), request.getAddress(), request.getPosX(),request.getPosY(),request.getRole(),request.getPlayerState());
        GlobalState.getStateObject().addPlayer(playerExtended);
        responseObserver.onNext(PlayerMessageResponse.newBuilder().setResponseCode("200").build());
        responseObserver.onCompleted();

    }


}
