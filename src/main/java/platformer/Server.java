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

import platformer.code.gameengine.input.KeyboardInputManager;
import platformer.code.gameengine.loaders.LeveldataLoader;
import platformer.code.gamelogic.GameResources;
import platformer.code.gamelogic.LevelCompleteBar;
import platformer.code.gamelogic.Main;
import platformer.code.gamelogic.level.Level;
import platformer.code.gamelogic.level.LevelData;
import platformer.code.gamelogic.level.PlayerDieListener;
import platformer.code.gamelogic.level.PlayerWinListener;
import platformer.code.gamelogic.player.DummyPlayer;
import platformer.code.gamelogic.player.Player;


public class Server implements Runnable{
    public static final int PORT = 2017;

    private ServerSocket listener;
    private Socket connection;
    private static ArrayList<ConnectionHandler> handlers = new ArrayList<ConnectionHandler>();
    private boolean serverRunning;

    
    //list of packets received from players
    private static ArrayList<Packet> playerPositions = new ArrayList<Packet>();

    //update to give new id
    private static int nextPlayerId = 1;
    private static int itId = 1;

    // stops instant re-tagging
    private static long lastTagTime = 0;

    public Server() {
        serverRunning = false;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    //when client connect, add them to list of everybody, give them connectionHandler and spawn th
    @Override
    public void run() {
        try {
            listener = new ServerSocket(PORT);
            serverRunning = true;
            System.out.println("Server listening on port  " + PORT);

            while (serverRunning){
                connection = listener.accept();
                int assignedId = nextPlayerId;
                nextPlayerId++;

                //first one to connect is it
                boolean isIt = (assignedId == 1);
                Packet clientPacket = new Packet(assignedId, 400, 650, isIt);
                playerPositions.add(clientPacket);

                ConnectionHandler handler = new ConnectionHandler(connection, assignedId);
                handlers.add(handler);
                handler.start();
                System.out.println("Player " + assignedId + " connected.");
            }
        } catch (IOException e) {
                shutdown();
        }
    }
    
    //Will need to be client side I think
    // private static void checkTagCollisions(){
    //     ArrayList<DummyPlayer> players = level.getListOfPlayers();
    //     for (int i = 0; i < players.size(); i++) {
    //         if ((i != itId - 1) && (players.get(i).getHitbox().isIntersecting(players.get(itId - 1).getHitbox()))) {
    //             int previousItId = itId;
    //             itId = i + 1;
    //             lastTagTime = 0;
    //             for (Packet p: playerPositions) {
    //                 if (p.getPlayerId() == itId){
    //                     p.setIt(true);
    //                 }
    //                 else if (p.getPlayerId() == previousItId){
    //                     p.setIt(false);
    //                 }
    //             }             
    //         }
    //         //players.get(i).update(tslf);
    //     }
    // }
    
    private static void broadcastAllPlayers(Object broadcast) {
        for (ConnectionHandler h : handlers) {
            try {
                h.oos.writeObject(broadcast);
                h.oos.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("Unable to broadcast the Player " + h.assignedId + ".");
            }
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

    private static class ConnectionHandler extends Thread {
        private Socket client;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private int assignedId;

        ConnectionHandler(Socket socket, int assignedId) {
            client = socket;
            this.assignedId = assignedId;
            //Level.listOfPlayers.add(new Player(400 ,650 , level));
            try {
                oos = new ObjectOutputStream(client.getOutputStream());
                ois = new ObjectInputStream(client.getInputStream());
            }
            catch(Exception e){
            }
        }
 
        public void sendPackets() {
            for (Packet p: playerPositions) {
                try {
                    oos.writeObject(p);
                    //oos.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
 
        public void run() {
            String clientAddress = client.getInetAddress().toString();
            while(true) {
                try {
                    //Getting and unpacking client data into variables
                    Packet clientInfo = (Packet) ois.readObject();
                    System.out.println(clientInfo);
                    //find the player and update their packet
                    boolean wasIt = playerPositions.get(assignedId - 1).isIt();
                    clientInfo.setIt(wasIt);
                    playerPositions.set(assignedId - 1, clientInfo);
    
                    //we need to do server updating of game here checking for collisions and tag logic before broadcasting
                    //checkTagCollisions();
                    sendPackets();
                }
                catch(EOFException e){
                    System.out.println("the client disconnected, bye!!!");
                    shutdownConnection();
                }
                catch (Exception e){
                    System.out.println("Error on connection with: " + clientAddress + ": " + e);
                    shutdownConnection();
                }
            }

            
        }
        
        public void shutdownConnection() {
            try {
                //dont want people sitting there when they leave
                handlers.remove(this);
                playerPositions.remove(assignedId - 1);
                if (!client.isClosed()) {
                    oos.close();
                    ois.close();
                    client.close();
                }
            } catch (IOException e) {
                // TODO: handle exception
            }
        }
    }
}

