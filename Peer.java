import java.util.Scanner;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import Mensagem.*;

class Peer{



    public static void main(String args[]) throws Exception{

        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        int serverPort = 10098;

        ServerIn sin = new ServerIn();
        ServerOut sout = new ServerOut();
        sin.start();
        sout.start();

        while (true){
        Scanner input = new Scanner(System.in);
        String mensagem = input.nextLine();
        DatagramSocket clientSocket = new DatagramSocket(); // SO da uma porta automaticamente
        byte[] sendData = new byte[1024];
        sendData = mensagem.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
        clientSocket.send(sendPacket);

		byte[] recBuffer = new byte[1024];
		DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
		clientSocket.receive(recPacket); // BLOCKING

		String info = new String(recPacket.getData(), recPacket.getOffset(), recPacket.getLength());
		System.out.println(info);
    }



    }
}

class ServerIn extends Thread{
    public void run(){
        System.out.println("thread ClientIn");
    }
}

class ServerOut extends Thread{
    public void run(){
        System.out.println("thread ClientOut");

    }
}
