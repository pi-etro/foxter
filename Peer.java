import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.Thread;
import Mensagem.*;

@SuppressWarnings({ "resource", "unused" }) // removes never closed and not used warnings

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

        // desired file
        String filetoDownload = "";
        ArrayList<String> peerswithFile = new ArrayList<String>();

        DatagramSocket clientSocket = new DatagramSocket(peerPort);

        // initialize a thread for peer download requests
        PeerRequests pr = new PeerRequests(peerPort, folderPath);
        pr.start();

        // initialize a thread for server messages
        ServerHandler server = new ServerHandler(clientSocket);
        server.start();

        // options menu
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("MENU:\n[1] JOIN\n[2] SEARCH\n[3] DOWNLOAD\n[4] LEAVE\nEntre com a opção: ");
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
                    filetoDownload = input.nextLine();
                    l.add(filetoDownload);
                    requisicao.setList(l);

                    // waits for server response
                    Mensagem resposta = waitResponse("SEARCH_OK", server, requisicao, clientSocket, serverAddress,
                            serverPort); // blocking

                    peerswithFile = resposta.getList();

                    System.out.print("Peers com arquivo solicitado:");
                    printList(resposta.getList());

                } else if (option == 3) { // DOWNLOAD

                    // download available only after search
                    if (!filetoDownload.isEmpty()) {
                        Boolean received = false;

                        // try to download file from all peers
                        // ask again if all peers deny
                        while (!received) {
                            for (int i = 0; i < peerswithFile.size(); i++) {

                                String ip = peerswithFile.get(i).split(":")[0];
                                int port = Integer.parseInt(peerswithFile.get(i).split(":")[1]);

                                // tcp connection
                                Socket s = new Socket(ip, port);
                                OutputStream os = s.getOutputStream();
                                DataOutputStream writer = new DataOutputStream(os);

                                // send name of the desired file
                                writer.writeBytes(filetoDownload + "\n");

                                DataInputStream is = new DataInputStream(s.getInputStream());
                                FileOutputStream fos = new FileOutputStream(
                                        new File(folderPath, filetoDownload).getAbsolutePath());
                                byte[] buffer = new byte[1024];

                                // receives data until EOF
                                int read;
                                while ((read = is.read(buffer)) != -1) {
                                    fos.write(buffer, 0, read);
                                }
                                fos.close();
                                is.close();

                                // read header to check if download was denied
                                byte[] b = new byte[15];
                                InputStream fis = new FileInputStream(
                                        new File(folderPath, filetoDownload).getAbsolutePath());
                                fis.read(b);

                                if (!new String(b).equals("DOWNLOAD_NEGADO")) {
                                    received = true;
                                    System.out.println("Arquivo " + filetoDownload + " baixado com sucesso na pasta " + folderPath);
                                    break;
                                } else {

                                    // delete temporary file with DOWNLOAD_NEGADO message
                                    new File(folderPath, filetoDownload).delete();

                                    int index = i == peerswithFile.size() - 1 ? 0 : i + 1;
                                    System.out.println(
                                            "Peer " + ip + ":" + port + " negou o download, pedindo agora para o peer "
                                                    + peerswithFile.get(index));
                                }
                                fis.close();
                            }
                            // waits 10 seconds to start requesting again
                            if (!received)
                                Thread.sleep(10000);
                        }

                        // send request to update
                        Mensagem requisicao = new Mensagem();
                        ArrayList<String> l = new ArrayList<String>();
                        l.add(filetoDownload);
                        requisicao.setMessage("UPDATE");
                        requisicao.setList(l);

                        // waits for server response
                        Mensagem resposta = waitResponse("UPDATE_OK", server, requisicao, clientSocket, serverAddress,
                                serverPort); // blocking
                    }
                } else if (option == 4) { // LEAVE

                    // send request to leave
                    Mensagem requisicao = new Mensagem();
                    requisicao.setMessage("LEAVE");

                    // waits for server response
                    Mensagem resposta = waitResponse("LEAVE_OK", server, requisicao, clientSocket, serverAddress,
                            serverPort); // blocking

                    // nothing to print

                } else {
                    System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("Opção inválida!");
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
                    AliveHandler answer = new AliveHandler(this.clientSocket, recPacket.getAddress(),
                            recPacket.getPort());
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

    private DatagramSocket clientSocket;
    private InetAddress serverAddress;
    private int serverPort;

    public AliveHandler(DatagramSocket socket, InetAddress address, int port) {
        this.clientSocket = socket;
        this.serverAddress = address;
        this.serverPort = port;
    }

    public void run() {
        try {
            Mensagem resposta = new Mensagem();
            resposta.setMessage("ALIVE_OK");

            // send message with alive confirmation
            Peer.send(resposta, this.clientSocket, this.serverAddress, this.serverPort);
        } catch (Exception e) {

        }
    }
}

@SuppressWarnings("resource") // removes never closed warnings

class PeerRequests extends Thread {

    private int port;
    private String folderPath;

    public PeerRequests(int p, String f) {
        this.port = p;
        this.folderPath = f;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);

            while (true) {
                try {
                    Socket no = serverSocket.accept(); // blocking

                    // start a thread to handle the new peer request
                    PeerHandler peer = new PeerHandler(no, this.folderPath);
                    peer.start();
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {

        }
    }
}

class PeerHandler extends Thread {

    private Socket no;
    private String folderPath;

    public PeerHandler(Socket no, String f) {
        this.no = no;
        this.folderPath = f;
    }

    public void run() {

        try {

            InputStreamReader is = new InputStreamReader(this.no.getInputStream());
            BufferedReader reader = new BufferedReader(is);

            // receive the name of the file to send
            String filetoSend = reader.readLine();

            DataOutputStream os = new DataOutputStream(this.no.getOutputStream());
            byte[] buffer = new byte[1024];

            // randomly choose to send or not the file
            Random random = new Random();

            // if random is true and this peer has the file on the folder then send
            if (random.nextBoolean() && new File(folderPath, filetoSend).isFile()) {

                FileInputStream fis = new FileInputStream(new File(folderPath, filetoSend).getAbsolutePath());

                // send file
                int read = 0;
                while ((read = fis.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                }

                fis.close();
            } else { // send string DOWNLOAD_NEGADO
                os.writeBytes("DOWNLOAD_NEGADO\n");
            }
            os.flush();
            os.close();
            this.no.close();
        } catch (Exception e) {

        }
    }
}
