import java.util.Scanner;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import Mensagem.*;

//@SuppressWarnings("resource") // removes never closer warnings

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

		ServerHandler server = new ServerHandler();
		server.start();

		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.print("MENU:\n1 JOIN\n2 SEARCH\n3 DOWNLOAD\nEntre com a opção: ");
			int option = in.nextInt();
			switch(option){
				case 1:
					// JOIN
					break;
				case 2:
					// SEARCH
					break;
				case 3:
					// DOWNLOAD
					break;
				default:
					System.out.println("Opção inválida!");
			}
		}

	}
}

class ServerHandler extends Thread {

	private DatagramPacket recPacket;

	public void run() {
		// se for pacote ALIVE ativa flag para responder
		// caso contrario repassa pacote para o menu
		try {
			DatagramSocket clientSocket = new DatagramSocket(10098);

			// awaits serber contact
			while (true) {
				byte[] recBuffer = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
				clientSocket.receive(recPacket); // blocking
				String message = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
				if (message.equals("ALIVE")) {
					AliveHandler answer = new AliveHandler();
					answer.start();
				} else {
					setPacket(recPacket);
				}
			}
		} catch (Exception e) {

		}
	}

	public void setPacket(DatagramPacket packet) {
		this.recPacket = packet;
	}

	public DatagramPacket getPacket() {
		return this.recPacket;
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
