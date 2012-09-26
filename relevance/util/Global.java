package util;

import java.util.Set;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import query.QueryRecord;

public class Global {
	
	//these values are set after 
	private static Vector<QueryRecord> positives = new Vector<QueryRecord>(); 
	
	private static Vector<String> relevantTerms = new Vector<String>();

	public static void setPositives(Vector<QueryRecord> results) {
		
		positives.clear();
		
		for (QueryRecord result : results)
			if ( result.isRelevant() )
				positives.add(result);
	}
	
	public static void setRelevantTerms(Vector<String> terms) {
		
		relevantTerms.clear();
		
		for (String term : terms)
			relevantTerms.add(term);
	}
	
	public static Vector<QueryRecord> getPositives() {
		return positives;
	}
	
	public static Vector<String> getRelevantTerms() {
		return relevantTerms;
	}


}
