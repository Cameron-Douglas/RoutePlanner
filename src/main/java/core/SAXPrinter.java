package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import core.MapGenerator.ElevNode;


public class SAXPrinter extends DefaultHandler {

	//Sax Parser tutorial found at https://mkyong.com/java/how-to-read-xml-file-in-java-sax-parser/

	private StringBuilder currentValue = new StringBuilder();
	
	private Set<Node> nodeSet = new HashSet<Node>();
	private HashMap<String, List<Way>> wayMap = new HashMap<String, List<Way>>();
	String lastWay = "";
	String startNode = "";
    List<Way> nodeList = new ArrayList<Way>();
    
     public Set<Node> getSet(){
		return this.nodeSet;
    	 
     }
     public HashMap<String, List<Way>> getMap(){
 		return this.wayMap;
     	 
     }

	  @Override
	  public void startElement(
	          String uri,
	          String localName,
	          String qName,
	          Attributes attributes) {

	      if(qName.equals("node")) {
	    	  
	    	  
	    	  Node newNode = new Node(attributes.getValue("id"), attributes.getValue("lat"), attributes.getValue("lon"));
    	  
	    	  nodeSet.add(newNode);
	      }
	      
	      

		  // Logic for adding ways to wayMap

	      if(qName.contentEquals("way")) {
	    	  
	    	  List<Way> list = new ArrayList<Way>();
	    	  
	    	  if(!nodeList.isEmpty()) {
	    		  for(Way w :nodeList) {
	    			  list.add(w);
	    		  }
	    	  }
	    	  
	    	  if(!lastWay.equals("")) {
	    		  
	    		  wayMap.put(lastWay, list);
	    		  nodeList.removeAll(nodeList);
	    		  //System.out.println(wayMap.get(lastWay));
	    	  }
	    	  lastWay = attributes.getValue("id");
	    	  startNode = "";
	      }
	      
	      if(qName.contentEquals("nd")) {
	    	  
	    	  if(startNode != "") {
	    		  nodeList.add(new Way(startNode,attributes.getValue("ref")));
	    		  nodeList.add(new Way(attributes.getValue("ref"), startNode));
	    		  startNode = attributes.getValue("ref");
	    	  } else {
	    		  startNode = attributes.getValue("ref");
	    	  }
	    	  
	    	  
	    	 
	      }
	      
		  // Checking for maxspeed tag
		  
	      if(qName.contentEquals("tag")) {
	    	  //System.out.println(lastWay);
	    	  if(attributes.getValue("k").equals("maxspeed") && attributes.getValue("v").contentEquals("60 mph") || attributes.getValue("v").contentEquals("70 mph")) {
	    		  lastWay = "";
	    		  nodeList.removeAll(nodeList);
	    		  
	    	  }
	    	
	      }

	  } 
	  
	  static class Way{
		  public final String startNode;
		  public final String endNode;
		  
			private Way(String start, String end)  {
				this.startNode = start;
				this.endNode = end;
			}
			@Override
			public boolean equals(Object other) {
				
				return ((Way)other).startNode.equals(this.startNode);
				
			}
			@Override
			public int hashCode() {
				
				return (this.startNode.hashCode() + this.endNode.hashCode());
				
			}
	  }
	  
	  static class Node {
			public final String id, latitude, longitude;
			private Node(String id, String lat, String lon) {
				this.latitude = lat;
				this.longitude = lon;
				this.id = id;
			}
			@Override
			public boolean equals(Object other) {
				
				return ((Node)other).id.equals(this.id);
				
			}
			@Override
			public int hashCode() {
				
				return this.id.hashCode();
				
			}


	}
	  
}