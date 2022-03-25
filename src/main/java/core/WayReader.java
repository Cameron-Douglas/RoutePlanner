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
	
	public static void read(String file) throws Exception {
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
