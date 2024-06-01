package Game.Services.GrpcCalls;

import Game.GameClasses.PlayerExtended;
import Game.Global.GlobalState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.Player;
import proto.PlayerServiceGrpc;

public class GrpcCalls {
    public static void greetingCallAsync(String serverAddress) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);
        PlayerExtended myPlayer = GlobalState.getStateObject().getMyPlayer();

        Player.PlayerMessageRequest request = Player.PlayerMessageRequest
                .newBuilder()
                .setId(myPlayer.getId())
                .setPort(myPlayer.getPort().toString())
                .setAddress(myPlayer.getAddress())
                .setPosX(myPlayer.getPos_x().toString())
                .setPosY(myPlayer.getPos_y().toString())
                .setRole(myPlayer.getRole().name())
                .setPlayerState(myPlayer.getPlayerState().name())
                .setGameState(GlobalState.getStateObject().getGameState().name())
                .build();
        stub.election(request, new StreamObserver<Player.PlayerMessageResponse>() {
            @Override
            public void onNext(Player.PlayerMessageResponse res) {
                System.out.println("greetingCallAsync " + res);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t.getMessage());
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
            }
        });
    }
}
