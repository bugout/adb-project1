package analyzer;

import indexer.TermFreq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
		
		originalQuery = Arrays.asList(query);
		sanitizedOriginalQuery = Arrays.asList(query);
		
		Global.sanitizeList(sanitizedOriginalQuery);
		if (Global.DEBUG)
			myLogger.write("Sanitized Existing Query: " + 
				sanitizedOriginalQuery.toString(), MsgType.ERROR);
		
		//if there is a wiki page for the given query
		analyzeWikiTitle();	
		
		//add other analysis methods here
		
		//if we don't have a wiki page, then analyze other titles
		
		//if one of the top 5 words appear in 2 or more titles
		//can't be confident about the order in this case
		if( revisedQuery.isEmpty() )
			analyzeOtherTitles();
		
		//if the revised query is empty at the end of this function
		// add the existing query to the revised query and the top relevant 
		//word
		if( revisedQuery.isEmpty() )
		{
			revisedQuery.addAll(originalQuery);
			revisedQuery.add(relevantTerms.get(0));
			if(Global.DEBUG)
				myLogger.write("Revised Query: " + revisedQuery.toString(), MsgType.LOG);
		}
		
		String[] retval = revisedQuery.toArray(new String[0]);
		
		return retval;
	}
	
	private void analyzeWikiTitle() {
	
		List<String> wikiTitle = null;
		
		//if more than one wiki pages, append them. 
		//though in most cases that shouldn't be the case
		String title = "";

		for (QueryRecord result : Global.getPositives())
			if (result.getUrl().matches(".*wikipedia\\.org.*")) {
				String resultTitle = result.getTitle();
				resultTitle = resultTitle.replace("- Wikipedia, the free encyclopedia", "");
				resultTitle = resultTitle.replaceAll("[^\\w\\s]", "");//replace punctuations
				title = title + resultTitle;
			}
		
		//if nothing was added to the title, just return
		if (title == "")
			return;
		
		wikiTitle = Arrays.asList(title.split("\\s+"));
		Global.sanitizeList(wikiTitle);
		
		if(Global.DEBUG)
			myLogger.write("Wiki Title: " + wikiTitle.toString(), MsgType.ERROR);
	
		//if query term or terms are contained in the wikiTitle
		if ( partOfTitle(wikiTitle) ) {
			
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
		for (TermFreq element : newQuery)
			revisedQuery.add(element.getTerm());
		revisedQuery.addAll(subOriginalQuery);
	}
	
	//returns true if query or part of query is part of Wiki Title
	private boolean partOfTitle(List<String> title) {
		boolean retval = false;
		
		for (String s : sanitizedOriginalQuery) {
			if (title.contains(s)) {
				retval = true;
				break;
			}
		}
		return retval;
	}

	private void analyzeOtherTitles() {
/*		
		Vector<List<String>> titles = new Vector<List<String>>();
		
		//ignore wiki pages
		for (QueryRecord result : Global.getPositives())
			if (!result.getUrl().matches(".*wikipedia\\.org.*")) {
				String title = result.getTitle();
				title = title.replaceAll("[^\\w\\s]", "");//replace punctuations
				List<String> titleList = Arrays.asList(title.split("\\s+"));
				titles.add(titleList);
			}
		
		for (List<String> theList : titles)
			Global.sanitizeList(theList);
		
		//check in how many titles
		int occurance = 0;
		for (List<String> theList : titles)
			if ( partOfTitle(theList) )
				occurance++;
		
		//if query words are part of more than 2 title, check if any candidates
		//are in the title
		
		if (occurance > 2)
			
		//check if the query exist in keyword
		//if it does, check if 
*/
	}

}
