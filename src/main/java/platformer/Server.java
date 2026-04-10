package platformer;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import platformer.code.gamelogic.Main;
import platformer.code.gamelogic.level.Level;

public class Server {
    private ServerSocket listener;
    private Socket connection;
    private static ArrayList<ConnectionHandler> handlers = new ArrayList<ConnectionHandler>();
    private static ArrayBlockingQueue<Packet> messageQueue = new ArrayBlockingQueue<>(100);
 

    private Level level;
    private Main game;

    public static void main(String[] args) {
        Server server = new Server();
    }

    private static class ConnectionHandler extends Thread {
        Socket client;
        ObjectOutputStream oos; //you'll need to define this one for when you're ready to talk back to the client!
        ObjectInputStream ois;
        ConnectionHandler(Socket socket) {
            client = socket;
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                ois = new ObjectInputStream(client.getInputStream());
            }
            catch(Exception e){}
        }
 
        public void sendMessage(String message) {
            try {
                oos.writeObject(message);
                oos.flush();
            } catch (Exception e) {
                System.out.println("Error sending to client: " + e);
            }
        }
 
        public void run() {
            String clientAddress = client.getInetAddress().toString();
            while(true) {
                try {
                    Packet gameState = (Packet) ois.readObject();
                    // if(!message.equals("disconnect")){
                    //     System.out.println(message);
                    //     messageQueue.put(Packet);
                    // }
                    // else{
                    //     System.out.println("closing connection");
                    //     break;
                    // }
                }
                catch(EOFException e){
                    System.out.println("the client disconnected, bye!!!");
                    break;
                }
                catch (Exception e){
                    System.out.println("Error on connection with: "
                            + clientAddress + ": " + e);
                }
            }
            synchronized (handlers) {
                handlers.remove(this);
            }
            try { client.close(); } catch (Exception e) {}
        }
    }
    
}


