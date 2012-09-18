package query;

import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


//Loop first 10 web entries

public class QueryResultParser {
	
	public Vector<QueryRecord> parseQueryResult(String fileName) {
		Vector<QueryRecord> theRecords = new Vector<QueryRecord>();
		
		try {
			DocumentBuilder theBuilder = (DocumentBuilderFactory.newInstance()).newDocumentBuilder();
			Document theDoc = theBuilder.parse(fileName);
			theDoc.getDocumentElement().normalize();
			
			System.out.println("Root element :" + theDoc.getDocumentElement().getNodeName());
			
			NodeList nList = theDoc.getElementsByTagName("content");
			System.out.println(nList.getLength());
			
			for (int i = 0 ; i <  10; i++)
			{
				Element elmt = (Element) nList.item(i);
				String title = getTagValue("d:Title", elmt);
				String description = getTagValue("d:Description", elmt);
				String url = getTagValue("d:DisplayUrl", elmt);
				System.out.println(title);
				System.out.println(description);
				System.out.println(url);
				System.out.println("********************");
				theRecords.add(new QueryRecord(title, description, url));				
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
	
	public String getTagValue(String tagType, Element theElmt) {
		
		NodeList nList = theElmt.getElementsByTagName(tagType).item(0).getChildNodes();
		
		Node nValue = (Node) nList.item(0);
		
		return nValue.getNodeValue();		
		
	}
	

}
