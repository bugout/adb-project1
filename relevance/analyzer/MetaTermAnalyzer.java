package analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import query.QueryRecord;
import util.StopWord;

// Meta term analyzer will assign score to word according to the description
public class MetaTermAnalyzer extends TermAnalyzer {

	// there are four components in the meta data
	// 1. title 2. url 3. display url 4. description
	double[] weights = {0.7, 0, 0, 0.3};
	
	public MetaTermAnalyzer(String[] query) {
		super(query);
	}

	@Override
	public Map<String, Double> rateTerms(Vector<QueryRecord> results, String[] query) {		
		List<Map<String, Double>> rates = new ArrayList<Map<String, Double>>();
		rates.add(parseTitle(results));
		rates.add(parseUrl(results));
		rates.add(parseDisplayUrl(results));
		rates.add(parseDescription(results));
		
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
		
		for (Map.Entry<String, Double> entry : overallRates.entrySet()) {
			System.out.println(entry.getKey() + " - " + entry.getValue());
		}
		
		return overallRates;
	}
	
	private Map<String, Double> parseTitle(Vector<QueryRecord> results) {		
		Map<String, Integer> wordFreqs = new HashMap<String, Integer>();		
		
		int wordCount = 0;
		for (int i = 0; i < results.size(); i++) {
			if (!results.get(i).isRelevant())
				continue;
			String title = results.get(i).getTitle();
			title = title.toLowerCase();
			String[] words = title.split("\\s+");
			for (String word : words) {
				word = word.replaceAll("[^a-z0-9]", "");
				if (word.length() == 0 || StopWord.StopWordList().contains(word))
					continue;
							
				wordCount++;				 
				
				if (!wordFreqs.containsKey(word)) {
					wordFreqs.put(word, 1);
				}
				else {
					wordFreqs.put(word, wordFreqs.get(word) + 1);
				}
			}			
		}
		
		Map<String, Double> rates = new HashMap<String, Double>();
		for (Map.Entry<String, Integer> entry : wordFreqs.entrySet()) {
			rates.put(entry.getKey(), 1.0 * entry.getValue() / wordCount);
		}
		
		return rates;
	}
	private Map<String, Double> parseUrl(Vector<QueryRecord> results) {
		return new HashMap<String, Double>();
	}
	private Map<String, Double> parseDisplayUrl(Vector<QueryRecord> results) {
		return new HashMap<String, Double>();
	}
	private Map<String, Double> parseDescription(Vector<QueryRecord> results) {
		Map<String, Integer> wordFreqs = new HashMap<String, Integer>();		
		
		int wordCount = 0;
		for (int i = 0; i < results.size(); i++) {
			if (!results.get(i).isRelevant())
				continue;
			String description = results.get(i).getDescription();
			description = description.toLowerCase();
			String[] words = description.split("\\s+");
			for (String word : words) {
				if (StopWord.StopWordList().contains(word))
					continue;
				word = word.replaceAll("[^a-z0-9]", "");
				wordCount++;
				if (!wordFreqs.containsKey(word)) {
					wordFreqs.put(word, 1);
				}
				else {
					wordFreqs.put(word, wordFreqs.get(word) + 1);
				}
			}			
		}
		
		Map<String, Double> rates = new HashMap<String, Double>();
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