package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
	
	private static Logger instance = null;
	private FileWriter log = null ;
	private FileWriter errorLog = null;
	
	public enum MsgType {
		LOG, DEBUG
	};
	
	protected Logger() throws IOException {
		log = new FileWriter(new File("Transcript_Log.txt"), false);
		errorLog = new FileWriter(new File("IR_Debug_Log.txt"), false);
	}
	
	public static Logger getInstance() {
	
		if (instance == null) {
			try {
				instance = new Logger();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return instance;
	}
	
	public void write(String msg, MsgType type) {
		
		try {
			if (MsgType.LOG == type) {
				log.write(msg + '\n');
				log.flush();
			}
			else if (MsgType.DEBUG == type) {
				errorLog.write(msg + '\n');
				errorLog.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException {
		log.close();
		errorLog.close();
	}
}
