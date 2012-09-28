package analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import query.QueryRecord;
import util.Global;
import util.Logger;
import util.MapValueComparator;
import util.StopWord;
import util.Logger.MsgType;

// Meta term analyzer will assign score to word according to the description
public class MetaTermAnalyzer extends TermAnalyzer {

	// there are four components in the meta data
	// 1. title 2. url 3. display url 4. description	
	public enum MetaDataType {
	    TITLE, URL, DISPLAY_URL, DESCRIPTION 
	};
	
	double[] weights = {0.7, 0, 0, 0.3};
	
	public MetaTermAnalyzer(String[] query) {
		super(query);
	}

	@Override
	public Map<String, Double> rateTerms(Vector<QueryRecord> results, String[] query) {		
		List<Map<String, Double>> rates = new ArrayList<Map<String, Double>>();
		rates.add(parseMetaData(MetaDataType.TITLE));
		rates.add(parseMetaData(MetaDataType.URL));
		rates.add(parseMetaData(MetaDataType.DISPLAY_URL));
		rates.add(parseMetaData(MetaDataType.DESCRIPTION));
		
		Map<String, Double> overallRates = new HashMap<String, Double>();
		
		for (int i = 0; i < rates.size(); i++) {
			Map<String, Double> rate = rates.get(i);
			for (Map.Entry<String, Double> entry : rate.entrySet()) {
				if (!overallRates.containsKey(entry.getKey())) {
					overallRates.put(entry.getKey(), entry.getValue() * weights[i]);
				}
				else {
					overallRates.put(entry.getKey(), 
							overallRates.get(entry.getKey()) + entry.getValue() * weights[i]);
				}
			}
		}
		
		System.err.println("Weight of word in metadata analyzer");
		for (Map.Entry<String, Double> entry : overallRates.entrySet()) {
			System.err.println(entry.getKey() + " - " + entry.getValue());
		}
		
		TreeMap<String, Double> tmap = new TreeMap<String, Double>(
				new MapValueComparator(overallRates) );

		tmap.putAll(overallRates);
		
		Logger myLogger = Logger.getInstance();
		
		Iterator<Entry<String,Double>> itr = tmap.entrySet().iterator();
		int count = 0;
		myLogger.write("********List of top 10 words by Metadata Terms Analyzer****", 
				MsgType.LOG);
		while (itr.hasNext() && count < 10)
		{
			myLogger.write("term: " + itr.next().getKey(), MsgType.LOG);
			count++;
		}

		return overallRates;
	}
	
	private Map<String, Double> parseMetaData(MetaDataType type) {
		
		Map<String, Double> rates = new HashMap<String, Double>();
		
		if (type == MetaDataType.URL || type == MetaDataType.DISPLAY_URL)
			return rates;
		
		Map<String, Integer> wordFreqs = new HashMap<String, Integer>();
		
		int wordCount = 0;
		Vector<QueryRecord> positives = Global.getPositives();
		for (int i = 0; i < positives.size(); i++) {
			
			String metaData = new String("");
			
			if (type == MetaDataType.TITLE)
				metaData = positives.get(i).getTitle();
			else if (type == MetaDataType.DESCRIPTION)
				metaData = positives.get(i).getDescription();
			
			metaData = metaData.toLowerCase();
			String[] words = metaData.split("\\s+");
			for (String word : words) {
				word = word.replaceAll("[^a-z0-9]", "");
				if (word.length() == 0 || StopWord.StopWordList().contains(word))
					continue;
							
				wordCount++;				 
				
				//this calculates the word frequency
				if (!wordFreqs.containsKey(word)) {
					wordFreqs.put(word, 1);
				}
				else {
					wordFreqs.put(word, wordFreqs.get(word) + 1);
				}
			}			
		}
		// for each word, put termFreq/TotalWordCount in the map.	
		for (Map.Entry<String, Integer> entry : wordFreqs.entrySet()) {
			rates.put(entry.getKey(), 1.0 * entry.getValue() / wordCount);
		}
		
		return rates;	
	}
	
	
	public static void main(String[] args) {
		Vector<QueryRecord> results = new Vector<QueryRecord>();
		results.add(new QueryRecord("bill gates a the", "", "", "bill microsoft gates"));
		results.add(new QueryRecord("bill gates a the", "", "", "microsoft microsoft"));
		results.add(new QueryRecord("bill a the a", "", "", "microsoft microsoft"));
		for (QueryRecord r : results)
			r.setFeedback(true);
		
		
		MetaTermAnalyzer a = new MetaTermAnalyzer(new String[]{"aaa"});
		Map<String, Double> rates = a.rateTerms(results, new String[]{"aaa"});
		for (Map.Entry<String, Double> entry : rates.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}
	}
}