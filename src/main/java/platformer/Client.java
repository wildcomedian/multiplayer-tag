package platformer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

import platformer.code.gamelogic.Main;
import platformer.code.gamelogic.player.DummyPlayer;
import platformer.code.gamelogic.player.Player;

public class Client extends Main {
    private static int id = 0;
    Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean connected;

    public static void main(String[] args) {
        Client client = new Client();
        System.out.println("Running Client");
        client.start("TAG",SCREEN_WIDTH,SCREEN_HEIGHT);
        System.out.println("Created Window");
    }

    public Client() {
        id++;

        try {
            String host = "localhost";
            socket = new Socket(host, Server.PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            PacketInputHandler inputHandler = new PacketInputHandler();
            inputHandler.start();
            connected = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private class PacketInputHandler extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (connected) {
                try {
                    Packet serverPacket = (Packet) ois.readObject();
                    // Do Stuff with serverPacket figure out
                    int packetId = serverPacket.getPlayerId();
                    float newX = serverPacket.getX();
                    float newY = serverPacket.getY();
                    if (currentLevel != null && (currentLevel.getListOfPlayers() != null) && (packetId > currentLevel.getListOfPlayers().size() + 1)) {
                        currentLevel.getListOfPlayers().add(new DummyPlayer(newX + 200, newY + 200, currentLevel));
                    }
                    else if (currentLevel != null && packetId < currentLevel.getListOfPlayers().size() + 1){
                        
                        for (DummyPlayer dp: currentLevel.getListOfPlayers()) {
                            if (dp.getId() == packetId) {
                                dp.setPosition(newX, newY);
                            }
                        }
                        serverPacket.isIt();
                    }
                    
    
                    sendPacket(serverPacket);
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
