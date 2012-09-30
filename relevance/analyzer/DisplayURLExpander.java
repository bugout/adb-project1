package analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import query.QueryRecord;
import util.Global;
import util.Logger;
import util.Logger.MsgType;

public class DisplayURLExpander extends Expander {

	@Override
	public void expand(String[] query, List<String> revisedQuery) {
		
		setQueries(query);
		
		List<String> relevantTerms = Global.getRelevantTerms();
		Logger myLogger = Logger.getInstance();
		
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
			
		if(Global.DEBUG)
			myLogger.write("Title Text by Display URL Expander: " 
													+ titleText, MsgType.DEBUG);
					
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
			
			myLogger.write("Revised Query generated by Display URL Expander:" +
								revisedQuery.toString(), MsgType.DEBUG);
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
