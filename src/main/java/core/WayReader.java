package core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import core.SAXPrinter.Node;
import core.SAXPrinter.Way;
import core.XMLReader.Param;

public class WayReader {
	
	public static final String fileName = "way.osm";
	
	private static HashMap<String, List<Way>> wayMap = new HashMap<String, List<Way>>();

	public static void main(String[] args) throws Exception {

		read(fileName);
		
    }
	
	// Read in the parameterised file and pass it to SAXPrinter
	public static void read(String file) throws Exception {
		
		System.err.println("Reading Ways...");
		
		//Sax Parser tutorial found at https://mkyong.com/java/how-to-read-xml-file-in-java-sax-parser/
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		
		SAXParser saxParser = factory.newSAXParser();

        SAXPrinter handler = new SAXPrinter();
        saxParser.parse(file, handler);
        
        wayMap = handler.getMap();
        
	}
	
	public HashMap<String, List<Way>> getMap(){
		return wayMap;
	}

}
