package project1;

import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

public class GrepProcess implements MigratableProcess
{
	private static final long serialVersionUID = -6343182790215064198L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	private static boolean isTerminate;
	private volatile boolean suspending;

	public GrepProcess(String args[]) throws Exception
	{
		isTerminate = false;
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		System.out.println(args[0]+":"+args[1]+":"+args[2]);
		query = args[0];
		
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2], false);
		
	}

	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				String line = in.readLine();

				if (line == null) break;
				
				if (line.contains(query)) {
					out.println(line);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
			if(!suspending) {
				System.out.println("Finished!");
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
//		while (suspending) {}
	}
	
	public String toString()
	{
		String ss = this.getClass().getName(); 
		return ss;
	}

	public synchronized void resume()
	{
		suspending = false;
		this.notify();
	}

	public void terminate()
	{
		isTerminate = true;	
	}
	
	public static void main (String args[]) throws Exception {
		String[] input = new String[3];
		input[0] = "and";
		input[1] = "in.txt";
		input[2] = "out.txt";
		GrepProcess gp = new GrepProcess(input);
		new Thread(gp).start();
	}
	

}
