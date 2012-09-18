package analyzer;

import java.util.Vector;
import query.QueryRecord;

public abstract class Analyzer {
	protected String[] originQuery;
	protected Vector<String> expandedQuery = new Vector<String>();
	
	public Analyzer(String[] query) {
		this.originQuery = query;
	}
	
	public abstract String[] expand(Vector<QueryRecord> parsedResult, boolean[] feedbacks);
}
