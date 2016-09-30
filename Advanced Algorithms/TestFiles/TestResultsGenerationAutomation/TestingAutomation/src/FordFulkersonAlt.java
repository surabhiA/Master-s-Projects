import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/*
 * @author Swati Garg
 * This class implement FordFulkerson algorithm to compute max flow
 */
public class FordFulkersonAlt {			
	private class Vertex {
		public Vertex(String idVertex) {
			id = idVertex;
			peers = new ArrayList<Vertex>();
		}
		String id;
		List<Vertex> peers;
	}
	
	private class Edge {
		public Edge(String key, Vertex vertexStart, Vertex vertexEnd, int flow, int capacity) {
			this.keyID = key;
			this.vertexStart = vertexStart;
			this.vertexEnd = vertexEnd;
			this.flow = flow;
			this.capacity = capacity;
		}
		String keyID;
		Vertex vertexStart;
		Vertex vertexEnd;
		int flow;
		int capacity;
	}
	
	// data structures to store the graph
	HashMap<String, Vertex> vertices;
	HashMap<String, Edge> edges;
	HashMap<String, Edge> residualGraphEdges;
	HashMap<String, Vertex> residualGraphVertices;

	String sourceID = "s";
	String sinkID = "t";
	
	FordFulkersonAlt() {
		vertices = new HashMap<String, Vertex>();
		edges = new HashMap<String, Edge>();
		residualGraphVertices = new HashMap<String, Vertex>();
		residualGraphEdges = new HashMap<String, Edge>();
	}
	
	private Vertex getOrAddVertex(HashMap<String, Vertex> vertices, String idVertex) {
		if (vertices.containsKey(idVertex)) {
			return vertices.get(idVertex);
		}
		else {
			Vertex vertex = new Vertex(idVertex);
			vertices.put(idVertex, vertex);
			return vertex;
		}
	}
	
	private String getEdgeKey(String idVertexStart, String idVertexEnd) {
		return idVertexStart + ":" + idVertexEnd;
	}
	
	private Edge addEdge(String idVertexStart, String idVertexEnd, int capacity) {
		String key = getEdgeKey(idVertexStart, idVertexEnd);
		if (!edges.containsKey(key)) {
			Vertex vertexStart = getOrAddVertex(vertices, idVertexStart);
			Vertex vertexEnd = getOrAddVertex(vertices, idVertexEnd);		
			Edge newEdge = new Edge(key, vertexStart, vertexEnd, 0, capacity);
			vertexStart.peers.add(vertexEnd);
			edges.put(key, newEdge);
			return newEdge;
		}
		return null;
	}
	
	private void addEdgeResidual(Edge edge) {
		String keyForward = edge.keyID;
		String keyReverse = getEdgeKey(edge.vertexEnd.id, edge.vertexStart.id);
		Vertex vertexStart = getOrAddVertex(residualGraphVertices, edge.vertexStart.id);
		Vertex vertexEnd = getOrAddVertex(residualGraphVertices, edge.vertexEnd.id);		

		if (!residualGraphEdges.containsKey(keyForward)) {
			Edge newEdge = new Edge(keyForward, vertexStart, vertexEnd, 0, edge.capacity);
			vertexStart.peers.add(vertexEnd);
			residualGraphEdges.put(keyForward, newEdge);
		} else {
			Edge newEdge = residualGraphEdges.get(keyForward);
			newEdge.capacity += edge.capacity;
		}
		
		if (!residualGraphEdges.containsKey(keyReverse)) {
			Edge newEdge = new Edge(keyReverse, vertexEnd, vertexStart, 0, 0);
			vertexEnd.peers.add(vertexStart);
			residualGraphEdges.put(keyReverse, newEdge);
		}
	}
	
	/*
	 * Entry function to load the graph from the file provided 
	 * Runs the algorithm and returns the max flow
	 * Also prints out the time takes
	 */
	public int run(String filename) throws IOException {
		loadFile(filename);
		return maxFlow();
	}
		
	/*
	 * Function to load the file
	 * The file format is (for each edge in the graph)
	 * 		node	node	capacity
	 *
	 * It read the file twice, once to compute number of nodes
	 * and again to read in the capacities
	 * 
	 * s denotes the source
	 * t denotes the sink
	 */
	private void loadFile(String filename) throws IOException {
		// read the graph to compute number of vertices.
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while((line = reader.readLine()) != null)
			{
				String[] vertices = line.trim().split("\\s+");
				assert(vertices.length == 3);
				Edge edge = addEdge(vertices[0], vertices[1], Integer.parseInt(vertices[2]));
				if (edge != null)
					addEdgeResidual(edge);
			}
			reader.close();
		} catch (IOException e) {
			throw e;
		}
	}
	
	/*
	 * Implementation of the Ford-Fulkerson algorithm to find the max flow
	 * returns the max flow.
	 * capacities is a 2D array s.t. capacities[i][j] is the capacity of edge from i to j
	 * if there is no edge it should be 0
	 */
	private int maxFlow() {
		if (vertices.size() < 1)
			return 0;
	
		int maxflow = 0;
		ArrayList<Edge> path;
		
		while((path = findPath()) != null && !path.isEmpty()) {
			int bottleneck = findBottleneck(path);			
			maxflow = maxflow + bottleneck;
			for(Edge e : path) {
				String keyForward = e.keyID;
				String keyReverse = getEdgeKey(e.vertexEnd.id, e.vertexStart.id);
				if (edges.containsKey(keyForward))
					edges.get(keyForward).flow += bottleneck;
				else
					edges.get(keyReverse).flow -= bottleneck;
				residualGraphEdges.get(keyForward).capacity -= bottleneck;
				residualGraphEdges.get(keyReverse).capacity += bottleneck;
			}						
		}
		
		return maxflow;
	}
		
		/*
		 * Finds the bottleneck in the path provided in the residual graph
		 */
		private int findBottleneck(ArrayList<Edge> path) {
			int bottleneck = path.get(0).capacity;
			for(Edge e : path) {
				bottleneck = bottleneck < e.capacity ? bottleneck : e.capacity;
			}
			return bottleneck;
		}

		/*
		 * Find the path in the residual graph from source to sink, if one exists
		 * the path array contains the list of vertices that make the path
		 * The path is found by using Breadth First Search on the residual graph
		 */
		private ArrayList<Edge> findPath() {
			boolean fPathExists = false;
			ArrayList<Edge> path = new ArrayList<Edge>();
			
			LinkedList<Vertex> queue = new LinkedList<Vertex>();
			HashMap<String, String> reverseEdges = new HashMap<String, String>();
			Vertex sink = residualGraphVertices.get(sinkID);
			
			queue.addLast(residualGraphVertices.get(sourceID));			
			while(!queue.isEmpty()) {
				Vertex vertexCurr = queue.removeFirst();
				if (vertexCurr == sink) {
					fPathExists = true;
					break;
				}
				for (Vertex v : vertexCurr.peers) {					
					if (!v.id.equals(sourceID) && !reverseEdges.containsKey(v.id) && residualGraphEdges.get(getEdgeKey(vertexCurr.id, v.id)).capacity > 0 ) {
						reverseEdges.put(v.id, vertexCurr.id);
						queue.addLast(v);
					}
				}
			}
			
			if (fPathExists) {
				String vCurrID = sinkID;
				while(reverseEdges.containsKey(vCurrID)) {
					String vNextID = reverseEdges.get(vCurrID);
					path.add(residualGraphEdges.get(getEdgeKey(vNextID, vCurrID)));
					vCurrID = vNextID;
				}
			}
			
			return path;
		}

}
