package project1;

public class SlaveInfo {
	
	private int slaveID;
	private int processNum;
	
	public SlaveInfo (int slaveID, int processNum) {
		this.slaveID = slaveID;
		this.processNum = processNum;
	}
	
	public int getSlaveID() {
		return slaveID;
	}
	
	public void setSlaveID(int slaveID) {
		this.slaveID = slaveID;
	}
	
	public int getProcessNum() {
		return processNum;
	}
	
	public void setProcessNum(int processNum) {
		this.processNum = processNum;
	}
		
}
