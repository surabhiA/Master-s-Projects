import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;


public class ScalingMaxFlow {
	SimpleGraph G;
	SimpleGraph Gf;
	LinkedList<Vertex> path;
	LinkedList<Edge> pathEdges;
	Double maxFlow;
	Hashtable<Edge,Edge> ht;
	Hashtable<Edge,Edge> fwdBack;
	double bottleneck;
	double delta;
	
	public ScalingMaxFlow(){
		path = new LinkedList<Vertex>();
		pathEdges = new LinkedList<Edge>();
		maxFlow = 0.0;
		ht = new Hashtable<Edge,Edge>();
		fwdBack = new Hashtable<Edge,Edge>();
		bottleneck = 0.0;
		delta = 0.0;
	}
	
	/*
	 *Updates fwd and back edges of Gf involved in augmenting path */
	
	public void updateResidualGraph(){
		Edge ef;
		Edge efback;
	
		for(int i = 0; i < pathEdges.size(); i++){
			ef = pathEdges.get(i);
			ef.setData((Double) ef.getData() - bottleneck);
			efback = fwdBack.get(ef);
			efback.setData((Double) efback.getData() + bottleneck);
		}
	}
	
	/*
	 * Finds bottleneck in augmenting path*/
	
	public double findBottleneck(){
		
		double minCapacity = (Double) pathEdges.get(0).getData();
		double newCapacity;
		for(int i = 1; i < pathEdges.size(); i++){
			newCapacity = (Double) pathEdges.get(i).getData();
			if (minCapacity > newCapacity){
				minCapacity = newCapacity;
			}
		}
		return minCapacity;
	}
	
	/*
	 * Finds bottleneck in augmenting path
	 * Updates maxflow
	 * Updates flows on edges in G*/
	
	public void augment(){
		bottleneck = findBottleneck(); 
		maxFlow = maxFlow + bottleneck;
		
		Edge ef;
		Edge e;
		
		for(int i = 0; i < pathEdges.size(); i++){
			ef = pathEdges.get(i);
			
			if((Double)ef.getFlow() == 1.0){
				//forward edge
				e = ht.get(ef);  //find corresponding edge in G
				e.setFlow((Double) e.getFlow() + bottleneck);
			}
			else if((Double) ef.getFlow() == -1.0){
				//backward edge
				e = ht.get(ef); //find corresponding edge in G
				e.setFlow((Double) e.getFlow() - bottleneck);
			}
		}
	}
	
	/*
	 * Resets information atored in residual graph about augmenting path
	 * Marks all nodes unvisited
	 * Clears list which stores augmenting path
	 * Clears list which stores edges in augmenting path  */
	
	public void resetResidualGraph() {
		for (Iterator i = Gf.vertices(); i.hasNext(); ) {
			Vertex v = (Vertex) i.next();
			v.setData(false);
		}
		path.clear();
		pathEdges.clear();
	}
	
	/*
	 * Finds augmenting path using BFS*/
	
	public void findAugmentingPath(){
		resetResidualGraph();
		Vertex v1;
		Vertex v2;
		Edge e;
		Hashtable<Vertex,Vertex> prevNode = new Hashtable<Vertex,Vertex>();
		Hashtable<Vertex,Edge> vertexEdge = new Hashtable<Vertex,Edge>();
		LinkedList<Vertex> queue = new LinkedList<Vertex>();
		boolean pathFound = false;
		
		v1 = (Vertex) Gf.vertexList.getFirst();
		queue.addFirst(v1); //add to front of queue
		v1.setData(true); //mark visited
		
		while(!queue.isEmpty() && !pathFound){  //while queue is not empty and while no path to sink
			v1 = queue.remove();
			for(int i = 0; i < v1.incidentEdgeList.size(); i++){
				e = (Edge) v1.incidentEdgeList.get(i);
				v2 = Gf.opposite(v1, e);
				
				if(v2.getData().equals(true) || (Double) e.getData() == 0.0 || (Double) e.getData() < delta){
					//node visited or residual capacity 0 or capacity less than delta: don't select node
					continue;
				}
				else{
					//node selected
					queue.addLast(v2);
					v2.setData(true);  //mark visited
					prevNode.put(v2, v1); // mark its previous node
					vertexEdge.put(v2, e); //mark edge leading to previous node
					if (v2.getName().equals("t")) {
						//if t reached,exit and find augmenting path;
						pathFound = true;
						break;
					}
				}
				
			}
		}
		
		if(queue.isEmpty()){
			//no augmenting path
			return;
		}
		else{
			//retrace augmenting path
			v2 = queue.getLast();
			while(!v2.getName().equals("s")){
				path.addFirst(v2); //add nodes to beginning of list
				v1 = prevNode.get(v2); //find previous node
				pathEdges.addFirst(vertexEdge.get(v2)); //find edge leading to previous node
				v2 = v1;
				if(v2.getName().equals("s")){
					path.addFirst(v2); //add s to augmenting path
				}
			}
			return;
		}
	}
	
//	public void findAugmentingPathDFS(){
//		resetResidualGraph();
//		Vertex v1;
//		Vertex v2;
//		Edge e;
//
//		v1 = (Vertex) Gf.vertexList.getFirst();
//		path.push(v1);
//		
//		while(!path.getLast().getName().equals("t")){
//			v1 = path.getLast();
//			v1.setData(true);
//			
//			boolean pathFound = false;
//			for(int i = 0; i < v1.incidentEdgeList.size(); i++){
//				
//				e = (Edge) v1.incidentEdgeList.get(i);
//				v2 = Gf.opposite(v1, e);
//				if(v2.getData().equals(true) || (Double) e.getData() == 0.0 || (Double) e.getData() < delta){
//					//node visited or residual capacity 0
//					continue;
//				}
//				else{
//					//path found
//					pathEdges.add(e);
//					v2.setData(true);
//					path.addLast(v2);
//					pathFound = true;
//					break;
//				}
//			}
//			
//			if(pathFound == false){
//				//no path exists
//				path.removeLast();
//				if(path.isEmpty()){
//					return;
//				}
//				pathEdges.removeLast();
//			}
//			
//		}
//	
//	}
	
	/*
	 * Sets delta to highest power of 2, such that max capacity of any edge out of s is greater than delta*/
	
	public void setDelta(){
		Edge e;
		double capacity;
		Vertex s = (Vertex) G.vertexList.getFirst();
		e = (Edge) s.incidentEdgeList.get(0);
		delta = (Double) e.getData();
		for(int i = 1; i < s.incidentEdgeList.size(); i++){
			e = (Edge) s.incidentEdgeList.get(i);
			capacity = (Double) e.getData();
			if(delta < capacity){
				delta = capacity;
			}
		}
		
		double power = 1.0;
		while(power <= delta)
		    power*=2;
		
		
		delta = power/2;
	}
	
	/*
	 * Sets Delta for choosing good augmenting paths
	 * Finds aug path
	 * Updates flows of G
	 * Updates Gf */
	
	public int findMaxFlow(){
		setDelta();
		
		while(delta >= 1.0){
			findAugmentingPath();
			
			while(!path.isEmpty()){
				augment();
				updateResidualGraph();
				
				findAugmentingPath();
			}
			delta = delta/2;
		}

		return maxFlow.intValue();
	}
	
	
	/*
	 * Initializes the residual graph with forward and backward edges*/
	
	public void initializeGf(){
		Hashtable<Vertex, Vertex> t = new Hashtable<Vertex, Vertex>();
		Gf = new SimpleGraph();
		Iterator i;
		Vertex v, newV;
		Vertex v1, v2;
		Edge e;
		Edge ef;
		Edge efback;
		
		for (i= G.vertices(); i.hasNext(); ) {
            v = (Vertex) i.next();
            newV = Gf.insertVertex(false, v.getName()); //insert vertices in Gf
            t.put(v, newV); //correspondence between vertices of G and Gf
        }
		for (i= G.edges(); i.hasNext(); ) {
            e = (Edge) i.next();
            v1 = e.getFirstEndpoint();
            v2 = e.getSecondEndpoint();
            ef = Gf.insertEdge(t.get(v1), t.get(v2), e.getData(), 1.0); //Insert fwd edge
            ht.put(ef, e); //Correspondence between forward edges of Gf and G 
            efback = Gf.insertEdge(t.get(v2), t.get(v1), 0.0, -1.0); //Insert backward edge
            ht.put(efback, e); //Correspondence between back edges of Gf and fwd edges of G 
            fwdBack.put(ef, efback); //Correspondence between fwd and back edges of Gf
            fwdBack.put(efback, ef); //Correspondence between fwd and back edges of Gf
        }
	} 
	
	/*
	 * This function initializes the graph from the given filename
	 * Initializes the residual graph
	 * Calls the function which gives maxflow*/
	
	public int run(String filename){
		this.G = new SimpleGraph();
		GraphInput input = new GraphInput();
		input.LoadSimpleGraph(this.G, filename);
		
		this.initializeGf();
		return this.findMaxFlow();
	}

//	public static void main(String[] args) {
//		ScalingMaxFlow scmx = new ScalingMaxFlow();
//		scmx.G = new SimpleGraph();
//		GraphInput input = new GraphInput();
//		Hashtable table = new Hashtable();
//		table = input.LoadSimpleGraph(scmx.G, "/Users/surabhi/Desktop/AlgoProject/graphGenerationCode/FixedDegree/100v-5out-25min-200max.txt");
//		
//		scmx.initializeGf();
//		scmx.findMaxFlow();
//		
//	}

}
