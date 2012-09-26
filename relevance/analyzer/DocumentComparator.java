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
	
	public void setRelevantTerms() {
	
		DocumentIndexer indexer;
		Vector<Vector<String> > allDocWords = new Vector<Vector<String> >();
		
		try {
			indexer = new DocumentIndexer();
			
			for (QueryRecord result : Global.getPositives()) 
			{
				Vector<String> list = result.getHtmlPageWords();	
				allDocWords.add(list);
			}
			indexer.addDocuments(allDocWords);
			
			tf = indexer.getTermFrequencies();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		Vector<String> relevantTerms = analyzeTermFreq();
		Global.setRelevantTerms(relevantTerms);
	}
	
	private Vector<String> analyzeTermFreq() {
		
		/* Algorithm to anaylze terms 
		 * 
		 *  Step 1 - Count highest frequency words in each relevant document - 
		 *  Vector<Vector<String> >
		 *  Step 2 - From these high frequency words, calculate what words 
		 *  appear in the most documents.  Limit to top 10 words.
		 *  
		 */
		
		Map<String, Integer> theMap = new HashMap<String, Integer>();
		for (Vector<TermFreq> theDocument : tf )
		{
			//insert top 10 words from theDocument to map
			//the count of the word is increased, depending on how many documents the term 
			//appears in
			
			ListIterator<TermFreq> itr = theDocument.listIterator(theDocument.size());
			int count = 0;
			
			while (itr.hasPrevious() && count < 15)
			{
				String term = itr.previous().getTerm();
				
				//ignore the term if it is contained in the query
				if ( Global.getCurrentQuery().contains(term.trim().toLowerCase()) )
					continue; 
				else if( theMap.containsKey( term ) )
					theMap.put(term, theMap.get(term) + 1 );
				else
					theMap.put(term, 1);
				
				count++;
			}
		}
		
		//create a list<TermFreq> using the map to high frequency words that 
		//appear the highest amongst all documents
		
		Vector<TermFreq> theVec = new Vector<TermFreq>();
		
		for (Map.Entry<String, Integer> theEntry : theMap.entrySet())
			theVec.add(new TermFreq(theEntry.getKey(), theEntry.getValue()));

		Collections.sort(theVec);
		
		Vector<String> retTerms = new Vector<String>();
		
		ListIterator<TermFreq> itr = theVec.listIterator(theVec.size());
		
		int count = 0;
		System.out.println(theVec.size());
		while (itr.hasPrevious() && count < 50) {
			retTerms.add(itr.previous().getTerm());
			count++;
		}
		return retTerms;
		
	}
}