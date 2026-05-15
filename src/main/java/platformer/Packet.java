package platformer;
import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    private int playerId;
    private float x;
    private float y;
    private boolean isIt;
    private float movementVectorX;
    private float movementVectorY;

        
    public Packet(int playerId, float x, float y, boolean isIt){
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.isIt = isIt;
    }

    public Packet(int playerId, float x, float y, boolean isIt, float movementVectorX, float movementVectorY){
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.isIt = isIt;
        this.movementVectorX = movementVectorX;
        this.movementVectorY = movementVectorY;
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
    public float getMovementX() {
        return movementVectorX;
    }
    public float getMovementY() {
        return movementVectorY;
    }
    
    public boolean isIt(){
        return isIt;
    }
    
    public void setIt(boolean isIt){
        this.isIt = isIt;
    }
    
    public String toString(){
        return "PacketId: " + playerId + ", Player Location: (" + x + ", " + y + "), isIt: " + isIt + "Movement Vector (X): " + movementVectorX + "Movement Vector (Y): " + movementVectorY;
    }
}