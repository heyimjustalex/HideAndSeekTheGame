package AdministrationClient;
import AdministrationClient.Utilities.HTTPUtilities;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws InterruptedException {
        String endpointUrl = "http://localhost:1337/";
        while (true){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Choose option: ");
            System.out.println("1. GET the list of the players currently in the game");
            System.out.println("2. GET The average of the last n heart rate values sent to the server by a given player");
            System.out.println("3. GET The average of the heart rate values sent by all the Players to the server that occurred between timestamp t1 and timestamp t2");
            System.out.println("4. SEND Custom text messages to all players");
            System.out.println("5. Start game");
            String option = scanner.nextLine();

            switch (option){
                case "1":
                    HTTPUtilities.httpGetPlayers(endpointUrl+"players");
                    break;

                case "2":
                    System.out.println("Give me playerId: ");
                    String playerId = scanner.nextLine();
                    System.out.println("Give me N: ");
                    String n = scanner.nextLine();
                    HTTPUtilities.httpGetNMeasurementsByPlayerId(endpointUrl+"players/measurements",playerId,n);
                    break;

                case "3":
                    System.out.println("Give me t1 in the format: yyyy:mm:dd:hh:mm:ss:sss ");
                    String t1 = scanner.nextLine();
                    System.out.println("Give me t2 in the format: yyyy:mm:dd:hh:mm:ss:sss ");
                    String t2 = scanner.nextLine();
                    HTTPUtilities.httpGetNMeasurementsByTimestamps(endpointUrl+"players/measurements",t1,t2);
                    break;

                case "4":
                    System.out.println("Specify message type");
                    String messageType = scanner.nextLine();
                    System.out.println("Specify message value");
                    String messageValue = scanner.nextLine();

                    try{
                        HTTPUtilities.broadcastMqttMessage(messageType,messageValue,false);
                    }
                    catch (Exception e){
                        System.out.println(e);
                    }
                    break;

                case "5":
                    try{
                        HTTPUtilities.broadcastMqttMessage("gameState","ELECTION_STARTED",true);
                    }
                    catch (Exception e){
                        System.out.println(e);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
