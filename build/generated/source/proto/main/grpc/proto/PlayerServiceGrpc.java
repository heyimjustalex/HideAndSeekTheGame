package proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.25.0)",
    comments = "Source: Player.proto")
public final class PlayerServiceGrpc {

  private PlayerServiceGrpc() {}

  public static final String SERVICE_NAME = "proto.PlayerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<proto.Player.PlayerMessageRequest,
      proto.Player.PlayerMessageResponse> getGreetingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "greeting",
      requestType = proto.Player.PlayerMessageRequest.class,
      responseType = proto.Player.PlayerMessageResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Player.PlayerMessageRequest,
      proto.Player.PlayerMessageResponse> getGreetingMethod() {
    io.grpc.MethodDescriptor<proto.Player.PlayerMessageRequest, proto.Player.PlayerMessageResponse> getGreetingMethod;
    if ((getGreetingMethod = PlayerServiceGrpc.getGreetingMethod) == null) {
      synchronized (PlayerServiceGrpc.class) {
        if ((getGreetingMethod = PlayerServiceGrpc.getGreetingMethod) == null) {
          PlayerServiceGrpc.getGreetingMethod = getGreetingMethod =
              io.grpc.MethodDescriptor.<proto.Player.PlayerMessageRequest, proto.Player.PlayerMessageResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "greeting"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Player.PlayerMessageRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Player.PlayerMessageResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PlayerServiceMethodDescriptorSupplier("greeting"))
              .build();
        }
      }
    }
    return getGreetingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PlayerServiceStub newStub(io.grpc.Channel channel) {
    return new PlayerServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PlayerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new PlayerServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PlayerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new PlayerServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class PlayerServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void greeting(proto.Player.PlayerMessageRequest request,
        io.grpc.stub.StreamObserver<proto.Player.PlayerMessageResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGreetingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGreetingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Player.PlayerMessageRequest,
                proto.Player.PlayerMessageResponse>(
                  this, METHODID_GREETING)))
          .build();
    }
  }

  /**
   */
  public static final class PlayerServiceStub extends io.grpc.stub.AbstractStub<PlayerServiceStub> {
    private PlayerServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PlayerServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PlayerServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PlayerServiceStub(channel, callOptions);
    }

    /**
     */
    public void greeting(proto.Player.PlayerMessageRequest request,
        io.grpc.stub.StreamObserver<proto.Player.PlayerMessageResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGreetingMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class PlayerServiceBlockingStub extends io.grpc.stub.AbstractStub<PlayerServiceBlockingStub> {
    private PlayerServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PlayerServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PlayerServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PlayerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public proto.Player.PlayerMessageResponse greeting(proto.Player.PlayerMessageRequest request) {
      return blockingUnaryCall(
          getChannel(), getGreetingMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class PlayerServiceFutureStub extends io.grpc.stub.AbstractStub<PlayerServiceFutureStub> {
    private PlayerServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private PlayerServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PlayerServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new PlayerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Player.PlayerMessageResponse> greeting(
        proto.Player.PlayerMessageRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGreetingMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GREETING = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final PlayerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(PlayerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GREETING:
          serviceImpl.greeting((proto.Player.PlayerMessageRequest) request,
              (io.grpc.stub.StreamObserver<proto.Player.PlayerMessageResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class PlayerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PlayerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return proto.Player.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PlayerService");
    }
  }

  private static final class PlayerServiceFileDescriptorSupplier
      extends PlayerServiceBaseDescriptorSupplier {
    PlayerServiceFileDescriptorSupplier() {}
  }

  private static final class PlayerServiceMethodDescriptorSupplier
      extends PlayerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    PlayerServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PlayerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PlayerServiceFileDescriptorSupplier())
              .addMethod(getGreetingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
