package util;

import java.util.Vector;
import query.QueryRecord;

public class Global {
	
	//these values are set after 
	private static Vector<QueryRecord> positives = new Vector<QueryRecord>(); 
	
	private static Vector<String> relevantTerms = new Vector<String>();
	
	private static Vector<String> currentQueryForComp = new Vector<String>();

	public static void setCurrentQueryForComp(String[] query) {
		
		currentQueryForComp.clear();
		
		for (String s : query)
			currentQueryForComp.add(s.trim().toLowerCase());
	}
	
	public static void setPositives(Vector<QueryRecord> results) {
		
		positives.clear();
		
		for (QueryRecord result : results)
			if ( result.isRelevant() ) {
				positives.add(result);
				result.downloadRelevantPage();
			}
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
	
	public static Vector<String> getCurrentQuery() {
		return currentQueryForComp;
	}

}
