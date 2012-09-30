package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import query.QueryRecord;

public class Global {
	
	//these values are set after 
	private static Vector<QueryRecord> positives = new Vector<QueryRecord>(); 
	
	private static List<String> relevantTerms = new ArrayList<String>();
	
	public static boolean DEBUG = true;
	
	public static void setPositives(Vector<QueryRecord> results) {
		
		positives.clear();
		
		for (QueryRecord result : results)
			if ( result.isRelevant() ) {
				positives.add(result);
				result.downloadRelevantPage();
			}
	}
	
	public static void setRelevantTerms(List<String> terms) {
		
		relevantTerms.clear();
		
		for (String term : terms)
			relevantTerms.add(term);
		
		sanitizeList(relevantTerms);
	}
	
	public static Vector<QueryRecord> getPositives() {
		return positives;
	}
	
	public static List<String> getRelevantTerms() {
		return relevantTerms;
	}
	
	public static void sanitizeList(List<String> theList) {
		for (int i = 0; i < theList.size(); i++) {
			String s = theList.get(i);
			theList.set(i, s.trim().toLowerCase());
		}
	}
	
}
