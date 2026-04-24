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
public class Server implements Runnable, PlayerDieListener, PlayerWinListener{
    public static final int PORT = 2017;

    private ServerSocket listener;
    private Socket connection;
    private static ArrayList<ConnectionHandler> handlers = new ArrayList<ConnectionHandler>();
    //private static ArrayBlockingQueue<Packet> messageQueue = new ArrayBlockingQueue<>(100);
    private boolean serverRunning;

    
    //list of packets received from players
    private static ArrayList<Packet> playerPositions = new ArrayList<Packet>();

    //update to give new id
    private static int nextPlayerId = 1;
    private static int itId = 1;

    // stops instant re-tagging
    private static long lastTagTime = 0;

    private static Level level;
    private Main game;

    private LevelData[] levelDataArray;
    private long levelStartTime;
    private long levelFinishTime;
    private int currentLevelIndex;
    


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
                Packet firstPacket = new Packet(assignedId, 400, 650, isIt);
                synchronized (playerPositions){
                    playerPositions.add(firstPacket);
                }

                ConnectionHandler handler = new ConnectionHandler(connection, assignedId);
                synchronized(handlers) {
                    handlers.add(handler);
                }
                handler.start();
                System.out.println("Player " + assignedId + " connected.");
            }
        } catch (IOException e) {
                shutdown();
        }
    }
                
            
    public void init() {
        GameResources.load();

        currentLevelIndex = 0;

        levelDataArray = new LevelData[2]; //Instantiates an array that holds two levelDataArray (data)
        try {
            //levelDataArray[0] = LeveldataLoader.loadLeveldata("src/main/java/platformer/maps/map1.txt");
            levelDataArray[0] = LeveldataLoader.loadLeveldata("src/main/java/platformer/maps/tagYourIt.txt"); //Our data
            levelDataArray[1] = LeveldataLoader.loadLeveldata("src/main/java/platformer/maps/map1.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        level = new Level(levelDataArray[currentLevelIndex]); //Makes the current level the one from the 

        level.addPlayerDieListener(this);
        level.addPlayerWinListener(this);

        //screenTransition.addScreenTransitionListener(this);
        
        //numberOfTries = 0;
        levelStartTime = System.currentTimeMillis();
        
        //levelCompleteBar = new LevelCompleteBar(100, 10, SCREEN_WIDTH - 200, 10, level.getPlayer());
    }

    public void update(float tslf) {
        //if(KeyboardInputManager.isKeyDown(KeyEvent.VK_N)) init();
        //if(KeyboardInputManager.isKeyDown(KeyEvent.VK_ESCAPE)) System.exit(0);

        if (serverRunning) level.update(tslf);

        //screenTransition.update(tslf);
        
        //levelCompleteBar.update(tslf);
    }

    
    //Will need to be client side I think
    private static void checkTagCollisions(){
        ArrayList<DummyPlayer> players = level.getListOfPlayers();
        for (int i = 0; i < players.size(); i++) {
            if ((i != itId - 1) && (players.get(i).getHitbox().isIntersecting(players.get(itId - 1).getHitbox()))) {
                int previousItId = itId;
                itId = i + 1;
                lastTagTime = 0;
                for (Packet p: playerPositions) {
                    if (p.getPlayerId() == itId){
                        p.setIt(true);
                    }
                    else if (p.getPlayerId() == previousItId){
                        p.setIt(false);
                    }
                }
                
                
            }
            //players.get(i).update(tslf);
        }
    }
    
    private static void broadcastAllPlayers() {
        synchronized (playerPositions){
            synchronized(handlers) {
                for (Packet p : playerPositions){
                    for (ConnectionHandler h : handlers){
                        h.sendPacket(p);
                    }
                }
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

    public Level getlLevel() {
        return level;
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
                oos.flush();
                ois = new ObjectInputStream(client.getInputStream());
                oos.writeObject(new Packet(assignedId, 0, 0, false));
                oos.flush();
            }
            catch(Exception e){
            }
        }
 
        public void sendPacket(Packet gameState) {
            try {
                oos.writeObject(gameState);
                //makes sure sure fresh data gets used
                oos.reset();
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

                    //find the player and update their packet
                    synchronized (playerPositions) {
                        boolean found = false;
                        for (int i = 0; i < playerPositions.size(); i++) {
                            if (playerPositions.get(i).getPlayerId() == clientInfo.getPlayerId()) {
                                boolean wasIt = playerPositions.get(i).isIt();
                                clientInfo.setIt(wasIt);
                                playerPositions.set(i, clientInfo);
                                found = true;
                                break;
                            }
                        }
                        if (!found) playerPositions.add(clientInfo);
                    }


                    //we need to do server updating of game here checking for collisions and tag logic before broadcasting
                    checkTagCollisions();
                    broadcastAllPlayers();

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

            //dont want people sitting there when they leave
            synchronized (playerPositions){
                for (int i = 0; i < playerPositions.size(); i++){
                    if (playerPositions.get(i).getPlayerId() == assignedId) {
                        playerPositions.remove(i);
                        break;
                    }
                }
            }
            try { client.close(); } catch (Exception e) {}
        }
    }

    @Override
    public void onPlayerWin() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onPlayerWin'");
    }

    @Override
    public void onPlayerDeath() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onPlayerDeath'");
    }
    
}

