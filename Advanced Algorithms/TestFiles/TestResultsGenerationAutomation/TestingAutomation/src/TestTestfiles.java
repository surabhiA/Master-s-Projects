import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;  //for unique vertices
import NewTest.*;
/**
 * 
 * @author Jessica
 *Ucnomment some sections Metioned with UNCOMMENT and change the name appropiately
 */

public class TestTestfiles {
	/**Create other object here..**/
	/***
	 * Script to run the test Scripts one by one and Write the output in file
	 */
	/**Getting total number of edges and vertices**/
	public String[] Otherinfo(String file) throws IOException{
		int TotalEdges=0;
		String[] a = new String[2];
		HashSet<String> Uvertices = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		 for(String line; (line = br.readLine()) != null; ) {  //Reading from input file
		    	TotalEdges++;
		    	String[] vertices = line.trim().split("\\s+");
		        //System.out.println(vertices.length);
		        for (int i=0;i<vertices.length-1;i++){
		    	Uvertices.add(vertices[i]); // to get the unique nodes only
		        }
		 }
		 br.close();
		 a[0]=Uvertices.size()+"";
		 a[1]=TotalEdges+"";
		return a;
	}
	public void ReadFile(){
		int count=0;
		try{
		File out= new File("results3.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.append("FileName,Vertices,MaxFlow,TimeInSecsPrePush,TimeInSecsScale,TimeInSecsFordalt,TimeInSecsFordFulkerson");
		File folder = new File("TestFiles2/");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	String fileN="TestFiles2/"+file.getName();
		    	System.out.println(fileN);
		    	String Pre=fileN+","+ExecuteAlgoPre(fileN);
		    	String Scale =ExecuteAlgoScale(fileN);
		    	String fordAlt =ExecuteAlgofordAlt(fileN);
		    	String ford =ExecuteAlgoford(fileN);
		    	bw.append("\n"+Pre+","+Scale+","+fordAlt+","+ford);
		    	count++;
		    }
		    System.out.println(count);
		}
		bw.close();
		}
		catch(IOException e){
			System.err.println("Close the file");
		}
	}
	public String ExecuteAlgoford (String fileName) throws IOException{
		//String[] Totals= Otherinfo(fileName);
		
		double[] diff = new double[3];
		int[] maxFlow = new int[3];
		int maxFlowF =0;
		double time=0.0;
		String timeNalgo = "";
			for(int i=0;i<3;i++){
				//clear();
				long start = System.nanoTime();
				//PreFlowPush pa = new PreFlowPush();
				FordFulkerson pa = new FordFulkerson();
				maxFlow[i]=pa.run(fileName);
				//System.out.println("---------------------");
				diff[i]=(double) (System.nanoTime() - start)/1000000.0;
				time=time+diff[i];
				maxFlowF=maxFlowF+maxFlow[i];
				
			}
			timeNalgo=String.format( "%.3f",time/3);
		return(timeNalgo);
		}
	/**
	 * @param fileName
	 * @return the total number of Edges, Vertices and Time to Execute the InputFile
	 * @throws IOException 
	 */
	
	public String ExecuteAlgoPre (String fileName) throws IOException{
		String[] Totals= Otherinfo(fileName);
		double[] diff = new double[3];
		int[] maxFlow = new int[3];
		int maxFlowF =0;
		double time=0.0;
		String timeNalgo = "";
			for(int i=0;i<3;i++){
				//clear();
				long start = System.nanoTime();
				//PreFlowPush pa = new PreFlowPush();
				PreFlowPush pa = new PreFlowPush();
				maxFlow[i]=pa.runner(fileName);
				//System.out.println("---------------------");
				diff[i]=(double) (System.nanoTime() - start)/1000000.0;
				time=time+diff[i];
				maxFlowF=maxFlowF+maxFlow[i];
				
			}
			timeNalgo=String.format( "%.3f",time/3);
		return(Totals[0]+ ","+maxFlowF/3+","+timeNalgo);
		}
	public String ExecuteAlgoScale (String fileName) throws IOException{
		//String[] Totals= Otherinfo(fileName);
		
		double[] diff = new double[3];
		int[] maxFlow = new int[3];
		int maxFlowF =0;
		double time=0.0;
		String timeNalgo = "";
			for(int i=0;i<3;i++){
				//clear();
				long start = System.nanoTime();
				//PreFlowPush pa = new PreFlowPush();
				ScalingMaxFlow pa = new ScalingMaxFlow();
				maxFlow[i]=pa.run(fileName);
				//System.out.println("---------------------");
				diff[i]=(double) (System.nanoTime() - start)/1000000.0;
				time=time+diff[i];
				maxFlowF=maxFlowF+maxFlow[i];
				
			}
			timeNalgo=String.format( "%.3f",time/3);
		return(timeNalgo);
		}
	public String ExecuteAlgofordAlt (String fileName) throws IOException{
		//String[] Totals= Otherinfo(fileName);
		double[] diff = new double[3];
		int[] maxFlow = new int[3];
		int maxFlowF =0;
		double time=0.0;
		String timeNalgo = "";
			for(int i=0;i<3;i++){
				//clear();
				long start = System.nanoTime();
				//PreFlowPush pa = new PreFlowPush();
				FordFulkersonAlt pa = new FordFulkersonAlt();
				maxFlow[i]=pa.run(fileName);
				//System.out.println("---------------------");
				diff[i]=(double) (System.nanoTime() - start)/1000000.0;
				time=time+diff[i];
				maxFlowF=maxFlowF+maxFlow[i];
				
			}
			timeNalgo=String.format( "%.3f",time/3);
		return(timeNalgo);
		}
	/**
	 * 
	 * @param args
	 * Main function
	 */
	public static void main(String args[]){
		TestTestfiles tf = new TestTestfiles();
		tf.ReadFile();
		System.out.println("--------------Finished Wrirting---------------");
	}
}
