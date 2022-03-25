package core;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.opencsv.CSVWriter;

import core.Main.Param;
import core.SAXPrinter.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.net.http.HttpRequest.Builder;

public class XMLReader {
	public static final String fileName = "nodes.osm";
	
	public static final String path = "nodes.csv";
	

    public static void main(String[] args) {

        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {

            SAXParser saxParser = factory.newSAXParser();

            SAXPrinter handler = new SAXPrinter();
            saxParser.parse(fileName, handler);
            
            String url = "https://maps.googleapis.com/maps/api/elevation/json?locations";
            
            Param key = new Param("key","AIzaSyDp_PLQ8mFyXlVkjw_JxKgMcchqrJlCspk");
            
            Set<Node> nodeSet = handler.getSet();
            
            Set<Node> parameter = new HashSet<Node>();
            Iterator<Node> it = nodeSet.iterator();
            int calls = 0;
            int callcounter = 0;
            int nodes = 0;
            
            System.out.println(nodeSet.size());
            
            while(it.hasNext()) {
            	
            	if(callcounter == 90) {
	        		Thread.sleep(1000);
	        		callcounter = 0;
            	}
            	if(parameter.size()<512) {
            		parameter.add(it.next());
            		nodes++;
            		if(!it.hasNext()) {
            			System.out.println("making final call");
            			callGet(url,key,parameter);
            			calls++;
            			callcounter++;
            		}
            	} else if(parameter.size() == 512) {
            		System.out.println("making call " + calls);
            		callGet(url,key,parameter);
            		parameter.removeAll(parameter);
            		calls++;
            		callcounter++;
            	}
            }
            System.out.println("Nodes " + nodes);
            
            System.err.println(calls);
            
        } catch (Exception e) { // Change
            e.printStackTrace();
        }

    }
    
    private static void callGet(String url, Param key, Set<Node> nodeSet) throws InterruptedException {
    	
    	String response = get(url, key, nodeSet);
    	JSONObject obj = new JSONObject(response);
    	JSONArray res = obj.getJSONArray("results");
    
    	File file = new File(path);
    	
    	for(int i = 0; i<res.length(); i++) {
    		
    		System.out.println(res.getJSONObject(i).toString());
    		
    		String elevation = ""+res.getJSONObject(i).getDouble("elevation");
    		
    		Node tmp = nodeSet.iterator().next();
    	
    		writeCSV(file,tmp,elevation);
    		
    		nodeSet.remove(tmp);
    	}
    }
    
    //https://maps.googleapis.com/maps/api/elevation/json?locations=55.9188853,-3.3321096&key=AIzaSyDp_PLQ8mFyXlVkjw_JxKgMcchqrJlCspk  --Sample valid query
    
	private static String get(String url, Param key, Set<Node> locs) throws InterruptedException {
		
		//Construct URL
		int i = 0;
		
		for(Node n : locs) {
			if(i==0) {
				url += "=" + n.latitude + "," + n.longitude;
			} else if(i<locs.size()) {
				url += "%7C" + n.latitude + "," + n.longitude; //%7C is google encoding for | which would otherwise throw illegal character exception
			} 	
			i++;
		}
		url += "&" + key.name + "=" + key.value;
		
		//System.out.println(url);
		
		//Query API
		for(int tries = 0; tries<10; tries++) {
			try {
	            Builder builder = HttpRequest.newBuilder().uri( URI.create( url.toString() ) );
	            HttpRequest request = builder.method("GET", HttpRequest.BodyPublishers.noBody()).build();
	            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	            
	            return response.body();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            System.out.println("Sleeping on try" + tries);
	            Thread.sleep(1000);
	            
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	            System.out.println("Sleeping on try" + tries);
	            Thread.sleep(1000);
	            
	        }
	//		return "hello";
		
		}
		
		return "10 tries reached";
	}
	
	public static void writeCSV(File output, Node param, String elev) {
		
		try {
			
			
	        // create FileWriter object with file as parameter
	        FileWriter outputfile = new FileWriter(output,true);
	  
	        // create CSVWriter object filewriter object as parameter
	        CSVWriter writer = new CSVWriter(outputfile);
	  
	        // add data to csv
	        String[] data1 = { param.id, param.latitude, param.longitude, elev };
	        writer.writeNext(data1);
	       
	        
	        // closing writer connection
	        writer.close();
	    }
	    catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	
	static class Param {
		public final String name, value;
		private Param(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	

}
