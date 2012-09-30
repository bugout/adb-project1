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
		
		if( revisedQuery.isEmpty() )
			analyzeDisplayURL();
		
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

	
	//determine if any of the top 10 words appear in more than 4 documents
	//if it does, we can say with high confidence that is a relevant word
	//if there are two such words, we need to consider both of them as they may
	//form a phrase
	private void analyzeOtherTitles() {
		
		List<TermFreq> myTopWordsList = new ArrayList<TermFreq>();
		List<String> candidates = new ArrayList<String>();;
		//ignore wiki pages
		for (String s : relevantTerms) {	
			int occurrence = 0;
			for (QueryRecord result : Global.getPositives())
			{	
				String content = result.getTitle() + result.getDescription();		
				content = content.replaceAll("[^\\w\\s]", "");//replace punctuations
				List<String> contentList = Arrays.asList(content.split("\\s+"));
	
				Global.sanitizeList(contentList);
				
				if(contentList.contains(s))
					occurrence++;
			}	
			
			myTopWordsList.add(new TermFreq(s, occurrence));
		}
		
		Collections.sort(myTopWordsList);
		Collections.reverse(myTopWordsList);
	
		myLogger.write(myTopWordsList.toString(), MsgType.ERROR);
		
		Iterator<TermFreq> itr = myTopWordsList.iterator();
		int count = 0;
		while(itr.hasNext() && count < 2) {
			TermFreq termFreq = itr.next();
			if(termFreq.getFreq() > 3)
				candidates.add( termFreq.getTerm() );
			count++;
			
		}
		
		if(candidates.size() > 0)
		{
			if (candidates.size() == 2)
				analyzeOrder(candidates);
			revisedQuery.addAll(originalQuery);
			revisedQuery.addAll(candidates);
		}
	}
	
	//The order of candidates can be somewhat determined 
	//if we compare the position of candidate strings with respect to each other
	//and if there is a pattern found if one appears before the other always
	//otherwise the order of two words added will not matter
	
	private void analyzeOrder(List<String> candidates)
	{
		int swap = 0;
		Iterator<QueryRecord> iter = Global.getPositives().iterator();
		while(iter.hasNext() && swap == 0)
		{	
			QueryRecord result = iter.next();
			
			String content = result.getTitle() + result.getDescription();		
			content = content.replaceAll("[^\\w\\s]", "");//replace punctuations
			List<String> contentList = Arrays.asList(content.split("\\s+"));

			Global.sanitizeList(contentList);
			
			if ( contentList.contains(candidates.get(0)) && 
					contentList.contains(candidates.get(1)))
			{
				if ( contentList.indexOf(candidates.get(0)) > 
						contentList.indexOf(candidates.get(1)) )
					swap++;
			}
		}
		
		//if there are at least 3 documents where both the words co-exist 
		//and one always exist after the other
		if (swap > 2)
		{
			String temp = candidates.get(0);
			candidates.set(0, candidates.get(1));
			candidates.set(1, temp);
		}
	}
	
	private void analyzeDisplayURL() {
	
		boolean isFound = false;
		String titleText = "";
		
		List<String> candidates = new ArrayList<String>();
		List<String> titleList = new ArrayList<String>();
		
		Iterator<QueryRecord> iter = Global.getPositives().iterator();
		
		while(iter.hasNext())
		{	
			QueryRecord result = iter.next();
			String displayUrl = result.displayUrl();		
			displayUrl = displayUrl.replaceAll("[^\\w]", "");//replace punctuations
			displayUrl = displayUrl.replaceAll("[.*\\.]", "");
			displayUrl = displayUrl.replaceAll("[\\..*]", "");
			displayUrl = displayUrl.trim().toLowerCase();
			
			//check if display url contains any of the query words
			for (String s : sanitizedOriginalQuery)
				if ( displayUrl.contains(s) )
				{
					isFound = true;
					titleText = titleText + result.getTitle();
				}
		}
		
		Logger.getInstance().write(titleText, MsgType.ERROR);
				
		if (isFound)
		{
			//check which top two terms appear in the title and in which order
			titleText = titleText.trim().toLowerCase();
			String[] titleArray = titleText.split("[\\s+]");
			titleList = Arrays.asList(titleArray);
			
			Global.sanitizeList(titleList);
			
			//check top 5 elements
			
			Iterator<String> itr = relevantTerms.iterator();
			int count = 0;
			while (itr.hasNext() && count < 5)
			{
				String s = itr.next();
				if(titleList.contains(s))
					candidates.add(s);
			}
			
		}
		
		//add the top two elements to the revised list
		
		if(candidates.size() > 0)
		{
			if (candidates.size() > 1)
				analyzeOrder(candidates, titleList);
			revisedQuery.addAll(originalQuery);
			//add first two element
			revisedQuery.add(candidates.get(0));
			revisedQuery.add(candidates.get(1));
		}
		
	}
	
	private void analyzeOrder(List<String> candidates, List<String> titleList) {
		
		if ( titleList.indexOf(candidates.get(0)) > titleList.indexOf(candidates.get(1)) )
		{
			String temp = candidates.get(0);
			candidates.set(0, candidates.get(1));
			candidates.set(1, temp);
		}
	}
	
}
