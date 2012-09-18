package analyzer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.util.Version;

import query.QueryRecord;

public class SimpleAnalyzer extends Analyzer {
	public SimpleAnalyzer(String[] query) { super(query); }	
	
	@Override
	public String[] expand(Vector<QueryRecord> parsedResult, boolean[] feedbacks) {
		StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_CURRENT);
		Set stopWordSet = sa.getStopwordSet();
		
		Map<String, Integer> wordCount = new HashMap<String, Integer>();
		
		for (int i = 0; i < feedbacks.length; i++) {
			// skip negative feedbacks
			if (feedbacks[i]==false)				
				continue;
			
			// count word frequency
			String[] words = parsedResult.get(i).getDescription().split("\\s+");
			for (String word : words) {				
				word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(); // remove all non-alphanumeric chars
				if (stopWordSet.contains(word)) // skip stop words
					continue;
				if (!wordCount.containsKey(word))
					wordCount.put(word, 1);
				else					
					wordCount.put(word, wordCount.get(word) + 1);
			}				
		}
		String newWord = "";
		int maxFreq = 0;
		for (Map.Entry<String, Integer> e : wordCount.entrySet()) {
			if (e.getValue() > maxFreq) {
				maxFreq = e.getValue();
				newWord = e.getKey();
			}
		}		
		expandedQuery.add(newWord);
		
		Vector<String> newQuery = new Vector<String>(Arrays.asList(originQuery));
		newQuery.addAll(expandedQuery);
		return newQuery.toArray(new String[0]);
	}
}
