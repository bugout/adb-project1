package util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlParser {			
	
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
