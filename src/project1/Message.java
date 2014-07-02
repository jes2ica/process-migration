package project1;

import java.io.Serializable;

public class Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MigratableProcess mp;
	private int slaveID;
	private int processID;
	private Enum<Operation> operation;
	
	public enum Operation {
	    LAUNCH, MIGRATE, SUSPEND,RESUME, TERMINATE
	}
	public MigratableProcess getMp() {
		return mp;
	}
	public void setMp(MigratableProcess mp) {
		this.mp = mp;
	}
	public int getSlaveID() {
		return slaveID;
	}
	public void setSlaveID(int slaveID) {
		this.slaveID = slaveID;
	}
	public int getProcessID() {
		return processID;
	}
	public void setProcessID(int processID) {
		this.processID = processID;
	}
	public Enum<Operation> getOperation() {
		return operation;
	}
	public void setOperation(Enum<Operation> operation) {
		this.operation = operation;
	}
	
}
