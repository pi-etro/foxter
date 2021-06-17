import java.util.*;
import java.io.*;
import java.net.*;
import Mensagem.*;

@SuppressWarnings("resource") // removes never closer warnings

public class Servidor {

	public static String serverAddress;
	public static int serverPort = 10098; // default server port

	public static void main(String args[]) throws Exception {

		// address input
		// Scanner input = new Scanner(System.in);
		// System.out.print("Entre com o endereço IP do servidor: ");
		// Servidor.serverAddress = input.nextLine();
		// input.close();

		DatagramSocket serverSocket = new DatagramSocket(serverPort);

		// waits peers UDP contact
		while (true) {
			try {
				byte[] recBuffer = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
				System.out.print("Esperando resposta");
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
		if (this.requisicao.getMessage().equals("JOIN")) { // JOIN_OK
			Mensagem resposta = new Mensagem();
			resposta.setMessage("JOIN_OK");
			Servidor.send(resposta, this.serverSocket, this.requisicao.getAddress(), this.requisicao.getPort());
		}
	}
}

// Requisicoes: JOIN: recebe nome dos arquivos do peer, reponde JOIN_OK
//
// LEAVE: remove informacoes do peer, responde LEAVE_OK
//
// SEARCH: recebe nome do arquivo, responde com lista de peers que possui
// arquivo
//
// UPDATE: recebe de um peer que baixou o arquivo, atualiza info deste peer,
// responde UPDATE_OK
//
// ALIVE: enviado a cada 30 segundo aos peers, recebe ALIVE_OK, caso não, remove
// informações do peer
