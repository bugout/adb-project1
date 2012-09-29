package query;

import java.util.Vector;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import util.StopWord;

public class QueryRecord {
	
	private String title;
	private String url;
	private String displayUrl;
	private String description;
	private boolean relevant;
	private String htmlText;
	
	public QueryRecord(String title, String url, String displayUrl, String description)
	{
		this.title= title;
		this.url = url;
		this.displayUrl = displayUrl;
		this.description = description;
		relevant = false;
		htmlText = "";
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String displayUrl() {
		return displayUrl;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isRelevant() {
		return relevant;
	}
	
	public void setFeedback(boolean relevant) {
		this.relevant = relevant;
	}
	

	public void downloadRelevantPage() {
		
		try {
			Document htmlDoc = Jsoup.connect(url).get();
			htmlText = htmlDoc.text();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public String getHtmlPage() {
		return htmlText;
	}
	
	public String toString() {
		
		StringBuffer sb = new StringBuffer();	
		sb.append("Title: " + title);
		return sb.toString();
	}

}
