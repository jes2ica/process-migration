package project1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import project1.Message.Operation;
import project1.ProcessInfo.Status;

public class MasterHelper implements Runnable {

	ObjectInputStream in = null;
	ObjectOutputStream out = null;
	Socket socket = null;
	boolean running = true;

	public MasterHelper (Socket socket) throws IOException {
		this.socket = socket;
		in = new ObjectInputStream (this.socket.getInputStream());
		out = new ObjectOutputStream (this.socket.getOutputStream());
	}

	@Override
	public void run() {
		try {
			while(true){
				Object obj = in.readObject();
				Message m = (Message) obj;
				if(m.getOperation() == Operation.RESUME) {
					ProcessManager.getPM().resumeProcess(m.getSlaveID(), m.getProcessID());
				}
				if(m.getOperation() == Operation.LAUNCH) {
					ProcessManager.getPM().updateStatus(m.getProcessID(), Status.RUNNING);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	public void sendToSlave (Message m) throws IOException {
		out.writeObject(m);
		System.out.println("Message sended!");
	}
}
