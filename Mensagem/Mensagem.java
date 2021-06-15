package Mensagem;

import java.util.ArrayList;
import java.io.Serializable;

public class Mensagem implements Serializable{
	private String message = " ";
	private String address;
	private int port;
	private ArrayList<String> list;

	public String getMessage(){
		return this.message;
	}

	public void setMessage(String m){
		this.message = m;
	}

	public String getAddress(){
		return this.address;
	}

	public void setAddress(String a){
		this.address = a;
	}

	public int getPort(){
		return this.port;
	}

	public void setPort(int p){
		this.port = p;
	}

	public ArrayList<String> getList(){
		return this.list;
	}

	public void setList(ArrayList<String> l){
		this.list = l;
	}
}
