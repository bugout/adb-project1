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

import org.apache.lucene.index.CorruptIndexException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import query.QueryRecord;

public class WikiTermAnalyzer extends TermAnalyzer {
	private Indexer indexer;
	
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
		int count = 0;
		myLogger.write("********List of top 10 words by Wiki Terms Analyzer****", 
				MsgType.LOG);
		while (itr.hasNext() && count < 10)
		{
			myLogger.write("term: " + itr.next().getKey(), MsgType.LOG);
			count++;
		}
	
		
		return rates;
	}
	
	// Parse the html either by Tika or JSoup
	private Map<String, Double> analyzeWikiPages(Vector<QueryRecord> wikiPages, String[] query) {
		// Building a document repository
		// rate words using tf-rdf
		if (wikiPages.size() == 0)
			return new HashMap<String, Double>();
		
		HtmlParser parser = new HtmlParser();
		Vector<String> documents = new Vector<String>();		
		for (QueryRecord wikiPage : wikiPages) {
			Document htmlDoc = null;
			try {
				// Download & Parse the webpage
				htmlDoc = Jsoup.connect(wikiPage.getUrl()).get();			
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			String text = htmlDoc.text();
			documents.add(text);		
			
			// Extract and follow the External links
			Vector<String> links = extractExternalLinks(htmlDoc);
			// Download all pages
			
			// TODO: Filter out non-html pages
			for (String link : links) {
				System.err.printf("Downloading page %s\n", link);
				text = parser.getText(link);
				documents.add(text);
			}					
		}		
		
		System.err.println("Building index...");
		buildCorpus(documents);
		
		System.err.println("Reading index...");
		Map<String, Double> rates = analyzeTerms();
		return rates;
	}
	
	private void buildCorpus(Vector<String> docs) {
		try {
			indexer = new Indexer();			
			indexer.buildAsSingle(docs);
		}
		catch (IOException e){
			e.printStackTrace();
		}		
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
		for (int i = 0; i < 50; i++)
			System.err.println(termFreqs.get(i).getTerm() + " - " + termFreqs.get(i).getFreq());
		
		Map<String, Double> rates = new HashMap<String, Double>();
		for (TermFreq tf : termFreqs) {
			rates.put(tf.getTerm(), 1.0 * tf.getFreq() / totalFreq);
		}
		
		return rates;
	}
	
	// for now we keep it this way
	private static Vector<String> extractExternalLinks(Document htmlDoc) {
		Vector<String> links = new Vector<String>();
		Elements elinks = htmlDoc.select("h2 ~ ul li a[rel=nofollow][class=external text]");
		for (Element e : elinks) {
			String link = e.attr("href");
			links.add(link);
		}
		
		return links;
	}
	
	public static void main(String[] args) throws Exception {
		String url = "http://en.wikipedia.org/wiki/Bill_Gates";
		System.out.println(url.matches(".*wikipedia\\.org.*"));
		Vector<QueryRecord> positives = new Vector<QueryRecord>(1);
		positives.add(new QueryRecord("bill",url,url,"ddddd"));
		WikiTermAnalyzer a = new WikiTermAnalyzer(new String[0]);
		a.analyzeWikiPages(positives, new String[0]);
	}
}
