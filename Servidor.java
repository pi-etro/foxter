import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import Mensagem.*;

//@SuppressWarnings("resource") // removes never closed warnings

public class Servidor {

    public static String serverAddress;
    public static int serverPort = 10098; // default server port

    public static ConcurrentHashMap<String, ArrayList<String>> peerFiles = new ConcurrentHashMap<>(); // peers and files
    public static ConcurrentHashMap<String, Boolean> peerAlive = new ConcurrentHashMap<>(); // alive responses

    public static void main(String args[]) throws Exception {

        // address input
        Scanner input = new Scanner(System.in);
        System.out.print("Entre com o endereco IP do servidor: ");
        Servidor.serverAddress = input.nextLine();
        input.close();

        DatagramSocket serverSocket = new DatagramSocket(serverPort);

        peerWatchdog wd = new peerWatchdog(serverSocket);
        wd.start();

        // waits peers UDP contact
        while (true) {
            try {
                byte[] recBuffer = new byte[1024];
                DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
                serverSocket.receive(recPacket); // blocking

                // initialize a thread for this request
                ClientHandler peer = new ClientHandler(recPacket, serverSocket);
                peer.start();
            } catch (Exception e) {

            }
        }
    }

    // serialize and send Mensagem object
    public static void send(Mensagem m, DatagramSocket s, InetAddress a, int p) {
        try {
            // serialize Mensagem object
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
            ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            objectOut.flush();
            objectOut.writeObject(m);
            objectOut.flush();
            byte[] sendData = byteStream.toByteArray();

            // send packet
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, a, p);
            s.send(sendPacket);
            objectOut.close();
        } catch (Exception e) {

        }
    }

    public static void printFiles(ArrayList<String> files) {
        for (String file : files) {
            System.out.print(" " + file);
        }
        System.out.print("\n");
    }
}

class ClientHandler extends Thread {

    public Mensagem requisicao;
    public DatagramSocket serverSocket;

    public ClientHandler(DatagramPacket recPacket, DatagramSocket serverSocket) {
        try {
            // deserialize Mensagem object
            ByteArrayInputStream byteStream = new ByteArrayInputStream(recPacket.getData());
            ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(byteStream));
            Mensagem requisicao = (Mensagem) objectIn.readObject();

            // get IP and port from peer
            requisicao.setAddress(recPacket.getAddress());
            requisicao.setPort(recPacket.getPort());

            this.requisicao = requisicao;
            this.serverSocket = serverSocket;
        } catch (Exception e) {

        }
    }

    public void run() {

        String req = this.requisicao.getMessage();
        String ip_porta = this.requisicao.getAddress().toString().replace("/", "") + ":"
                + String.valueOf(this.requisicao.getPort());
        Mensagem resposta = new Mensagem();

        if (req.equals("ALIVE_OK")) { // ALIVE
            if (Servidor.peerAlive.containsKey(ip_porta)) {
                Servidor.peerAlive.replace(ip_porta, true);
            }
        } else if (req.equals("JOIN")) { // JOIN_OK
            if (!Servidor.peerFiles.containsKey(ip_porta)) {
                // adds peer ip:port and file list to table
                Servidor.peerFiles.put(ip_porta, this.requisicao.getList());

                // adds peer to alive time table
                Servidor.peerAlive.put(ip_porta, true);
            }

            // send confirmation
            resposta.setMessage("JOIN_OK");
            resposta.setAddress(this.requisicao.getAddress());
            resposta.setPort(this.requisicao.getPort());
            Servidor.send(resposta, this.serverSocket, resposta.getAddress(), resposta.getPort());

            System.out.print("Peer " + ip_porta + " adicionado com arquivos");
            Servidor.printFiles(this.requisicao.getList());
        } else if (req.equals("LEAVE")) { // LEAVE_OK
            if (Servidor.peerFiles.containsKey(ip_porta)) {
                // remove peer data from table
                Servidor.peerFiles.remove(ip_porta);
                Servidor.peerAlive.remove(ip_porta);
            }

            // send confirmation
            resposta.setMessage("LEAVE_OK");
            Servidor.send(resposta, this.serverSocket, this.requisicao.getAddress(), this.requisicao.getPort());
        } else if (req.equals("SEARCH")) { // SEARCH_OK

            ArrayList<String> peerList = new ArrayList<String>();

            if (Servidor.peerFiles.containsKey(ip_porta)) {

                System.out.println("Peer " + ip_porta + " solicitou arquivo " + requisicao.getList().get(0));

                // search for files in each peer
                Set<String> peers = Servidor.peerFiles.keySet();
                for (String peer : peers) {
                    if (!peer.equals(ip_porta)
                            && (Servidor.peerFiles.get(peer)).contains(requisicao.getList().get(0))) {
                        peerList.add(peer);
                    }
                }
            } else {
                peerList = new ArrayList<String>(); // only sends list of peers if peer already made join
            }

            // send list of peers with file
            resposta.setMessage("SEARCH_OK");
            resposta.setList(peerList);
            Servidor.send(resposta, this.serverSocket, this.requisicao.getAddress(), this.requisicao.getPort());
        } else if (req.equals("UPDATE")) { // UPDATE_OK
            if (Servidor.peerFiles.containsKey(ip_porta)) {

                if (!Servidor.peerFiles.get(ip_porta).contains(requisicao.getList().get(0))) {
                    // add a new file to peer file list
                    ArrayList<String> updatedList = Servidor.peerFiles.get(ip_porta);
                    updatedList.add(requisicao.getList().get(0));
                    Servidor.peerFiles.replace(ip_porta, updatedList);
                }
            }

            // send confirmation
            resposta.setMessage("UPDATE_OK");
            Servidor.send(resposta, this.serverSocket, this.requisicao.getAddress(), this.requisicao.getPort());
        }
    }
}

class peerWatchdog extends Thread {

    public DatagramSocket serverSocket;

    public peerWatchdog(DatagramSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {

        Mensagem alive = new Mensagem();
        alive.setMessage("ALIVE");

        // check if peers are alive every 30 seconds
        while (true) {
            for (String peer : Servidor.peerAlive.keySet()) {
                try {
                    if (!Servidor.peerAlive.get(peer)) {
                        Servidor.peerAlive.remove(peer);
                        System.out.print("Peer " + peer + " morto. Eliminando seus arquivos");
                        Servidor.printFiles(Servidor.peerFiles.get(peer));
                        Servidor.peerAlive.remove(peer);
                        Servidor.peerFiles.remove(peer);
                    } else {
                        Servidor.send(alive, this.serverSocket, InetAddress.getByName(peer.split(":")[0]),
                                Integer.parseInt(peer.split(":")[1]));
                        Servidor.peerAlive.replace(peer, false);
                    }
                } catch (Exception e) {

                }
            }
            try {
                Thread.sleep(30000);
            } catch (Exception e) {

            }
        }
    }
}
