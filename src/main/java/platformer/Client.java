package platformer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import platformer.code.gamelogic.player.Player;

public class Client implements Runnable{
    int id;
    Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean connected;
    Player player;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public Client() {
        try {
            InetAddress host = InetAddress.getLocalHost();
            socket = new Socket(host, 9876);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            connected = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (connected) {

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
        return new Packet(this.id, this.player.getX(), this.player.getY(), false);
    }


}
