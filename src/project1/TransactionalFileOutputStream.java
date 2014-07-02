package project1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8526864797605368368L;
	private String fileName;
	private boolean migrated;
	private int pos;
	private transient RandomAccessFile raf;
	
	public TransactionalFileOutputStream (String fileName, boolean migrated) throws IOException {
		this.fileName = fileName;
		this.migrated = migrated;
		this.raf = open();
		this.pos = 0;
	}
	
	@Override
	public void write(int arg0) throws IOException {
		if(migrated) {
			migrated = false;
		}
		raf = open();
		raf.write(arg0);
		pos++;
		raf.close();
	}

	public RandomAccessFile open() throws IOException {
		RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
		raf.seek(pos);
		return raf;
	}
}
