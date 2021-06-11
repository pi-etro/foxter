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
import foxter.Mensagem.*;

public class Servidor{

    public static String serverAddress;
    public static int serverPort = 10098;

    public static void main(String args[]){

        Scanner input = new Scanner(System.in);
        System.out.print("Entre com o endereço IP deste servidor: ");
        Servidor.serverAddress = input.nextLine();
        input.close();

        ClientIn cin = new ClientIn();
        ClientOut cout = new ClientOut();
        cin.start();
        cout.start();
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
