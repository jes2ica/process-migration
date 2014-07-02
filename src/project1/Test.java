package project1;




import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import project1.TransactionalFileInputStream;
import project1.TransactionalFileOutputStream;


public class Test implements MigratableProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3200687207966080647L;

	private String[] args;
	private String inFile;
	private String outFile;
	private int i = 0;

	private TransactionalFileInputStream inStream;
	private TransactionalFileOutputStream outStream;
	

	
	

	private static boolean isTerminate;
	private volatile boolean suspending;
	
	public Test(String[] _args) throws Exception {
		isTerminate = false;
		this.args = _args;

		if (args.length != 3) {
			System.out.println("usage: Test <infile> <outfile>");
			throw new Exception("Invalid arguments");
		}
		

		inStream = new TransactionalFileInputStream(args[1]);
		outStream = new TransactionalFileOutputStream(args[2], false);
	}
	
	

	public String toString() {
		return "Test " + args[1] + " " + args[2];
	}

	@Override
	public void run() {
      
		

		DataInputStream in = new DataInputStream(inStream);
		PrintStream out = new PrintStream(outStream);

		
		
		suspending = false;
		
		try {
			String line;
			while (!suspending) {
				line = in.readLine();
				
				if (line == null) {
					System.out.println("line is null");
					
                    break;
				}
				out.println(line);
				System.out.println("Line:" + line);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		suspending = false;
	}

	@Override
	public void suspend() {
		suspending = true;
		while(suspending  && !isTerminate);
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
}