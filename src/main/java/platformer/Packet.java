package platformer;

import java.io.Serializable;

import platformer.code.gameengine.graphics.Camera;
import platformer.code.gamelogic.player.Player;

public class Packet implements Serializable {
    
    private int playerId;
    private float x;
    private float y;
    private boolean isIt;
    
    public Packet(int playerId, float x, float y, boolean isIt){
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.isIt = isIt;
    }
    
    public int getPlayerId(){
        return playerId;
    }
    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }
    public boolean isIt(){
        return isIt;
    }
    
    public void setIt(boolean isIt){
        this.isIt = isIt;
    }
    
}