package query;

import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import util.Logger;
import util.Logger.MsgType;

//Loop first 10 web entries

public class QueryResultParser {
	
	private Logger myLogger;
	
	//parses xml data that is contained in xmlContent	
	
	public QueryResultParser() {
		myLogger = Logger.getInstance();
	}
	
	public Vector<QueryRecord> parseQueryResult(String xmlContent) {
		Vector<QueryRecord> theRecords = new Vector<QueryRecord>();
		
		try {
			DocumentBuilder theBuilder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
			Document theDoc = theBuilder.parse(new InputSource(new StringReader(xmlContent)));
			theDoc.getDocumentElement().normalize();
			
			NodeList nList = theDoc.getElementsByTagName("content");
			
			//we need first 10 elements
			int listSize = 10;
			
			if (nList.getLength() < listSize)
				listSize = nList.getLength();
			
			for (int i = 0 ; i <  listSize; i++)
			{
				Element elmt = (Element) nList.item(i);
				String title = getTagValue("d:Title", elmt);
				String url = getTagValue("d:Url", elmt);
				String displayUrl = getTagValue("d:DisplayUrl", elmt);
				String description = getTagValue("d:Description", elmt);
				theRecords.add(new QueryRecord(title, url, displayUrl, description));	
				
			}
			
		} catch (ParserConfigurationException e) {
			myLogger.write("Error Parsing XML Data: " + e.toString(), MsgType.DEBUG);
		} catch (SAXException e) {
			myLogger.write("Error Parsing XML Data: " + e.toString(), MsgType.DEBUG);
		} catch (IOException e) {
			myLogger.write("Error Parsing XML Data: " + e.toString(), MsgType.DEBUG);
		}
		
		return theRecords;
	}
	
	private String getTagValue(String tagType, Element theElmt) {
		
		String retval = "";
		
		NodeList theNodeList = theElmt.getElementsByTagName(tagType);
		
		if (theNodeList.getLength() == 0)
			return retval;
		
		NodeList childrenList = theNodeList.item(0).getChildNodes();
		
		if (childrenList.getLength() > 0 )
		{
			Node theValue = (Node) childrenList.item(0);		
			retval = theValue.getNodeValue();
	
		}
		return retval;		
	}
}
