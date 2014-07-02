package project1;

import java.io.Serializable;

public interface MigratableProcess extends Runnable, Serializable{
	
	void suspend();
	String toString();
	void terminate();
	void resume();
}
