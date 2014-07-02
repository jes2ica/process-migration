package project1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawlerProcess implements MigratableProcess
{
	private static final long serialVersionUID = -6343182790215064198L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private static boolean isTerminate;
	private Queue<String> queue;
	private HashSet<String> visited;
	

	private volatile boolean suspending;

	public WebCrawlerProcess(String args[]) throws Exception
	{
		isTerminate = false;
		if (args.length != 2) {
			System.out.println("usage: WebCrawlerProcess <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1], false);
		queue = new LinkedList<String>();
		visited = new HashSet<String>();
	}

	public void run()
	{
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				String line = in.readLine();

				if (line == null) break;
				
				queue.offer(line);
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
			
			while (!queue.isEmpty() && (visited.size() < 10) && !suspending) {
				crawl();
			}    
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		}


		suspending = false;
	}
	
	private void crawl() throws IOException {
		
		PrintStream out = new PrintStream(outFile);
		
		String cur = queue.poll();
		if(visited.contains(cur)) return;
		StringBuilder sb = new StringBuilder();
		URL url = new URL(cur);
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		String line = br.readLine();
		if(line == null) return;
		while((line  = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		br.close();
		
		visited.add(cur);
		String content = sb.toString();
	    out.println(url);
	    out.println(content);
	    out.println();
	    out.flush();
	    
	    Pattern pattern = Pattern.compile("http://(\\w+\\.)*(\\w+)");
	    Matcher matcher = pattern.matcher(sb.toString());
	    
	    while (matcher.find()) {
	      String newURL = matcher.group();
	      if (!visited.contains(newURL)) {
	        queue.add(newURL);
	      }
	    }
	    
	    suspending = false;
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

