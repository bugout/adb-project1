package analyzer;

import java.util.Vector;
import query.QueryRecord;

public abstract class Expander {
	protected String[] basicQuery;
	
	public Expander(String[] query) { this.basicQuery = query;	}

	// given the query result of this round and feedback, return an array of new keywords
	public abstract String[] expand(Vector<QueryRecord> results, boolean[] feedbacks, String[] query);

}
