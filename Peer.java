import java.util.Scanner;
import java.net.*;
import Mensagem.*;
import java.io.*;

// quando enviar LEAVE?
@SuppressWarnings("resource") // removes never closer warnings

public class Peer {

	public static void main(String args[]) throws Exception {

		InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
		int serverPort = 10098;

		// server address input
		/*
		 * Scanner input = new Scanner(System.in);
		 * System.out.print("Entre com o endereço IP deste peer: "); InetAddress
		 * peerAddress = InetAddress.getByName(input.nextLine());
		 * System.out.print("Entre com a porta deste peer: "); int peerPort =
		 * Integer.parseInt(input.nextLine()); input.close();
		 */

		InetAddress peerAddress = InetAddress.getByName("127.0.0.1"); // temporario
		int peerPort = 55000; // temporario

		DatagramSocket clientSocket = new DatagramSocket();

		ServerHandler server = new ServerHandler(clientSocket);
		server.start();

		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.print("MENU:\n1 JOIN\n2 SEARCH\n3 DOWNLOAD\nEntre com a opção: ");
			int option = in.nextInt();
			switch (option) {
			case 1:
				// JOIN
				Mensagem requisicao = new Mensagem();
				requisicao.setMessage("JOIN");

				Mensagem resposta = new Mensagem();

				send(requisicao, clientSocket, serverAddress, serverPort);
				long sent = System.nanoTime();
				while (!resposta.getMessage().equals("JOIN_OK")) {
					if (System.nanoTime() - sent >= 15 * Math.pow(10, 9)) {
						send(requisicao, clientSocket, serverAddress, serverPort);
						sent = System.nanoTime();
					}
					resposta = server.getResposta();
					System.out.println(resposta.getMessage());
				}
				break;
			case 2:
				// SEARCH
				break;
			case 3:
				// DOWNLOAD
				break;
			case 4:
				// LEAVE
				break;
			default:
				System.out.println("Opção inválida!");
			}
		}

	}

	private static void send(Mensagem m, DatagramSocket s, InetAddress a, int p) {
		try {
			// serialize
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
			// ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			// ObjectOutput objectOut = new ObjectOutputStream(byteStream);
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

class ServerHandler extends Thread {

	private Mensagem resposta;
	private DatagramSocket clientSocket;

	public ServerHandler(DatagramSocket socket) {
		this.resposta = new Mensagem();
		this.clientSocket = socket;
	}

	public void run() {
		// se for pacote ALIVE ativa flag para responder
		// caso contrario repassa pacote para o menu
		try {
			// awaits server contact
			while (true) {
				byte[] recBuffer = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);

				clientSocket.receive(recPacket); // blocking

				ByteArrayInputStream byteStream = new ByteArrayInputStream(recBuffer);
				ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(byteStream));
				// ObjectInputStream objectIn = new ObjectInputStream(new
				// ByteArrayInputStream(recPacket));
				Mensagem resposta = (Mensagem) objectIn.readObject();
				// objectIn.close();

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
