package query;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import util.Global;
import util.StopWord;

public class QueryRecord {
	
	private String title;
	private String url;
	private String displayUrl;
	private String description;
	boolean relevant;
	private String htmlText;
	
	public QueryRecord(String title, String url, String displayUrl, String description)
	{
		this.title= title;
		this.url = url;
		this.displayUrl = displayUrl;
		this.description = description;
		relevant = false;
		htmlText = "";
		
		downloadRelevantPage();
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

	private void downloadRelevantPage() {
		
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
	
	public Vector<String> getHtmlPageWords() {
		String[] wordList = htmlText.split("\\s+");
		Vector<String> list = new Vector<String>();
		int i = 0;
		for (String word : wordList) { 				
			word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(); // remove all non-alphanumeric chars
			
			if(StopWord.StopWordList().contains(word))
				continue;
			else {
				list.add(word);
				i++;
			}
		}
		
		return list;
	}

}
