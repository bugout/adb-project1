package analyzer;

import indexer.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import util.*;
import util.Logger.MsgType;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import query.QueryRecord;

public class WikiTermAnalyzer extends TermAnalyzer {
	private Indexer indexer;
	private static boolean external = false;
	
	public WikiTermAnalyzer(String[] query) {
		super(query);
	}

	@Override
	public Map<String, Double> rateTerms(Vector<QueryRecord> results, String[] query) {

		// Find wikipedia pages in the query result
		Vector<QueryRecord> wikiPages = new Vector<QueryRecord>();
		
		for (QueryRecord result : Global.getPositives()) 
			if (result.getUrl().matches(".*wikipedia\\.org.*")) 
				wikiPages.addElement(result);
		
		// Check postive wikipedia pages in the query result
		Map<String, Double> rates = analyzeWikiPages(wikiPages, query);
		
		//DO NOTHING FOR NEGATIVES
		
		TreeMap<String, Double> tmap = new TreeMap<String, Double>(
				new MapValueComparator(rates) );

		tmap.putAll(rates);
		Logger myLogger = Logger.getInstance();

		//write the top 10 words to the cop
		Iterator<Entry<String,Double>> itr = tmap.entrySet().iterator();
		if (Global.DEBUG)
		{
			int count = 0;
			StringBuilder sb = new StringBuilder();
			sb.append("Top 10 words by Wiki Terms Analyzer: [ ");
			while (itr.hasNext() && count < 10)
			{
				sb.append(itr.next().getKey() + " ");
				count++;
			}
			sb.append("]");
			myLogger.write(sb.toString(), MsgType.DEBUG);
		}
		return rates;
	}
	
	private Map<String, Double> analyzeWikiPages(Vector<QueryRecord> wikiPages, String[] query) {
		// Building a document repository
		// rate words using tf-rdf
		if (wikiPages.size() == 0)
			return new HashMap<String, Double>();
		
		HtmlParser parser = new HtmlParser();
		Vector<String> documents = new Vector<String>();		
		for (QueryRecord wikiPage : wikiPages) {
			Document htmlDoc = null;
			String text = wikiPage.getHtmlPage();	
			documents.add(text);		
			
			if (external) {
				Logger.getInstance().write("Trying to follow trustful external links from the wiki page ", Logger.MsgType.LOG);
				// Extract and follow the External links
				Vector<String> links = extractExternalLinks(htmlDoc);
				// Download all pages			
				for (String link : links) {					
					Logger.getInstance().write(String.format("Downloading page %s\n", link), Logger.MsgType.LOG);
					text = parser.getText(link);
					documents.add(text);
				}				
			}
		}		
				
		try {
			indexer = new Indexer();			
			indexer.buildAggregate(documents);
		}
		catch (IOException e){
			e.printStackTrace();
			Logger.getInstance().write("Building index fails.", Logger.MsgType.DEBUG);
		}	
		
		Map<String, Double> rates = analyzeTerms();
		return rates;
	}

	
	private Map<String, Double> analyzeTerms()  {
		Vector<TermFreq> termFreqs = indexer.getTermFreqs();
		
		int totalFreq = 0;
		for (TermFreq tf : termFreqs) {
			totalFreq += tf.getFreq();
		}
		
		Collections.sort(termFreqs);
		Collections.reverse(termFreqs);
		
		//get the top 50 words
		for (int i = 0; i < 10; i++)
			System.err.println(termFreqs.get(i).getTerm() + " - " + termFreqs.get(i).getFreq());
		
		Map<String, Double> rates = new HashMap<String, Double>();
		for (TermFreq tf : termFreqs) {
			rates.put(tf.getTerm(), 1.0 * tf.getFreq() / totalFreq);
		}
		
		return rates;
	}
	
	private static Vector<String> extractExternalLinks(Document htmlDoc) {
		Vector<String> links = new Vector<String>();
		Elements elinks = htmlDoc.select("h2 ~ ul li a[rel=nofollow][class=external text]");
		for (Element e : elinks) {
			String link = e.attr("href");
			links.add(link);
		}
		
		return links;
	}	
}
