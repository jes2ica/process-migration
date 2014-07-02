package project1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ProcessHelper {
	
	public void writeProcess(MigratableProcess mp, int processId) {
		FileOutputStream outFile = null;
		ObjectOutputStream out = null;
		System.out.println("Begin writting...");
		try {
			outFile = new FileOutputStream("Process_"+processId, false);
			out = new ObjectOutputStream(outFile);
			out.writeObject(mp);
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				out.close();
				outFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finish writting...");
	}
	
	public MigratableProcess readProcess(int processId) {
		
		FileInputStream inFile = null;
		ObjectInputStream in = null;
		MigratableProcess mp = null;
		try {
			inFile = new FileInputStream("Process_"+processId);
			in = new ObjectInputStream(inFile);
			mp = (MigratableProcess) in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally{
			try{
				in.close();
				inFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mp;
	}
	
	@SuppressWarnings("unchecked")
	public static void main (String args[]) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, ClassNotFoundException {
		String[] inputs = Arrays.copyOfRange(args, 1, args.length);
		Class<MigratableProcess> processClass = (Class<MigratableProcess>)(Class.forName("project1."+args[0]));
		Constructor<MigratableProcess> processConstructor = processClass.getConstructor(String[].class);
        Object[] obj = new Object[1];
        obj[0] = (Object[])inputs;
        MigratableProcess newProcess = (MigratableProcess) processConstructor.newInstance(obj);
        ProcessHelper ph = new ProcessHelper();
        ph.writeProcess(newProcess,0);
        MigratableProcess mp = ph.readProcess(0);
        new Thread(mp).start();
	}

}
