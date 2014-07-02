package project1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import project1.Message.Operation;

public class SlaveNode implements Runnable {
	
	Socket socket = null;
	PrintWriter pw = null;
	BufferedReader br = null;
	ObjectOutputStream out = null;
	ObjectInputStream in = null;
	boolean running = true;
	
	public SlaveNode() throws UnknownHostException, IOException {
		this.socket = new Socket("localhost",15640);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		
	}
	public void sendToMaster (Message m) throws IOException {
		out.writeObject(m);
	}
	
	public void lauchProcess (Message m) {
		MigratableProcess mp = new ProcessHelper().readProcess(m.getProcessID());
		new Thread(mp).start();
		m.setOperation(Operation.LAUNCH);
		System.out.println("The process has been launched");
	}
	
	public void migrateProcess (Message m) throws IOException {
		MigratableProcess mp = new ProcessHelper().readProcess(m.getProcessID());
		Message newMsg = new Message();
		newMsg.setMp(mp);
		newMsg.setSlaveID(m.getSlaveID());
		newMsg.setOperation(Operation.RESUME);
		sendToMaster(newMsg);	
	}
	
	public void resumeProcess (Message m) throws IOException {
		MigratableProcess mp = new ProcessHelper().readProcess(m.getProcessID());
		mp.resume();
		System.out.println("The process has been resumed");
	}
	
	public void stop() {
		running = false;
	}
	
	public void run() {	
		
		try {
			while(true) {
				Object obj = in.readObject();
				Message m = (Message) obj;
				
				if(m.getOperation() == Operation.LAUNCH) {
					lauchProcess(m);
				} else if (m.getOperation() == Operation.MIGRATE) {
					migrateProcess(m);
				} else if (m.getOperation() == Operation.RESUME) {
					resumeProcess(m);
				}
			}
		}
			
		catch (IOException e) {
			e.printStackTrace();
			stop();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void main (String args[]) throws UnknownHostException, IOException {
		SlaveNode st = new SlaveNode();
		new Thread(st).start();
	}
	
}
