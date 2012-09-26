package analyzer;

import indexer.DocumentIndexer;
import indexer.TermFreq;

import java.io.IOException;
import java.util.*;
import query.QueryRecord;
import util.Global;

public class DocumentComparator {
	
	private Vector<Vector<TermFreq> > tf;

	public DocumentComparator() {
		//This vector contains the term frequency (sorted) for each document 
		//in the positives
		tf = new Vector<Vector<TermFreq> >();
	}
	
/*	public String[] tempFunc(Vector<QueryRecord> results, String[] query) {
		
		rateTerms(results, query);
		
		System.out.println("Expanding terms :" + keyWord);
		
		// Append the expanded term to the end of the previous query
		List<String> newquery = new ArrayList(Arrays.asList(query));
		newquery.add(keyWord);
		
		return newquery.toArray(new String[0]);		
		
		
	}*/
	
	public void setRelevantTerms() {
	
		DocumentIndexer indexer;
		
		try {
			indexer = new DocumentIndexer("TestDirectory");
			for (QueryRecord result : Global.getPositives()) 
			{
				Vector<String> list = result.getHtmlPageWords();	
				indexer.addDocument(list);
			}
			//investigate if a flush can replace close
			indexer.closeWriter();
			tf = indexer.getTermFrequencies();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		Vector<String> relevantTerms = analyzeTermFreq();
		
		Global.setRelevantTerms(relevantTerms);
	}
	
	private Vector<String> analyzeTermFreq() {
		
		//Needs a better mechanism to analyze terms, rather than just taking a subset of top terms
		//need to find a way to add weight to top 10 terms
		//for now - top 10 terms that appear the most across the documents
		
		//here frequency is by top terms that appear in most documents, it can't be part of original query
		
		//create a map with terms and frequency, example 
		
		Map<String, Integer> theMap = new HashMap<String, Integer>();
		for (Vector<TermFreq> doc : tf )
		{
			for(int i = (doc.size()-1); i > (doc.size() - 11); i--)
			{
				String term = doc.get(i).getTerm();
				Integer value = doc.get(i).getFreq();
				
				if (term.toLowerCase().indexOf("gates") != -1)
				{ System.out.printf( "ignoring %s%n", term ); }
				else if( theMap.containsKey( term ) )
					theMap.put(term, theMap.get(term) + 1 );
				else
					theMap.put(term, 1);
			}
		}
		
		//create a list<TermFreq> using the map to high frequency words that 
		//appear the highest amongst all documents
		
		Vector<TermFreq> theVec = new Vector<TermFreq>();
		
		for (Map.Entry<String, Integer> theEntry : theMap.entrySet())
			theVec.add(new TermFreq(theEntry.getKey(), theEntry.getValue()));

		Collections.sort(theVec);
		
		Vector<String> retTerms = new Vector<String>();
		
		System.out.println(theVec.size());
		for (int i = 1; i < 11; i++)
			retTerms.add(theVec.get( (theVec.size() - i) ).getTerm());
				
		return retTerms;
		
	}
}