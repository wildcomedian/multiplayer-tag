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
    private static int id = 0;
    Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    public boolean connected;

    public static void main(String[] args) {
        Client client = new Client();
        System.out.println("Running Client");
        client.start("TAG",SCREEN_WIDTH,SCREEN_HEIGHT);
        System.out.println("Created Window");
    }

    public Client() {
        id++;

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
                    Packet serverPacket = serverPackets.get(id - 1);
                    // System.out.println(serverPacket);
                    // Do Stuff with serverPacket figure out
                    int packetId = serverPacket.getPlayerId();
                    float newX = serverPacket.getX();
                    float newY = serverPacket.getY();
                  
                    currentLevel.getListOfPlayers().get(packetId-1).setPosition(newX, newY);
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

    private Packet makePacket() {
        return new Packet(this.id, currentLevel.getPlayer().getX(), currentLevel.getPlayer().getY(), false);
    }

}
