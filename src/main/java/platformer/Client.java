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
    private static int numOfPlayers = 0;
    private int id;
    Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    public boolean connected;

    public static void main(String[] args) {
        Client client = new Client();
        System.out.println("Running Client");
        Thread clientStarter1 = new Thread() {
            @Override
            public void run() {
                client.start("TAG" + client.getId(), SCREEN_WIDTH, SCREEN_HEIGHT);
            }
        };

        Client client2 = new Client();
        System.out.println("Running Client");
        Thread clientStarter2 = new Thread() {

            @Override
            public void run() {
                client2.start("TAG" + client2.getId(), SCREEN_WIDTH, SCREEN_HEIGHT);
            }
        };

        clientStarter1.start();
        clientStarter2.start();
        System.out.println("Created Window");
    }

    public Client() {
        numOfPlayers++;
        id = numOfPlayers;
        System.out.println("Assigned Id" + id);
        try {
            //Setting up connection to server
            String host = "localhost";
            socket = new Socket(host, Server.PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
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
                    if (currentLevel != null && currentLevel.getPlayer().isIt() == true) {
                        currentLevel.getPlayer().isIt = currentLevel.isPlayerTagging();
                    }
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

    public void updateDummyPlayers(ArrayList<Packet> playerPackets) {
        for (Packet playerPacket: playerPackets) {
            int packetId = playerPacket.getPlayerId();
            if (currentLevel != null) {
                int correspondingDummyIndex = packetId - 1;
                if ((correspondingDummyIndex >= currentLevel.getListOfPlayers().size())) {
                    currentLevel.addDummyPlayer();
                }
                float newX = playerPacket.getX();
                float newY = playerPacket.getY();
                float newVectorX = playerPacket.getMovementX();
                float newVectorY = playerPacket.getMovementY();
                currentLevel.getListOfPlayers().get(correspondingDummyIndex).setPosition(newX, newY, newVectorX, newVectorY);
            }
        }
    }

    private Packet makePacket() {
        return new Packet(this.id, currentLevel.getPlayer().getX(), currentLevel.getPlayer().getY(), currentLevel.getPlayer().isIt(), currentLevel.getPlayer().getMovementX(), currentLevel.getPlayer().getMovementY());
    }

    public int getId() {
        return id;
    }

}
