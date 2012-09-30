package analyzer;

import java.util.Arrays;
import java.util.List;

import util.Global;

public abstract class Expander {
	
	//we maintain two lists so that we can modify one to compare
	protected List<String> sanitizedOriginalQuery = null;
	
	//as per instructions, original query should never be modified
	protected List<String> originalQuery = null;
	
	// given the query result of this round and feedback, return an array of new keywords
	public abstract void expand(String[] query, List<String> revisedQuery);
	
	//set original and sanitizedOriginalQuery
	protected void setQueries(String[] query) {
		
		originalQuery = Arrays.asList(query);
		
		String[] sanitizedQuery = (String[])query.clone();
		
		sanitizedOriginalQuery = Arrays.asList(sanitizedQuery);
		
		Global.sanitizeList(sanitizedOriginalQuery);
		
	}
	
}
