import java.util.*;
import java.io.*;

/**
 * 
 * @author JessicaG
 * The code is for PreFlowPush Algorithm
 * 1) The height of all the nodes except Source Node is 0
 * 2) The maximum flow at Source Node is assumed to be Maximum possible (like infinity)
 * 3) The PreflowPush function contains both Push and Relabel operation as a part of it.
 * 4) Used an additional parameter true and false, to distinguish between source/sink node and forward/backward edges.
 * 5) Takes the input file name as Argument
 */
public class PreFlowPushAlgo {
	static HashMap<String, Node> nodes = new HashMap<String, Node>();
	static Node s = null;
	static Node t = null;
	
	/**
	 * Main runner Class
	 **/
	public int runner(String fileName){
		clear(); //Clear Hashmap for next run
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    for(String line; (line = br.readLine()) != null; ) {  //Reading from input file
		        String[] vertices = line.trim().split("\\s+");
		        Node VertexS = getOrAddVertex(vertices[0]);
		        Node VertexE = getOrAddVertex(vertices[1]);
		        new Edge(VertexE,VertexS,Integer.parseInt(vertices[2]), false); // Call to create edge
		    }
	        br.close();
		} 
		catch (IOException e) {
			System.out.println("Problem in reading from the File");
		}
		s.height=nodes.size();
		//System.out.println("Total Vertices: "+nodes.size());
		PreflowPush(s, t);
		//System.err.printf("Flow pushed through the graph: %d\n", t.excess);
		return t.excess;
		
	}
	
	/**It will only add if Vertex is not already identified**/
	public static Node getOrAddVertex(String V) {
		if (nodes.containsKey(V)) {
			return nodes.get(V);
		}
		else {
			Node vertex = new Node(V);
			if(V.equals("s")){
				vertex.isSourceOrSink=true;
				vertex.excess=Integer.MAX_VALUE;
				//vertex.height=TVertices;
				s=vertex;
			}
			else if(V.equals("t")){
				vertex.isSourceOrSink=true;
    			t=vertex;
			}
			nodes.put(V, vertex);
			return vertex;
		}
	}
	/*
	 * PreflowPush algorithm
	 */
	public static void PreflowPush(Node s, Node t) {
		// source.height should be the number of nodes, sink.height should be 0
		LinkedList<Node> q = new LinkedList<Node>();
		q.add(s);//add the nodes with Pre-flow
		while (!q.isEmpty()) {
			Node node = q.remove();
			int minHeight = Integer.MAX_VALUE;
			for (int i = 0; i < node.neighbors.size(); i++) {
				/*
				 * This is to check if there is an edge with less height than the source node( node v as v to w)
				 */
				if (node.neighbors.get(i).dest.height < minHeight
						&& node.neighbors.get(i).remaining() > 0)
					minHeight = node.neighbors.get(i).dest.height;
			}
			/*
			 * Relabel operation
			 */
			if (minHeight != Integer.MAX_VALUE && minHeight >= node.height)
				node.height = minHeight + 1;
			for (int i = 0; i < node.neighbors.size(); i++) {
				if (node.neighbors.get(i).dest.height < node.height) {
					int pushedFlow = node.neighbors.get(i).remaining();
					if (pushedFlow > node.excess)
						pushedFlow = node.excess;
					/**
					 * Push Operation
					 * */
					if (pushedFlow > 0) {
						node.neighbors.get(i).flow += pushedFlow;
						node.neighbors.get(i).back.flow -= pushedFlow;
						node.neighbors.get(i).dest.excess += pushedFlow;
						node.excess -= pushedFlow;
						if (!node.neighbors.get(i).dest.isSourceOrSink)
							q.add(node.neighbors.get(i).dest);
						if (node.excess <= 0)
							break;
					}
				}
			}
			if (node.excess > 0 && !node.isSourceOrSink)
				q.add(node);
		}
	}
	/****
	 * Just to clean and Prepare the Hash Map for next run
	 */
	public static void clear(){
		nodes.clear();
		s=null;
		t=null;
	}
	/*
	 * The class file for Nodes
	 */
	static class Node {
		ArrayList<Edge> neighbors ;
		 int height;
		 int excess;
		 boolean isSourceOrSink;
		public Node(String v){
		neighbors = new ArrayList<Edge>();
		height = 0;
		excess = 0;
		isSourceOrSink = false;
		}
		public Node(){  //Specifically to handle Sink and Source Node
			neighbors = new ArrayList<Edge>();
			height = 1;
			excess = 0;
			isSourceOrSink = false;
			}
	}
	/*
	 * Handle Adjacent edges and flow, capacity values.
	 */
	class Edge {
		public Node dest;
		public Edge back;
		public int capacity;
		public int flow = 0;

		public Edge(Node dest, Node source, int capacity, boolean isBack) {
			this.dest = dest;
			this.capacity = capacity;
			source.neighbors.add(this);
			if (!isBack) {
				Edge backEdge = new Edge(source, this.dest, 0, true);
				this.back = backEdge;
				backEdge.back = this;
			}
		}

		public int remaining() {
			return capacity - flow;
		}
	}
	/**
	 *Main
	 **/
	// public static void main(String[] args) {
	// 	PreFlowPushAlgo pa = new PreFlowPushAlgo();
	// 	String filen = args[0];  //Manipulate the path of file as argument
	// 	long start = System.nanoTime();
	// 	pa.runner(filen);
	// 	double diff=(double) ((System.nanoTime() - start)/1000000000.0);
	// 	System.out.println("finished... "+diff);

	// }

}


