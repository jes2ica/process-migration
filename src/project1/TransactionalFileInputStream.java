package project1;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable {

	private static final long serialVersionUID = 6829456265076579681L;
	private String fileName;	
	private int pos;
	private boolean mitigated;
	private transient FileInputStream fis;
	
	public TransactionalFileInputStream(String fileName) throws IOException {
		this.fileName = fileName;
		mitigated = false;
		pos = 0;
		fis = open();
	}
	
	@Override
	public int read() throws IOException {
		if(mitigated) {
			mitigated = false;
		}
		fis = open();
		int nextByte = fis.read();
		if(nextByte != -1) {
			pos += 1;
		}
		fis.close();
		return nextByte;
	}
	
	@Override
	public long skip(long n) throws IOException {
		if(n <= 0) {
			return 0;
		}
		pos += n;
		return n;
	}
	
	public FileInputStream open() throws IOException {
		fis = new FileInputStream(fileName);
		fis.skip(pos);
		return fis;
	}
}
