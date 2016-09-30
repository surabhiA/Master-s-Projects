import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class logs all information for the client and server in their respective log files
 * @author Surabhi Agrawal
 */

public class Logger {

	SimpleDateFormat timestamp;
	String query;
	FileWriter fw;
	String time;

	public Logger(String filename) throws IOException {
		timestamp = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss:SSS");
		fw = new FileWriter(filename,true);
	}
	
	/**
	 * The add() function logs each query and response received at the server into the server log 
	 * 
	 * @param ClientIP : This is the IP address of the client from whom the query was received
	 * @param Clientport : This is the port number of the client from whom the query was received
	 * @param query : This is the actual query received from client
	 * @param response : This is the response sent by the server to the client
	 */
	
	public void add(InetAddress ClientIP, int Clientport, KVRequest query, KVResponse response) throws IOException{
		
		time = timestamp.format(new Date());
		String instr = query.getInstruction();
		String key = query.getKey();
		String value = query.getValue();
		if(value == null){
			this.query = instr + "(" + key + ")";
		}
		else{
			this.query = instr + "(" + key + ", " + value + ")";
		}
		
	    fw.write("[" + time + "] " + ClientIP + ": " + Clientport + "\n");
	    fw.write("Query: " + this.query + "\n");
	    fw.write("Response: " + response.toString() + "\n\n");
	    fw.flush();
	}
	
	/**
	 * The addError() function adds the error thrown by client and server to their log files
	 * @param ClientIP : The IP address of the system that sent the malformed request/response
	 * @param Clientport : The port number of the system that sent the malformed request/response
	 * @param errorMessage : The error thrown is sent as an error message
	 */
	
	public void addError(InetAddress ClientIP, int Clientport, String errorMessage){
		time = timestamp.format(new Date());
		try{
			fw.write("[" + time + "] " + "\n");
		    fw.write(errorMessage + " from " + ClientIP + " : " + Clientport + "\n\n");
		    fw.flush();
		} catch(IOException e) {
			System.out.println("IO Exception occured " + e.getMessage());
		}
		
	}
	
	/**
	 * This function logs the TimeoutException error for the client when the server is unresponsive
	 */
	
	public void addError(KVRequest m , String errorMessage) {
		
		time = timestamp.format(new Date());
		String instr = m.getInstruction();
		String key = m.getKey();
		String value = m.getValue();
		
		if(value == null){
			this.query = instr + "(" + key + ")";
		}
		else{
			this.query = instr + "(" + key + ", " + value + ")";
		}
		
		try{
			fw.write("[" + time + "]" + "\n");
		    fw.write("Query: " + this.query + "\n");
		    fw.write("Response: " + errorMessage + "\n\n");
			fw.flush();
		} catch(IOException e) {
			System.out.println("IO Exception occured " + e.getMessage());
		}
	}
	

	
	public void close(){
		try{
			fw.close();
		} catch(IOException e) {
			
		}
		
	}
	
}
