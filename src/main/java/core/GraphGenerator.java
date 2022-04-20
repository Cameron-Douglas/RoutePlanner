package core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	public static final String outPath = "out.gpx";
	
	private static double elevPrio;
	
	private static HashMap<String, ElevNode> nodeMap = new HashMap<String, ElevNode>();
	private static HashMap<String, List<Way>> wayMap = new HashMap<String, List<Way>>();
	
	private static Graph<ElevNode,Way> graph = new DirectedWeightedPseudograph<ElevNode, Way>(Way.class);

	public static void main(String[] args) {

		// Take in user input
		Scanner input = new Scanner(System.in);
		
		System.err.println("Enter Elevation Priority (0.1 minimum, 1 default, >1 high):");
		elevPrio = Double.parseDouble(input.next());
		
		buildGraph();
		
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
		
		input.close();
		
		if(endNode == null) {
			System.err.println("Unable to find node, please enter valid coordinates");
		}
		

		// Initialise graph path variable
		GraphPath<ElevNode,Way> path = DijkstraShortestPath.findPathBetween(graph, startNode, endNode);
		
		File outFile = new File(outPath);
		
		double elevationGain = 0;
		double distance = 0;
		
		// write to output file
		if(path != null) {
			try {
				FileWriter fw = new FileWriter(outFile, true);

				// GPX Formatting found at https://www.opencpn.org/OpenCPN/info/gpxvalidation.html
				fw.write("<gpx version=\"1.1\" creator=\"Rasbats\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www8.garmin.com/xmlschemas/GpxExtensionsv3.xsd\" xmlns:opencpn=\"http://www.opencpn.org\">\r\n" + 
						"<rte>\r\n" + 
						"<name>Route</name>");

				fw.close();
			} catch (IOException e) {
				System.err.println("Could not find out file");
				e.printStackTrace();
			}
			
			for(Way w : path.getEdgeList()) {

				// Calculate elevation gain
				ElevNode start = nodeMap.get(w.startNode);
				ElevNode end = nodeMap.get(w.endNode);
				if(Double.parseDouble(end.elevation) > Double.parseDouble(start.elevation)) {
					elevationGain += Double.parseDouble(end.elevation) - Double.parseDouble(start.elevation);
				}
				//System.out.println(distance(start,end) + "(" + start.latitude +", "+ start.longitude + ")" + "(" + end.latitude +", "+ end.longitude + ")" );
				distance += distance(start,end);
				//System.out.println(start.latitude + ", " +start.longitude);
				try {
					FileWriter fw = new FileWriter(outFile, true);
					fw.write("\n<rtept lat=\""+start.latitude+"\" lon=\""+start.longitude+"\">");
					fw.write("\n <extensions>\r\n" + 
							"<opencpn:viz>1</opencpn:viz>\r\n" + 
							"</extensions>\r\n" + 
							"</rtept>");
					fw.close();
				} catch (IOException e) {
					System.err.println("Could not find out file");
					e.printStackTrace();
				}
				
			}
			try {
				FileWriter fw = new FileWriter(outFile, true);
				// Close GPX file
				fw.write("\n</rte>" + "\n</gpx>");
				fw.close();
			} catch (IOException e) {
				System.err.println("Could not find out file");
				e.printStackTrace();
			}

			System.err.println("Total elevation gain: " + elevationGain + " metres");
			System.err.println("Total distance: " + distance + " kilometers");
		} else {
			System.err.println("No path found!");
		}

	}

	// ----- TEST ROUTE COORDINATES -----

	// Route 1
	// 55.930233735430306, -3.25487014394548
	// 55.91191826583644, -3.3138021216833526
	
	// Route 2
	// 55.90968534363319, -3.3203696047212112
	// 55.94492477407985, -3.188720965745217
	
	// Route 3
	// 55.9420617566527, -3.21667413047194
	// 55.950101351719375, -3.1880379750193537
	
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
			
			System.err.println("Building Graph...");
			
			for(ElevNode n : nodeMap.values()) {
				graph.addVertex(n);
			}
	
			for(List<Way> l : wayMap.values()) {
				for(Way w : l) {
					
					graph.addEdge(nodeMap.get(w.startNode), nodeMap.get(w.endNode), w);
					nodeMap.get(w.startNode).connected = true;
					nodeMap.get(w.endNode).connected = true;
					graph.setEdgeWeight(w, cost(nodeMap.get(w.startNode),nodeMap.get(w.endNode)));
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
		double n = 13 * elevPrio; //Naismith conversion value multiplied by the user's elevation priority
		
		
		if(endAlt > startAlt) {
			cost += n*elevChange;
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
