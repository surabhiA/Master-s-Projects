import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/*
 * @author Swati Garg
 * This class implement FordFulkerson algorithm to compute max flow
 */
public class FordFulkerson {	
	final int numRun = 3; // number of runs to measure the time taken
	double timeAverageCurrRun = 0.0; // stores the average run time in milliseconds
	
	// stores the data needed to run the algorithm.
	int[][] capacities;
	int source;
	int sink;
	
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
		HashMap<String, Integer> vertexMap = new HashMap<String, Integer>();
		
		// read the graph to compute number of vertices.
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			int currIndex = 0;
			while((line = reader.readLine()) != null)
			{
				String[] vertices = line.trim().split("\\s+");
				assert(vertices.length == 3);
				if (!vertexMap.containsKey(vertices[0])) {
					vertexMap.put(vertices[0], currIndex);
					if (vertices[0].equalsIgnoreCase("s")) {
						source = currIndex;
					}
					if (vertices[0].equalsIgnoreCase("t")) {
						sink = currIndex;
					}
					currIndex++;
				}
				if (!vertexMap.containsKey(vertices[1])) {
					vertexMap.put(vertices[1], currIndex);
					if (vertices[1].equalsIgnoreCase("s")) {
						source = currIndex;
					}
					if (vertices[1].equalsIgnoreCase("t")) {
						sink = currIndex;
					}
					currIndex++;
				}
			}
			reader.close();
		} catch (IOException e) {
			throw e;
		}
	
		// read in the capacities
		int countVertices = vertexMap.size();
		capacities = new int[countVertices][countVertices];
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while((line = reader.readLine()) != null)
			{
				String[] vertices = line.trim().split("\\s+");
				assert(vertices.length == 3);
				capacities[vertexMap.get(vertices[0])][vertexMap.get(vertices[1])] = Integer.parseInt(vertices[2]);
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
		if (capacities.length < 1)
			return 0;
	
		int maxflow = 0;
		int[][] flows = new int[capacities.length][capacities.length];
		int[][] residualGraph = new int[capacities.length][capacities.length];
		int[] path = new int[capacities.length];
	
		// initialize the flows and residual graph
		for(int i = 0; i < capacities.length; i++) {
			for(int j = 0; j < capacities.length; j++) {
				residualGraph[i][j] = capacities[i][j];
				flows[i][j] = 0;
			}
		}	
		
		while(findPath(residualGraph, path)) {
			assert(path[0] == source);
			int bottleneck = findBottleneck(residualGraph, path);			
			maxflow = maxflow + bottleneck;
			for(int i = 0 ; i < capacities.length - 1 && path[i] != sink; i++) {
				flows[path[i]][path[i+1]] += bottleneck;
				residualGraph[path[i]][path[i+1]] -= bottleneck;
				residualGraph[path[i+1]][path[i]] += bottleneck;
			}						
		}
		
		return maxflow;
	}
	
	/*
	 * Finds the bottleneck in the path provided in the residual graph
	 */
	private int findBottleneck(int[][] residualGraph, int[] path) {
		int bottleneck = residualGraph[path[0]][path[1]];
		for(int i = 0 ; i < residualGraph.length - 1 && path[i] != sink; i++) {
			bottleneck = bottleneck < residualGraph[path[i]][path[i+1]] ? bottleneck : residualGraph[path[i]][path[i+1]];
		}
		return bottleneck;
	}

	/*
	 * Find the path in the residual graph from source to sink, if one exists
	 * the path array contains the list of vertices that make the path
	 * The path is found by using Breadth First Search on the residual graph
	 */
	private boolean findPath(int[][] residualGraph, int[] path) {
		boolean fPathExists = false;
		
		int[] vertexVisited = new int[residualGraph.length];
		Arrays.fill(vertexVisited, -2);
		LinkedList<Integer> queue = new LinkedList<Integer>();
		
		queue.addLast(source);
		vertexVisited[source] = -1;
		while(!queue.isEmpty()) {
			int vertex = queue.removeFirst();
			if (vertex == sink) {
				fPathExists = true;
				break;
			}
			for (int i = 0 ; i < residualGraph.length ; i++) {
				if (residualGraph[vertex][i] > 0 && vertexVisited[i] == -2) {
					vertexVisited[i] = vertex;;
					queue.addLast(i);
				}
			}
		}
		
		if (fPathExists) {
			// get the path and store it in the passed in array
			int pathLength = 1;
			int currentVertex = sink;
			while (vertexVisited[currentVertex] >= 0) {
				pathLength++;
				currentVertex = vertexVisited[currentVertex];
			}
			currentVertex = sink;
			for (int i = pathLength - 1; i > 0 ; i--) {
				path[i] = currentVertex;
				currentVertex = vertexVisited[currentVertex];
			}
			assert(currentVertex == source);
			path[0] = source;
		}
		
		return fPathExists;
	}
}
