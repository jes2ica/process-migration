package project1;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

public class MergesortProcess {
	//private static final long serialVersionUID = -6343182790215064198L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	//private String query;
	private static boolean isTerminate;
	private volatile boolean suspending;

	public MergesortProcess(String args[]) throws Exception
	{
		isTerminate = false;
		if (args.length != 3) {
			System.out.println("usage: MergesortProcess <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		//query = args[0];
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
				
				String res = mergeSort(line);
				
				out.println(res);
				
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("MergesortProcess: Error: " + e);
		}


		suspending = false;
	}
	public String mergeSort(String s) {
		String[] ss = s.split(" ");
		
		int length = ss.length;
		int[] arr = new int[length];
		
		
		
		for (int i = 0; i < length; i ++) {
			arr[i] = Integer.parseInt(ss[i]);
		}
		merge(arr,length);
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < length; i ++) {
			res.append(arr[i]);
			res.append(" ");
		}
		return res.toString();
		
	}
	
	public static void  merge(int[] num, int n) {
		int[] p = new int[n];
		merge (num,0,n - 1, p);
	}
	public static void merge (int[] num, int first, int last, int[] temp) {
		if (first < last) {
			int mid = first + (last - first) / 2;
			merge (num, first, mid, temp);
			merge (num, mid + 1, last, temp);
			mergeArray (num, first, mid, last, temp);
		}
	}
	public static void mergeArray (int[] a, int first, int mid, int last, int[] temp) {
		int i = first;
		int m = mid;
		int n = last;
		int j = mid + 1;
		int k = 0;
		while (i <= m && j <= n) {
			if (a[i] <= a[j]) {
				temp[k ++] = a[i ++];
			}
			else {
				temp[k ++] = a[j ++];
			}
		}
		while (i <= m) {
			temp[k ++] = a[i ++];
		}
		while (j <= n) {
			temp[k ++] = a[j ++];
		}
		for (i = 0; i < k; i ++) {
			a[first + i] = temp [i];
		}
	}
	
	
	public void suspend()
	{
		suspending = true;
		while (suspending);
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

}
