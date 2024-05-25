package Game.Buffer;

import Game.Models.Message;
import Game.GameClasses.PlayerExtended;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    String state;
    List<Message> mqttMessagesSent;
    List<PlayerExtended> players;
    private static GameState instance;
    private GameState(){
        // Available GAME states
        // BEFORE_ELECTION
        // ELECTION_STARTED
        // ELECTION_ENDED
        // GAME_ENDED
        mqttMessagesSent = new ArrayList<>();
        players=new ArrayList<>();

    }

    synchronized static public GameState getStateObject(){
        if(instance==null)
            instance = new GameState();
        return instance;
    }
    public synchronized List<Message> getMqttMessagesSent() {
        return mqttMessagesSent;
    }

    public synchronized String waitUntilElectionStarts() throws InterruptedException {
        while (this.state.equals("BEFORE_ELECTION")){
            wait();
        }
        System.out.println("BufferGameState: Changed game state to true");
        return state;
    }

    public synchronized void messageAdd(Message message){
        System.out.println("BufferGameState: "+" consumed message " + message.getValue());
        if(message.getType().equals("gameState") && message.getValue().equals("ELECTION_STARTED")){
               state="ELECTION_STARTED";
        }
        mqttMessagesSent.add(message);
        notifyAll();
    }



    
    
    
    
}
