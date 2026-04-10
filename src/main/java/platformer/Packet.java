package platformer;

import java.io.Serializable;

import platformer.code.gameengine.graphics.Camera;
import platformer.code.gamelogic.player.Player;

public class Packet implements Serializable {

    
    private String address;
    private Player theirPlayer;
    private Camera cam;
    private ArrayList<String> keys;
    
    
    public Packet() {

    }
}
