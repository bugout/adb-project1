package util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlParser {			
	public String getHtmlContent(String url) {
		
		try {
			Document htmlDoc = Jsoup.connect(url).get();
			
			//depending on what elements we want to parse in html document, 
			//we can call appropriate methods on htmlDoc
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return "";
	}
	
	public String getText(String url) {
		try {
			Document htmlDoc = Jsoup.connect(url).get();
			String text = htmlDoc.text();	
			return text;
			
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
}
