/*
Inicializacao:
Recebe entrada pelo teclado do endereço IP, porta default 10098
Requisicoes:
JOIN: recebe nome dos arquivos do peer, reponde JOIN_OK

LEAVE: remove informacoes do peer, responde LEAVE_OK

SEARCH: recebe nome do arquivo, responde com lista de peers que possui arquivo

UPDATE: recebe de um peer que baixou o arquivo, atualiza info deste peer, responde UPDATE_OK

ALIVE: enviado a cada 30 segundo aos peers, recebe ALIVE_OK, caso não, remove informações do peer
*/

import java.util.Scanner;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import Mensagem.*;

public class Servidor{

    public static String serverAddress;
    public static int serverPort = 10098;

    public static void main(String args[])throws Exception{

        Scanner input = new Scanner(System.in);
        System.out.print("Entre com o endereço IP deste servidor: ");
        Servidor.serverAddress = input.nextLine();
        input.close();
		Servidor.serverAddress = "127.0.0.1"; //temporario

        ClientIn cin = new ClientIn();
        ClientOut cout = new ClientOut();
        cin.start();
        cout.start();

		DatagramSocket serverSocket = new DatagramSocket(10098);
		while(true){
			byte[] recBuffer = new byte[1024];
			DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
			serverSocket.receive(recPacket); // BLOCKING
			String info = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
			System.out.println(info);

			byte[] sendData = new byte[1024];
			sendData = "resposta do servidor".getBytes();
			InetAddress clientAddress = recPacket.getAddress();
			int clientPort = recPacket.getPort();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
	        serverSocket.send(sendPacket);
		}

    }
}

class ClientIn extends Thread{
    public void run(){
        System.out.println("thread ClientIn");
    }
}

class ClientOut extends Thread{
    public void run(){
        System.out.println("thread ClientOut");
    }
}

class AliveSend extends Thread{
    public void run(){
        System.out.println("thread to check if peers are alive");
    }
}

class AliveReceive extends Thread{
    public void run(){
        System.out.println("thread to check if peers are alive");
    }
}
