
/*
Inicializacao:
Recebe entrada pelo teclado do endereço IP, porta default 10098
*/

import java.util.Scanner;
import java.net.*;
import java.io.*;
import Mensagem.*;

@SuppressWarnings("resource") // removes never closer warnings

public class Servidor {

	public static String serverAddress;
	public static int serverPort = 10098;

	public static void main(String args[]) throws Exception {

		// address input
		/*
		 * Scanner input = new Scanner(System.in);
		 * System.out.print("Entre com o endereço IP do servidor: ");
		 * Servidor.serverAddress = input.nextLine(); input.close();
		 * Servidor.serverAddress = "127.0.0.1"; // temporario
		 */

		DatagramSocket serverSocket = new DatagramSocket(serverPort);

		// awaits peers contact
		while (true) {
			// byte[] recBuffer = new byte[1024];
			// DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
			// serverSocket.receive(recPacket); // blocking

			try {
				// awaits server contact
				byte[] recBuffer = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);

				serverSocket.receive(recPacket); // blocking

				ByteArrayInputStream byteStream = new ByteArrayInputStream(recBuffer);
				ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(byteStream));
				// ObjectInputStream objectIn = new ObjectInputStream(new
				// ByteArrayInputStream(recPacket));
				Mensagem resposta = (Mensagem) objectIn.readObject();
				// objectIn.close();
				System.out.println(resposta.getMessage());
			} catch (Exception e) {

			}
			// ClientHandler peer = new ClientHandler(recPacket);
			// peer.start();
		}
	}
}

class ClientHandler extends Thread {

	public InetAddress clientAddress;
	public int clientPort;
	public String clientMessage;

	public ClientHandler(DatagramPacket recPacket) {
		// unpack datagram
		this.clientAddress = recPacket.getAddress();
		this.clientPort = recPacket.getPort();
		this.clientMessage = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
	}

	public void run() {
		System.out.println(this.clientAddress);
		System.out.println(this.clientPort);
		System.out.println(this.clientMessage);
	}
}

/*
 * Requisicoes: JOIN: recebe nome dos arquivos do peer, reponde JOIN_OK
 *
 * LEAVE: remove informacoes do peer, responde LEAVE_OK
 *
 * SEARCH: recebe nome do arquivo, responde com lista de peers que possui
 * arquivo
 *
 * UPDATE: recebe de um peer que baixou o arquivo, atualiza info deste peer,
 * responde UPDATE_OK
 *
 * ALIVE: enviado a cada 30 segundo aos peers, recebe ALIVE_OK, caso não, remove
 * informações do peer
 */
