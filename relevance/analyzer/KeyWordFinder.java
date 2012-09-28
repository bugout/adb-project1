package analyzer;

import indexer.TermFreq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import query.QueryRecord;
import util.Global;
import util.Logger;
import util.Logger.MsgType;

public class KeyWordFinder {
	
	private List<String> sanitizedOriginalQuery;
	private List<String> originalQuery;
	private List<String> revisedQuery;  
	private Logger myLogger; 
	private List<String> relevantTerms;
	
	public KeyWordFinder() {
		sanitizedOriginalQuery = null;
		originalQuery = null;
		revisedQuery = new ArrayList<String>();
		myLogger = Logger.getInstance();
		relevantTerms = Global.getRelevantTerms(); 
	}
	
	public String[] anlalyzeKeyWords(String[] query)  {
		
		sanitizedOriginalQuery = Arrays.asList(query);
		originalQuery = Arrays.asList(query);
		
		Global.sanitizeList(originalQuery);
		myLogger.write("Existing Query: " + originalQuery.toString(), MsgType.LOG);
		
		//if there is a wiki page for the given query
		analyzeWikiTitle();	
		
		//add other analysis methods here
		
		//if the revised query is empty at the end of this function
		// add the existing query to the revised query and the top relevant 
		//word
		if( revisedQuery.isEmpty() )
		{
			revisedQuery.addAll(originalQuery);
			revisedQuery.add(relevantTerms.get(0));
			myLogger.write("Revised Query: " + revisedQuery.toString(), MsgType.LOG);
		}
		
		String[] retval = revisedQuery.toArray(new String[0]);
		
		return retval;
	}
	
	private void analyzeWikiTitle() {
	
		List<String> wikiTitle = null;
		
		for (QueryRecord result : Global.getPositives())
			if (result.getUrl().matches(".*wikipedia\\.org.*")) {
				String title = result.getTitle();
				title = title.replace("- Wikipedia, the free encyclopedia", "");
				title = title.replaceAll("[^\\w\\s]", "");
				wikiTitle = Arrays.asList(title.split("\\s+"));
			}
				
		Global.sanitizeList(wikiTitle);
		myLogger.write("Wiki Title: " + wikiTitle.toString(), MsgType.LOG);
	
		//if query term or terms are contained in the wikiTitle
		if ( partOfWikiTitle(wikiTitle) ) {
			
			Global.sanitizeList(relevantTerms);
			
			myLogger.write("Relevant Terms: " + relevantTerms.toString(), MsgType.LOG);		
			
			List<String> candidates = new ArrayList<String>();
			for (String s : relevantTerms)
			{
				//check if s is not already a query term
				if ( !sanitizedOriginalQuery.contains(s) )
					if (wikiTitle.contains(s))
						candidates.add(s);
			}
			
			if (candidates.size() > 0)
			{
				getRevisedQueryFromWiki(candidates, wikiTitle);	
				myLogger.write("Revised Query: " + revisedQuery.toString(), MsgType.LOG);
			}
		}
	}
	
	private void getRevisedQueryFromWiki(List<String> candidates, List<String> wikiTitle) {
		
		//create a map with string, and integer position.  
		//sort the map
		//and write the query
		
		//note that we are overloading termfreq over here
		//we need a way to sort by values in the map
		
		List<String> subOriginalQuery = new ArrayList<String>();
		List<TermFreq> newQuery = new ArrayList<TermFreq>();
		
		for (String s : originalQuery)
		{
			if(wikiTitle.contains( s.toLowerCase().trim() ))
				newQuery.add(new TermFreq(s, wikiTitle.indexOf(s.toLowerCase().trim())) );
			else
				subOriginalQuery.add(s);
		}
		
		//top 2 words
		int count = 0;
		Iterator<String> itr = candidates.iterator();
		while (itr.hasNext() && count < 2) {
			String s = itr.next();
			newQuery.add(new TermFreq(s, wikiTitle.indexOf(s)));
			count++;
		}
			
		//sort the newQuery vector by values
		Collections.sort(newQuery);
		
		//construct the revisedQuery
		revisedQuery.addAll(subOriginalQuery);
		for (TermFreq element : newQuery)
			revisedQuery.add(element.getTerm());
	}
	
	//returns true if query or part of query is part of Wiki Title
	private boolean partOfWikiTitle(List<String> wikiTitle) {
		boolean retval = false;
		
		for (String s : sanitizedOriginalQuery) {
			if (wikiTitle.contains(s)) {
				retval = true;
				break;
			}
		}
		return retval;
	}
	
}
