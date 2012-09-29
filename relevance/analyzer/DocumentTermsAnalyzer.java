package analyzer;

import indexer.Indexer;
import indexer.TermFreq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import query.QueryRecord;
import util.Global;
import util.Logger;
import util.Logger.MsgType;
import util.MapValueComparator;

public class DocumentTermsAnalyzer extends TermAnalyzer {

	private Vector<Vector<TermFreq> > tf = null;
	private Logger myLogger = null;
	
	public DocumentTermsAnalyzer(String[] query) {
		super(query);
		myLogger = Logger.getInstance();
	}

	@Override
	public Map<String, Double> rateTerms(Vector<QueryRecord> results,
			String[] query) {
		
		Map<String, Double> overallRates = new HashMap<String, Double>();
		
		//build a vector with all documents html text
		Vector<String> docs = new Vector<String>();
		for (QueryRecord result : Global.getPositives())	
			docs.add(result.getHtmlPage());
		
		//build a corpus with all the documents
		Indexer indexer;
		try {
			indexer = new Indexer();
			indexer.buildCorpus(docs);
			tf = indexer.getDocTermFreqs();
		} catch (IOException e) {
			Logger.getInstance().write("Building index fails.", Logger.MsgType.ERROR);
			e.printStackTrace();
		}
		
		overallRates = analyzeTermFreq();
			
		TreeMap<String, Double> tmap = new TreeMap<String, Double>(
						new MapValueComparator(overallRates) );
		
		tmap.putAll(overallRates);
		
		//write the top 10 words to the cop
		Iterator<Entry<String,Double>> itr = tmap.entrySet().iterator();
		
		if (Global.DEBUG)
		{
			int count = 0;
			StringBuilder sb = new StringBuilder();
			sb.append("Top 10 words by Document Terms Analyzer: [ ");
			while (itr.hasNext() && count < 10)
			{
				sb.append(itr.next().getKey() + " ");
				count++;
			}
			sb.append("]");
			myLogger.write(sb.toString(), MsgType.ERROR);
		}
		
		return overallRates;
	}
	
	private Map<String, Double> analyzeTermFreq() {
		
		//calcuate tf/(total terms) for each term, and each document
		Vector<HashMap<String, Double>> mapVec = new Vector<HashMap<String, Double>>();
		for (Vector<TermFreq> theDocument : tf )
		{
			HashMap<String, Double> theMap = new HashMap<String, Double>();
	
			//calculate total terms
			int totalTerms = 0;
			for (TermFreq theTerm : theDocument)
				totalTerms = totalTerms + theTerm.getFreq();
						
			for (TermFreq theTerm : theDocument)
				theMap.put(theTerm.getTerm(), (1.0 * theTerm.getFreq() / totalTerms));	
			
			mapVec.add(theMap);
		}
				
		Map<String, Double> retval= new HashMap<String, Double>();
			
		for (HashMap<String, Double> termMap : mapVec ) {
			for (Map.Entry<String, Double> entry : termMap.entrySet()) {
				if (!retval.containsKey(entry.getKey())) {
					retval.put(entry.getKey(), entry.getValue());
				}
				else {
					retval.put(entry.getKey(), (retval.get(entry.getKey()) + entry.getValue()) );
				}
			}
		}
		
		return retval;
	}

}
