package AdministrationServer.Repositories;
import AdministrationServer.Models.Player;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)

public class PlayerRepository {
    @XmlElement(name="players")
    private List<Player> players;
    private static PlayerRepository instance;

    private PlayerRepository(){
        this.players = new ArrayList<>();
    }
    public synchronized static PlayerRepository getInstance(){
        if(instance==null)
            instance = new PlayerRepository();
        return instance;
    }
    public synchronized List<Player> getPlayers(){
        return new ArrayList<>(this.players);
    }
    public synchronized Player addPlayer(Player player){
        this.players.add(player);
        return player;
    }

    public Player getPlayerById(String id){
        List<Player> playersCopy = getPlayers();

        for(Player player: playersCopy)
            if(player.getId().equalsIgnoreCase(id))
                return player;
        return null;
    }
    public synchronized Player deletePlayerById(String id){
        for(Player player: this.players)
            if(player.getId().equalsIgnoreCase(id))
            {
                this.players.remove(player);
                return player;
            }
        return null;
    }


}
