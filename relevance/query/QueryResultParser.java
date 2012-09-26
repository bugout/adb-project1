package query;

import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//Loop first 10 web entries

public class QueryResultParser {
	//parses xml data that is contained in xmlContent	
	public Vector<QueryRecord> parseQueryResult(String xmlContent) {
		Vector<QueryRecord> theRecords = new Vector<QueryRecord>();
		
		try {
			DocumentBuilder theBuilder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
			Document theDoc = theBuilder.parse(new InputSource(new StringReader(xmlContent)));
			theDoc.getDocumentElement().normalize();
			
			NodeList nList = theDoc.getElementsByTagName("content");
			System.out.println(nList.getLength());
			
			for (int i = 0 ; i <  10; i++)
			{
				Element elmt = (Element) nList.item(i);
				String title = getTagValue("d:Title", elmt);
				String url = getTagValue("d:Url", elmt);
				String displayUrl = getTagValue("d:DisplayUrl", elmt);
				String description = getTagValue("d:Description", elmt);
				theRecords.add(new QueryRecord(title, url, displayUrl, description));	
				
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return theRecords;
	}
	
	private String getTagValue(String tagType, Element theElmt) {
		
		NodeList theNodeList = theElmt.getElementsByTagName(tagType).item(0).getChildNodes();
		Node theValue = (Node) theNodeList.item(0);
		return theValue.getNodeValue();		
	}
}
