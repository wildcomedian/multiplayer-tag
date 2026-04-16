package platformer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import platformer.code.gamelogic.Main;
import platformer.code.gamelogic.level.Level;

public class Server implements Runnable{
    public static final int PORT = 2017;

    private ServerSocket listener;
    private Socket connection;
    private static ArrayList<ConnectionHandler> handlers = new ArrayList<ConnectionHandler>();
    private static ArrayBlockingQueue<Packet> messageQueue = new ArrayBlockingQueue<>(100);
    private boolean serverRunning;

    private Level level;
    private Main game;

    public Server() {
        serverRunning = false;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    @Override
    public void run() {
        try {
            listener = new ServerSocket(PORT);
            serverRunning = true;
            while (serverRunning) {
                connection = listener.accept(); // Listens for client to connect to the Server
                ConnectionHandler handler = new ConnectionHandler(connection);
                handlers.add(handler);
            }
        } catch (IOException e) {
            // TODO: Make a way to deal with when a connection fails
            shutdown();
        }
    }

    private void broadcastAllPlayers(Packet gameState) {
        for (ConnectionHandler handler: handlers) {
            handler.sendPacket(gameState);
        }
    }

    private void shutdown() {
        serverRunning = false;
        try {
            if (!listener.isClosed())
                listener.close();
        } catch (IOException e) {
            // TODO: handle exception
        }
    }

    public Level getlLevel() {
        return level;
    }

    private static class ConnectionHandler extends Thread {
        private Socket client;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        ConnectionHandler(Socket socket) {
            client = socket;
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                ois = new ObjectInputStream(client.getInputStream());
            }
            catch(Exception e){
            }
        }
 
        public void sendPacket(Packet gameState) {
            try {
                oos.writeObject(gameState);
                oos.flush();
            } catch (Exception e) {
                System.out.println("Error sending to client: " + e);
            }
        }
 
        public void run() {
            String clientAddress = client.getInetAddress().toString();
            while(true) {
                try {
                    Packet clientInfo = (Packet) ois.readObject();

                    //Process clientInfo and update the game accordingly


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


