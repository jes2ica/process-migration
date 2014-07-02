package project1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MasterThread implements Runnable {
	
	ServerSocket ss;
	Socket socket = null;
	BufferedReader br = null;
	PrintWriter pw = null;
	ObjectOutputStream out = null;
	InputStream in = null;
	static int slaveId = 0;
	
	public MasterThread() throws IOException {
		ss = new ServerSocket(15640);
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				socket = ss.accept();
				System.out.println("Welcome "+slaveId);
				MasterHelper mh = new MasterHelper(socket);
				ProcessManager.getPM().addsidToHelper(slaveId, mh);
				ProcessManager.getPM().addToSlaveList(slaveId);
				ProcessManager.getPM().addsidToPid(slaveId);
				new Thread(mh).start();
				slaveId++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
