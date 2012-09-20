package analyzer;

import java.util.Map;
import java.util.Vector;

import query.QueryRecord;

// We use a term analyzer to rate each term in the documents
// based on their relevance to the query
public abstract class TermAnalyzer {
	protected String[] basicQuery;
	
	public TermAnalyzer(String[] query) {
		basicQuery = query;
	}
		
	public abstract Map<String, Double> rateTerms(Vector<QueryRecord> results, boolean[] feedbacks, String[] query);
}
