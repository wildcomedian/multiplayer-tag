package platformer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import platformer.code.gamelogic.Main;
import java.util.logging.Level;
import platformer.code.gamelogic.player.DummyPlayer;
import platformer.code.gamelogic.player.Player;

import java.util.ArrayList;

public class Client extends Main {
    private int myId;
    Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    public boolean connected;

    public static void main(String[] args) {
        Client client = new Client();
        System.out.println("Running Client");
        client.start("TAG" + client.getId(), SCREEN_WIDTH, SCREEN_HEIGHT);
        System.out.println("Created Window");
    }

    public Client() {
        
        try {
            //Setting up connection to server
            String host = "localhost";
            socket = new Socket(host, Server.PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            myId = ois.readInt();
            System.out.println("Assigned myId: " + myId);

            //Creating and starting PacketInputHandler to read packets from the connected server
            PacketInputHandler inputHandler = new PacketInputHandler();
            inputHandler.start();
            //Creating a thread to constantly just send the client's packet (player status)
            Thread clientMessengerThread = new Thread() {
                private float prevX = 0;
                private float prevY = 0;
                @Override
                public void run() {
                    while (connected) {
                        if ((currentLevel != null) && ((currentLevel.getPlayer().getX() != prevX) || (currentLevel.getPlayer().getY() != prevY))) {
                            prevX = currentLevel.getPlayer().getX();
                            prevY = currentLevel.getPlayer().getY();
                            sendPacket(makePacket());
                        }
                    }
                    System.out.println("No longer connected");
                }
            };
            clientMessengerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
        }
    }

    private class PacketInputHandler extends Thread{
        @Override
        public void run() {
            while (connected) {
                try {
                    ArrayList<Packet> serverPackets = (ArrayList<Packet>) ois.readObject();
                    updateDummyPlayers(serverPackets);
                    updateItStatus(serverPackets);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }     
            }
        }
    }

    private void sendPacket(Packet p) {
        try {
            oos.writeObject(p);
            oos.flush();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void updateItStatus(ArrayList<Packet> serverPackets) {
        Packet myPacket = serverPackets.get(myId - 1);
        
        boolean itStatus = myPacket.isIt();
        if (currentLevel != null) {
            currentLevel.getPlayer().isIt = itStatus;
            currentLevel.getListOfPlayers().get(myId - 1).tagMode = itStatus;
        }
    }

    public void updateDummyPlayers(ArrayList<Packet> playerPackets) {
        for (Packet playerPacket: playerPackets) {
            int packetId = playerPacket.getPlayerId();
            if (currentLevel != null) {

                int correspondingDummyIndex = packetId - 1;
                if ((playerPackets.size() > currentLevel.getListOfPlayers().size())) {
                    currentLevel.addDummyPlayer();
                }
                
                float newX = playerPacket.getX();
                float newY = playerPacket.getY();
                float newVectorX = playerPacket.getMovementX();
                float newVectorY = playerPacket.getMovementY();
                currentLevel.getListOfPlayers().get(correspondingDummyIndex).setPosition(newX, newY, newVectorX, newVectorY);
                
                if (playerPacket.isIt() && currentLevel.gettingTagged(packetId)) {
                    currentLevel.getPlayer().isIt = true;
                    currentLevel.getListOfPlayers().get(myId - 1).tagMode = true;
                }
                if (playerPacket.isIt()) {
                    currentLevel.getListOfPlayers().get(correspondingDummyIndex).tagMode = true;
                }
                else {
                    currentLevel.getListOfPlayers().get(correspondingDummyIndex).tagMode = false;
                }
            }
        }
    }

    private Packet makePacket() {
        return new Packet(this.myId, currentLevel.getPlayer().getX(), currentLevel.getPlayer().getY(), currentLevel.getPlayer().isIt(), currentLevel.getPlayer().getMovementX(), currentLevel.getPlayer().getMovementY());
    }

    public int getId() {
        return myId;
    }

}
