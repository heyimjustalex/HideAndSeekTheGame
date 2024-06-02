package Game.Broker;

import Game.Global.GlobalState;
import Game.Models.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Timestamp;

public class SubscriberHandler {
    public static void handleSubscription() throws InterruptedException, MqttException {
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
            System.out.println(clientId + "SubscriberHandler: Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + "SubscriberHandler: Connected " + Thread.currentThread().getId());

            //Callback
            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) throws MqttException {

                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    String receivedMessage = new String(message.getPayload());
                    System.out.println(clientId + " SubscriberHandler: Received a Message! - Callback - Thread PID: " + Thread.currentThread().getId() +
                            "\n\tTime:    " + time +
                            "\n\tTopic:   " + topic +
                            "\n\tMessage: " + receivedMessage +
                            "\n\tQoS:     " + message.getQos() + "\n");

                    if (topic.equals("/broadcast")) {
                        Message retreviedMessage = gson.fromJson(receivedMessage, Message.class);
                        GlobalState.getStateObject().messageAdd(retreviedMessage);
//                        System.out.println("SubscriberHandler: " + retreviedMessage.getType() + " " + retreviedMessage.getValue());
                    }
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + "SubscriberHandler: Subscriber: Connection lost! cause:" + cause.getMessage() + "-  Thread PID: " + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    if (token.isComplete()) {
                        System.out.println("SubscriberHandler: " + clientId + " Subscriber: Message delivered - Thread PID: " + Thread.currentThread().getId());
                    }
                }

            });

            System.out.println("SubscriberHandler: Subscriber: " + clientId + " Subscribing ... - Thread PID: " + Thread.currentThread().getId());
            client.subscribe(topic, qos);
            System.out.println("SubscriberHandler: Subscriber: " + clientId + " Subscribed to topics : " + topic);
//            client.disconnect();


        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }


    }
}
