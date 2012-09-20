package analyzer;

import indexer.Indexer;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import util.HtmlParser;

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
	public Map<String, Double> rateTerms(Vector<QueryRecord> results,
			boolean[] feedbacks, String[] query) {
		// Find wikipedia pages in the query result
		Vector<QueryRecord> positives = new Vector<QueryRecord>();
		Vector<QueryRecord> negatives = new Vector<QueryRecord>();
		findWikiPages(results, feedbacks, positives, negatives);
		
		// Check postive wikipedia pages in the query result
		analyzePositive(positives, query);
		
		// Check negative wikipedia pages in the query result
		for (QueryRecord negative : negatives) {
			// do nothing
		}		
		
		return null;
	}
	
	// Parse the html either by Tika or JSoup
	private void analyzePositive(Vector<QueryRecord> positives, String[] query) {
		// Building a document repository
		// rate words using tf-rdf
		HtmlParser parser = new HtmlParser();
		Vector<String> documents = new Vector<String>();		
		for (QueryRecord positive : positives) {
			Document htmlDoc = null;
			try {
				// Download & Parse the webpage
				htmlDoc = Jsoup.connect(positive.getUrl()).get();			
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
		analyzeTerms();
				
	}
	
	private void buildCorpus(Vector<String> docs) {
		try {
			indexer = new Indexer("~/index");			
			indexer.buildAsSingle(docs);
		}
		catch (IOException e){
			e.printStackTrace();
		}		
	}
	
	private void analyzeTerms()  {
		Vector<String> terms = indexer.getTopTerms();
		for (int i = 0; i < 50; i++)
			System.err.println(terms.get(i));
	}
	
	
	private void findWikiPages(Vector<QueryRecord> results, boolean[] feedbacks,
			Vector<QueryRecord> positives, Vector<QueryRecord> negatives) {
		for (int i = 0; i < results.size(); i++) {
			QueryRecord result = results.get(i);
			if (result.getUrl().matches("wikipedia\\.org")) {
				if (feedbacks[i] == true)
					positives.add(result);
				else
					negatives.add(result);
			}
		}
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
		Vector<QueryRecord> positives = new Vector<QueryRecord>(1);
		positives.add(new QueryRecord("bill",url,url,"ddddd"));
		WikiTermAnalyzer a = new WikiTermAnalyzer(new String[0]);
		a.analyzePositive(positives, new String[0]);
	}
}
