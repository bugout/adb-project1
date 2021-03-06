package analyzer;

import indexer.TermFreq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import query.QueryRecord;
import util.Global;
import util.Logger;
import util.Logger.MsgType;

public class WikiTitleExpander extends Expander {

	@Override
	public void expand(String[] query, List<String> revisedQuery) {
		
		
		setQueries(query);
		
		Logger myLogger = Logger.getInstance();
			
		List<String> wikiTitle = null;
		
		List<String> relevantTerms = Global.getRelevantTerms();
		
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
			myLogger.write("Wiki Title: " + wikiTitle.toString(), MsgType.DEBUG);
		
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
				getRevisedQueryFromWiki(candidates, wikiTitle, revisedQuery);	
				myLogger.write("Revised Query genrated by wiki title expander: " 
								+ revisedQuery.toString(), MsgType.DEBUG);
			}
		}
	}
		
	private void getRevisedQueryFromWiki(List<String> candidates, List<String> wikiTitle, List<String> revisedQuery) {
		
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
}
