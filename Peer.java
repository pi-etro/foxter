import java.util.Scanner;
import foxter.Mensagem.*;

class Peer{
    public static void main(String args[]){

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
