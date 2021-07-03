package Mensagem;

import java.net.InetAddress;
import java.util.ArrayList;
import java.io.Serializable;

@SuppressWarnings("serial") // serialVersionUID warnings

public class Mensagem implements Serializable {
    private String message = " ";
    private InetAddress address;
    private int port;
    private ArrayList<String> list;

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String m) {
        this.message = m;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public void setAddress(InetAddress a) {
        this.address = a;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int p) {
        this.port = p;
    }

    public ArrayList<String> getList() {
        return this.list;
    }

    public void setList(ArrayList<String> l) {
        this.list = l;
    }

    public void addFile(String s) {
        this.list.add(s);
    }
}
