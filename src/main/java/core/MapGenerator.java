package core;


import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import com.opencsv.*;

import core.SAXPrinter.Node;

public class MapGenerator {
	
	private static HashMap<String, ElevNode> nodeMap = new HashMap<String, ElevNode>();
	
	
	// Read the file parameter and parse each line into an ElevNode which is then put in the node map
	public static void read(File file) throws Exception {
		
		System.err.println("Reading Nodes CSV...");
		
		FileReader filereader = new FileReader(file);
		
		CSVReader csvReader = new CSVReader(filereader);
		
		String[] nextRecord;
		
		while((nextRecord = csvReader.readNext()) != null) {
			
			ElevNode newNode = new ElevNode(nextRecord[0], nextRecord[1], nextRecord[2], nextRecord[3]);
			
			nodeMap.put(nextRecord[0], newNode);
		}
		
		csvReader.close();
		
	}
	
	public HashMap<String, ElevNode> getMap(){
		
		return nodeMap;
	}
	
	 static class ElevNode {
			public final String id, latitude, longitude, elevation;
			public boolean connected;
			private ElevNode(String id, String lat, String lon, String ele) {
				this.latitude = lat;
				this.longitude = lon;
				this.id = id;
				this.elevation = ele;
				this.connected = false;
			}
			@Override
			public boolean equals(Object other) {
				
				return ((ElevNode)other).id.equals(this.id);
				
			}
			@Override
			public int hashCode() {
				
				return this.id.hashCode();
				
			}
	
	
	}

}
