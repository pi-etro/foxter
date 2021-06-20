
// ALIVE_OK ainda nao implementado
// Downloads TCP de outros peers ainda nao implementado

import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.Thread;
import Mensagem.*;

// @SuppressWarnings("resource") // removes never closer warnings

public class Peer {

    public static void main(String args[]) throws Exception {

        // default server IP and port
        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        int serverPort = 10098;

        Scanner input = new Scanner(System.in);

        // peer address
        System.out.print("Entre com o endereço IP deste peer: ");
        InetAddress peerAddress = InetAddress.getByName(input.nextLine());

        // peer port
        System.out.print("Entre com a porta deste peer: ");
        int peerPort = Integer.parseInt(input.nextLine());

        // peer folder
        System.out.print("Entre com o endereço da pasta: ");
        String folderPath = input.nextLine();
        File[] listFiles = new File(folderPath).listFiles();
        while (listFiles == null) {
            System.out.print("Indereço inválido, insira novamente: ");
            folderPath = input.nextLine();
            listFiles = new File(folderPath).listFiles();
        }

        // peer file names
        ArrayList<String> fileNames = new ArrayList<String>();
        for (File f : listFiles) {
            if (f.isFile())
                fileNames.add(f.getName());
        }

        DatagramSocket clientSocket = new DatagramSocket();

        // initialize a thread for server messages
        ServerHandler server = new ServerHandler(clientSocket);
        server.start();

        // options menu
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("MENU:\n[1] JOIN\n[2] SEARCH\n[3] DOWNLOAD\nEntre com a opção: ");
            try {
                int option = in.nextInt();
                if (option == 1) { // JOIN

                    // send request and list of files
                    Mensagem requisicao = new Mensagem();
                    requisicao.setMessage("JOIN");
                    requisicao.setList(fileNames);

                    // waits for server response
                    Mensagem resposta = waitResponse("JOIN_OK", server, requisicao, clientSocket, serverAddress,
                            serverPort); // blocking

                    System.out.print("Sou peer " + resposta.getAddress().toString().replace("/", "") + ":"
                            + resposta.getPort() + " com arquivos");
                    printList(fileNames);
                } else if (option == 2) { // SEARCH

                    // send request and name of file to search
                    Mensagem requisicao = new Mensagem();
                    requisicao.setMessage("SEARCH");

                    ArrayList<String> l = new ArrayList<String>();
                    System.out.print("Entre com o nome do arquivo desejado: ");
                    l.add(input.nextLine());
                    requisicao.setList(l);

                    // waits for server response
                    Mensagem resposta = waitResponse("SEARCH_OK", server, requisicao, clientSocket, serverAddress,
                            serverPort); // blocking

                    System.out.print("Peers com arquivo solicitado:");
                    printList(resposta.getList());
                } else if (option == 3) { // DOWNLOAD

                } else if (option == 4) { // LEAVE option needed?

                } else {
                    System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("Opção inválida!");
            }
        }

    }

    // serialize and send Mensagem object
    private static void send(Mensagem m, DatagramSocket s, InetAddress a, int p) {
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

    // waits for server response expected
    // send new requests in timeout
    private static Mensagem waitResponse(String r, ServerHandler sh, Mensagem m, DatagramSocket s, InetAddress a,
            int p) {

        int timeout = 15; // time in seconds to send a new request

        // first request
        send(m, s, a, p);

        // periodically check response and sends new request on timeout
        int t = 0;
        while (!sh.getResposta().getMessage().equals(r)) {
            try {
                Thread.sleep(100);
                t++;
            } catch (Exception e) {

            }
            if (t == timeout * 10) {
                send(m, s, a, p);
                t = 0;
            }
        }

        Mensagem resposta = sh.getResposta();
        sh.setResposta(new Mensagem());
        return resposta;
    }

    private static void printList(ArrayList<String> lista) {
        for (String item : lista) {
            System.out.print(" " + item);
        }
        System.out.print("\n");
    }
}

class ServerHandler extends Thread {

    private Mensagem resposta;
    private DatagramSocket clientSocket;

    public ServerHandler(DatagramSocket socket) {
        this.resposta = new Mensagem();
        this.clientSocket = socket;
    }

    public void run() {
        try {
            // waits server contact
            while (true) {

                byte[] recBuffer = new byte[1024];
                DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
                clientSocket.receive(recPacket); // blocking

                // deserialize Mensagem object
                ByteArrayInputStream byteStream = new ByteArrayInputStream(recBuffer);
                ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(byteStream));
                Mensagem resposta = (Mensagem) objectIn.readObject();

                if (resposta.getMessage().equals("ALIVE")) {
                    AliveHandler answer = new AliveHandler();
                    answer.start();
                } else {
                    setResposta(resposta);
                }
            }
        } catch (Exception e) {

        }
    }

    public void setResposta(Mensagem r) {
        this.resposta = r;
    }

    public Mensagem getResposta() {
        return this.resposta;
    }
}

class AliveHandler extends Thread {
    public void run() {
        System.out.println("thread AliveHandler");
    }
}

class PeerHandler extends Thread {
    public void run() {
        System.out.println("thread PeerHandler");
    }
}
