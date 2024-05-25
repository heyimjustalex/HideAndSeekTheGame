package Game.Broker;

import Game.Global.GlobalState;
import Game.Models.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Timestamp;
import java.util.Scanner;

public class SubscriberHandler {
    public static void handleSubscription(GlobalState bufferGlobalState) throws InterruptedException, MqttException {
        String topic = "/broadcast";
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        int qos = 2;

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            //Connect the client
            System.out.println(clientId + " Subscriber: Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " Subscriber: Connected " + Thread.currentThread().getId());

            //Callback
            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) throws MqttException {

                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    String receivedMessage = new String(message.getPayload());
                    System.out.println(clientId +" Subscriber: Received a Message! - Callback - Thread PID: " + Thread.currentThread().getId() +
                            "\n\tTime:    " + time +
                            "\n\tTopic:   " + topic +
                            "\n\tMessage: " + receivedMessage +
                            "\n\tQoS:     " + message.getQos() + "\n");

                    if(topic.equals("/broadcast")){
                        Message message1= gson.fromJson(receivedMessage, Message.class);
                        bufferGlobalState.messageAdd(message1);
                        System.out.println(message1.getType()+" "+message1.getValue());
                    }
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Subscriber: Connection lost! cause:" + cause.getMessage() +  "-  Thread PID: " + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    if (token.isComplete()) {
                        System.out.println(clientId + " Subscriber: Message delivered - Thread PID: " + Thread.currentThread().getId());
                    }
                }

            });

            System.out.println(" Subscriber: "+clientId + " Subscribing ... - Thread PID: " + Thread.currentThread().getId());
            client.subscribe(topic,qos);
            System.out.println(" Subscriber: "+clientId + " Subscribed to topics : " + topic);


            client.disconnect();



        } catch (MqttException me ) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }


    }
}
