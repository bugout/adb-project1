package analyzer;

import indexer.DocumentIndexer;
import indexer.TermFreq;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import query.QueryRecord;
import util.Global;

public class DocumentTermsAnalyzer extends TermAnalyzer {

	private Vector<Vector<TermFreq> > tf;
	
	public DocumentTermsAnalyzer(String[] query) {
		super(query);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, Double> rateTerms(Vector<QueryRecord> results,
			String[] query) {
		
		Map<String, Double> overallRates = new HashMap<String, Double>();
		
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
		
		overallRates = analyzeTermFreq();
		
		return overallRates;
	}
	
	private Map<String, Double> analyzeTermFreq() {
		
		Vector<HashMap<String, Double>> mapVec = new Vector<HashMap<String, Double>>();
		for (Vector<TermFreq> theDocument : tf )
		{
			HashMap<String, Double> theMap = new HashMap<String, Double>();
	
			//calculate total terms
			int totalTerms = 0;
			for (TermFreq theTerm : theDocument)
				totalTerms = totalTerms + theTerm.getFreq();
			
			for (TermFreq theTerm : theDocument)
			{
				theMap.put(theTerm.getTerm(), (1.0 * theTerm.getFreq() / totalTerms));				
				mapVec.add(theMap);
			}
		}
				
		Map<String, Double> retval= new HashMap<String, Double>();
			
		for (HashMap<String, Double> termMap : mapVec ) {
			for (Map.Entry<String, Double> entry : termMap.entrySet()) {
				if (!retval.containsKey(entry.getKey())) {
					retval.put(entry.getKey(), entry.getValue());
				}
				else {
					retval.put(entry.getKey(), 
							retval.get(entry.getKey()) + entry.getValue());
				}
			}
		}
		
		return retval;
	}

}
