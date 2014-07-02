package project1;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import project1.Message.Operation;
import project1.ProcessInfo.Status;

public class ProcessManager {
	
	private volatile ArrayList<Integer> processList;
	private volatile ArrayList<Integer> slaveList;
	private volatile Hashtable<Integer, ArrayList<Integer>> sidToPid;
	private volatile Hashtable<Integer, MasterHelper> sidToHelper;
	private volatile Hashtable<Integer, ProcessInfo> pidToProcess;
	private static ProcessManager PM;
	private static int processId = 0;
	private static int average = 0;
	private static int bestSlaveID = 0;
	private ProcessHelper ph = new ProcessHelper();
	
	public static synchronized ProcessManager getPM() {
		if(PM == null) {
			PM = new ProcessManager();
		}
		return PM;
	}
	
	public ProcessManager () {
		processList = new ArrayList<Integer>();
		slaveList = new ArrayList<Integer>();
		sidToPid = new Hashtable<Integer,ArrayList<Integer>>();
		sidToHelper = new Hashtable<Integer, MasterHelper>();
		pidToProcess = new Hashtable<Integer,ProcessInfo>();
//		sidToSlave = new Hashtable<Integer,SlaveInfo>();	
	}
	
	public void launchProcess (ProcessInfo process) throws Exception {
		//Launch new process
		
		Message m = new Message();
		m.setProcessID(process.getProcessID());
		m.setMp(process.getProcess());
		m.setSlaveID(process.getSlaveID());
		m.setOperation(Operation.LAUNCH);
		ph.writeProcess(process.getProcess(),process.getProcessID());
		sidToHelper.get(process.getSlaveID()).sendToSlave(m);
		System.out.println("Sending the lauching command!");
		addProcess(process);
		updateStatus(m.getProcessID(), Status.RUNNING);
	}

	public void migrateProcess(int slaveId, int processId) throws Exception{
		//migrate a process from one node to other node
		
		ProcessInfo process = pidToProcess.get(processId);
		Message m = new Message();
		m.setProcessID(processId);
		m.setSlaveID(slaveId);
		m.setOperation(Operation.MIGRATE);
		process.getProcess().suspend();
		updateStatus(m.getProcessID(), Status.SUSPENDING);
		System.out.println("The process has been suspended");
		ph.writeProcess(process.getProcess(),process.getProcessID());
		sidToHelper.get(process.getSlaveID()).sendToSlave(m);
		removeFromSlave(process.getSlaveID(), processId);
		System.out.println("Sending the migrating command!");
	}
	
	public void resumeProcess (int slaveId, int processId) throws IOException {
		//resume a suspending process
		
		ProcessInfo process = pidToProcess.get(processId);
		Message m = new Message();
		m.setMp(process.getProcess());
		m.setSlaveID(slaveId);
		m.setOperation(Operation.RESUME);
		sidToHelper.get(slaveId).sendToSlave(m);
		addToSlave(slaveId, processId);
		process.setSlaveID(slaveId);
		updateStatus(m.getProcessID(), Status.RUNNING);
	}
	
	public void terminateProcess (int processId) throws IOException {
		//terminate a process
		
		ProcessInfo process = pidToProcess.get(processId);
		Message m = new Message();
		m.setProcessID(processId);
		m.setSlaveID(process.getSlaveID());
		m.setOperation(Operation.TERMINATE);
		sidToHelper.get(process.getSlaveID()).sendToSlave(m);
		System.out.println("Sending the terminating command!");
		removeProcess(process.getSlaveID(), process.getProcessID());
	}
		
	private void suspendProcess (int processId) throws IOException {
		//suspend a process
		
		ProcessInfo process = pidToProcess.get(processId);
		Message m = new Message();
		m.setProcessID(processId);
		m.setSlaveID(process.getSlaveID());
		m.setOperation(Operation.SUSPEND);
		sidToHelper.get(process.getSlaveID()).sendToSlave(m);
		updateStatus(m.getProcessID(), Status.SUSPENDING);
	}
	
private void startBalanceTimer() {
		
		Timer balanceSlave = new Timer();
		balanceSlave.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
			for (int slaveID : slaveList) {
				average += sidToPid.get(slaveID).size();
			}
			if (slaveList.size() != 0) {
			average /= slaveList.size();
			}
			for (int slaveID : slaveList) {
				while (sidToPid.get(slaveID).size() > average) {
					try {
						bestSlaveID = chooseBestSlave();
						if (bestSlaveID != slaveID) {
						migrateProcess(bestSlaveID,sidToPid.get(slaveID).get(0));
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
				    
			}
				

			}, 5000, 5000);	
	}
	
	private void addToSlave (int slaveId, int processId) {
		ArrayList<Integer> processList = sidToPid.get(slaveId);
		processList.add(processId);
	}
	
	public void addProcess(ProcessInfo process) {
		addToProcessList(process);
		addsidToPid(process.getSlaveID(),process.getProcessID());
		addpidToProcess (process.getProcessID(), process);
	}
	
	public void addToProcessList (ProcessInfo process) {
		synchronized(processList) {
			processList.add(process.getProcessID());
		}
	}
	public void addsidToPid (int slaveID) {
		synchronized (sidToPid) {
			if(!sidToPid.containsKey(slaveID)) {
				sidToPid.put(slaveID, new ArrayList<Integer>());
			}
		}
	}
	
	public void addsidToPid (int slaveID, int processID) {
		synchronized (sidToPid) {
			sidToPid.get(slaveID).add(processID);
		}
	}
	
	public void addpidToProcess (int processID,ProcessInfo process) {
		synchronized (pidToProcess) {
			pidToProcess.put(processID,process);
		}
	}
	
	public void addsidToHelper (int sid, MasterHelper mh) {
		synchronized(sidToHelper) {
			sidToHelper.put(sid, mh);
		}
	}
    
    public void addToSlaveList (int sid) {
    	synchronized(slaveList) {
    		slaveList.add(sid);
    	}
    }
    
    private void removeProcess (int slaveId, int processId) {
		removeFromSlave(slaveId, processId);
		removeFromProcessList(processId);
		removeFromProcess(processId);
	}
	
	private void removeFromProcess(int processId) {
		synchronized(pidToProcess) {
			pidToProcess.remove(processId);
		}
	}
	private void removeFromProcessList(int processId) {
		synchronized(processList) {
			for(int i = 0; i < processList.size(); i++) {
				if(processList.get(i) == processId) {
					processList.remove(i);
				}
			}
		}
	}
	private void removeFromSlave (int slaveId, int processId) {
		ArrayList<Integer> processList = sidToPid.get(slaveId);
		for(int i = 0; i < processList.size(); i++) {
			if(processList.get(i) == processId) {
				processList.remove(i);
			}
		}
	}
	
	public void removeFromSlaveList (SlaveInfo slave) {
    	synchronized(slaveList) {
    		slaveList.remove(slave);
    	}
    }
	
	public void removepidToProcess(int processID) {
    	synchronized(pidToProcess) {
    		pidToProcess.remove(processID);
		}
    }
    public void removesidToPid(int slaveID, int processID) {
    	synchronized (sidToPid) {
    		for (int i = 0; i < sidToPid.get(slaveID).size(); i ++) {
    			if (sidToPid.get(slaveID).get(i) == processID) {
    				sidToPid.get(slaveID).remove(i);
    			}
    		}
    	}
    }
	
	
	public int chooseBestSlave() {
		int min = Integer.MAX_VALUE;
		int bestSlave = 0;
		
		for(int slaveId : slaveList) {
			if (sidToPid.get(slaveId).size() < min) {
				min = sidToPid.get(slaveId).size();
				bestSlave = slaveId;
			}
		}
		return bestSlave;
	}
	
	private void quit() {
		System.exit(0);
	}
	
	
	public void updateStatus (int processId, Enum<Status> status) {
		synchronized(pidToProcess) {
			pidToProcess.get(processId).setStatus(status);
		}
	}
	
	public void printProcess () {
		System.out.println("ProcessID\tProcessName\t\tSlaveID\t\tProcessStatus\t");
		String processName;
		int slaveID;
		Enum<Status> status;
		
		for (int processID : processList) {
			processName = pidToProcess.get(processID).getProcessName();
			slaveID = pidToProcess.get(processID).getSlaveID();
			status = pidToProcess.get(processID).getStatus();
			
			System.out.printf("[%d]\t\t[%s]\t\t[%d]\t\t(%s)\n",processID, processName,slaveID, status);
		}
	}
	public void printSlave() {
		System.out.println("SlaveID\tNumberOfProcess");
		for (int slaveId : slaveList) {
			System.out.printf("[%d]\t[%d]\n",slaveId, sidToPid.get(slaveId).size());
		}
	}
	
	public void start() throws Exception {
		
		MasterThread mt = null;
		try {
			mt = new MasterThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Thread t = new Thread(mt);
		t.start();
		this.startBalanceTimer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			String command;
			String args[];
			try {
				command = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			args = command.split(" ");
			if (args[0].equals("quit")) {
				quit();
			}
			else if (args[0].equals("terminate")) {
				int tProcessID;
				if (args.length == 1) {
					System.out.println("Please provide processID");
					continue;
				}
				else {
					tProcessID = Integer.parseInt(args[1]);
					terminateProcess(tProcessID);
				}
				
			}
			else if (args[0].equals("suspend")) {
				int tProcessID;
				if (args.length == 1) {
					System.out.println("Please provide processID");
					continue;
				}
				else {
					tProcessID = Integer.parseInt(args[1]);
					suspendProcess(tProcessID);
				}
			}
			else if (args[0].equals("migrate")) {
				int slaveID;
				int processID;
				if (args.length == 1) {
					System.out.println("Please provide process ID");
					continue;
				}
				else if (args.length == 2) {
					processID = Integer.parseInt(args[1]);
					if (pidToProcess.containsKey(processID)) {
					slaveID = chooseBestSlave();
					try {
						migrateProcess(slaveID,processID);
					} catch (Exception e) {
						e.printStackTrace();
					}
					}
					else {
						System.out.println("process ID is wrong");
						continue;
					}
				}
				else {
					processID = Integer.parseInt(args[1]);
					if (pidToProcess.containsKey(processID)) {
						slaveID = Integer.parseInt(args[2]);
						if (sidToPid.containsKey(slaveID)) {
							try {
								migrateProcess(slaveID, processID);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						else {
							System.out.println("slave ID is wrong");
							continue;
						}
					}
					else {
						    System.out.println("process ID is wrong");
					}
					
				}
			}
			else if (args[0].equals("print")) {
				if (args.length == 1) {
					System.out.println("please select print type: \n");
					System.out.println("print process: print process information");
					System.out.println("print slave: print slave information");
					continue;
				}
				else {
					if (args[1].equals("process")) {
						printProcess ();
					}
					else if (args[1].equals ("slave")) {
						printSlave ();
					}
					else {
						System.out.println("print type is wrong");
						continue;
					}
				}			
			} else {
				int slaveID = chooseBestSlave();
				String processName = args[0];
				String[] inputs = Arrays.copyOfRange(args, 1, args.length);
				                
				try {
					@SuppressWarnings("unchecked")
					Class<MigratableProcess> processClass = (Class<MigratableProcess>)(Class.forName("project1."+args[0]));
					Constructor<MigratableProcess> processConstructor = processClass.getConstructor(String[].class);
	                Object[] obj = new Object[1];
	                obj[0] = (Object[])inputs;
	                MigratableProcess newProcess = (MigratableProcess) processConstructor.newInstance(obj);
					ProcessInfo process = new ProcessInfo(newProcess, processId++, processName, slaveID, Status.INITIALIZING);
		            launchProcess(process);
		            
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		ProcessManager pm = ProcessManager.getPM();
		try {
			pm.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
