package core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedPseudograph;

import core.MapGenerator.ElevNode;
import core.SAXPrinter.Way;

public class GraphGenerator {
	
	private static final String path = "nodes.csv";
	public static final String fileName = "way.osm";
	
	private final static double g = 9.80655;
	
	private static HashMap<String, ElevNode> nodeMap = new HashMap<String, ElevNode>();
	private static HashMap<String, List<Way>> wayMap = new HashMap<String, List<Way>>();
	
	private static double elevPrio = 100.0;
	
	private static Graph<ElevNode,Way> graph = new DirectedWeightedPseudograph<ElevNode, Way>(Way.class);

	public static void main(String[] args) {
		
		buildGraph();
		
		Scanner input = new Scanner(System.in);
		
		System.out.println("Enter Start Latitude:");
		String startX = input.next();
		
		System.out.println("Enter Start Longitude:");
		String startY = input.next();
		
		System.err.println("Finding nearest node...");
		ElevNode startNode = findNode(startX, startY);
		
		if(startNode == null) {
			System.err.println("Unable to find node, please enter valid coordinates");
		}
		
		System.out.println("Enter End Latitude:");
		String endX = input.next();
		
		System.out.println("Enter End Longitude:");
		String endY = input.next();
		
		System.err.println("Finding nearest node...");
		ElevNode endNode = findNode(endX, endY);
		
		if(endNode == null) {
			System.err.println("Unable to find node, please enter valid coordinates");
		}
		
		GraphPath<ElevNode,Way> path = DijkstraShortestPath.findPathBetween(graph, startNode, endNode);
		
		double elevationGain = 0;
		double distance = 0;
		
		if(path != null) {
			for(Way w : path.getEdgeList()) {
				ElevNode start = nodeMap.get(w.startNode);
				ElevNode end = nodeMap.get(w.endNode);
				if(Double.parseDouble(end.elevation) > Double.parseDouble(start.elevation)) {
					elevationGain += Double.parseDouble(end.elevation) - Double.parseDouble(start.elevation);
				}
				//System.out.println(distance(start,end) + "(" + start.latitude +", "+ start.longitude + ")" + "(" + end.latitude +", "+ end.longitude + ")" );
				distance += distance(start,end);
				//System.out.println(start.latitude + ", " +start.longitude);
			}
//			System.out.println(path.getEdgeList());
			System.err.println("Total elevation gain: " + elevationGain + " metres");
			System.err.println("Total distance: " + distance + " kilometers");
		} else {
			System.err.println("No path found!");
		}

	}
	
	// 55.930233735430306, -3.25487014394548
	// 55.91191826583644, -3.3138021216833526
	
	/**
	 * Function which initialises map variables then builds the graph using JGraphT graph library
	 */
	private static void buildGraph() {
		MapGenerator mapGen = new MapGenerator();
		WayReader wayGen = new WayReader();
		
		File file = new File(path);
		
		try {
			MapGenerator.read(file);
			WayReader.read(fileName);
			nodeMap = mapGen.getMap();
			wayMap = wayGen.getMap();
			
//			System.out.println(nodeMap.size());
//			System.out.println(wayMap.size());
			
			for(ElevNode n : nodeMap.values()) {
				graph.addVertex(n);
			}
	
			for(List<Way> l : wayMap.values()) {
				for(Way w : l) {
					
					graph.addEdge(nodeMap.get(w.startNode), nodeMap.get(w.endNode), w);
					nodeMap.get(w.startNode).connected = true;
					//graph.addEdge(nodeMap.get(w.endNode), nodeMap.get(w.startNode), w);
					nodeMap.get(w.endNode).connected = true;
					graph.setEdgeWeight(w, cost(nodeMap.get(w.startNode),nodeMap.get(w.endNode)));
					//graph.setEdgeWeight(w, cost(nodeMap.get(w.endNode),nodeMap.get(w.startNode)));
					//System.out.println("adding edge ");
				}
			}
			System.out.println("Graph Built");
		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}
		
	}
	
	/**
	 * @param start 
	 * @param end
	 * @return
	 * 
	 * Takes in two nodes and returns the distance between them in km, based on their latitude and longitude
	 * Using the Haversine formula: https://en.wikipedia.org/wiki/Haversine_formula
	 * Found at: https://gist.github.com/vananth22/888ed9a22105670e7a4092bdcf0d72e4
	 */
	
	private static double distance(ElevNode start, ElevNode end) {
		
		 final int R = 6371; // Radiuus of the earth
		 Double lat1 = Double.parseDouble(start.latitude);
		 Double lon1 = Double.parseDouble(start.longitude);
		 Double lat2 = Double.parseDouble(end.latitude);
		 Double lon2 = Double.parseDouble(end.longitude);
		 
		 Double latDistance = toRad(lat2-lat1);
		 Double lonDistance = toRad(lon2-lon1);
		 
		 Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
		 Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * 
		 Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		 
		 Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		 
		 Double distance = R * c;
				
		 return distance;
		
	}
	 // https://gist.github.com/vananth22/888ed9a22105670e7a4092bdcf0d72e4
	 private static Double toRad(Double value) {
		 return value * Math.PI / 180;
	 }
	
	/**
	 * 
	 * @param start
	 * @param end
	 * @return
	 * 
	 * Basic costing function to return the weight of each edge of the graph.
	 * Takes in two node, calculates the distance between them using distance() and then the elevation gain over the way
	 * Then calculate the force exerted to overcome gravity, using formula from: https://www.omnicalculator.com/sports/cycling-wattage#component-1-gravity 
	 * If there is an elevation gain, cost = distance * gravForce, else cost = distance.
	 * gravForce is multiplied my elevPrio which is a double which determines how much the user cares about hills (0 = not at all, 1 = default, >1 = more care)
	 */
	private static double cost(ElevNode start, ElevNode end) {
		double distance = distance(start,end);
		
		double cost = 0;
		
		double startAlt = Double.parseDouble(start.elevation);
		double endAlt = Double.parseDouble(end.elevation);
		
		double elevChange = endAlt - startAlt;
		double grad = elevChange/distance * 100;
		double gravForce = g * Math.sin(Math.atan(grad));
		
		if(endAlt > startAlt) {
			//cost += distance * (gravForce * (2* elevPrio));
			if(0.0 < grad && grad < 1.0) {
				cost = distance / grad;
			} else {
				cost = distance * grad;
			}
		} else {
			cost += distance;
		}
		
		return cost;
	}
	
	private static ElevNode findNode(String x, String y) {
		
		double xCoord = Double.parseDouble(x);
		double yCoord = Double.parseDouble(y);
		
		double bestDistance = Double.MAX_VALUE;
		ElevNode currNode = null;
		
		for(ElevNode n : nodeMap.values()) {
			
			if(!n.connected) {
				continue;
			}
			
			double xDist = xCoord - Double.parseDouble(n.latitude);
			double yDist = yCoord - Double.parseDouble(n.longitude);
			
			double distance = Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist,2));
			
			if(distance < bestDistance) {
				bestDistance = distance;
				currNode = n;
			}
		}
		
		return currNode;
		
	}

}
