package analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import query.QueryRecord;
import util.Global;
import util.Logger;
import util.MapValueComparator;
import util.Logger.MsgType;

/* Term rate expander expand the query
 * by adding the term with highest rating score
 */
public class TermRateExpander extends Expander {
	private Vector<TermAnalyzer> analyzers = new Vector<TermAnalyzer>();
		
	public TermRateExpander(String[] query) {
		super(query);			
		registerAnalyzers();
	}
	
	private void registerAnalyzers() {
		registerAnalyzer(new DocumentTermsAnalyzer(basicQuery));
		registerAnalyzer(new MetaTermAnalyzer(basicQuery));
		registerAnalyzer(new WikiTermAnalyzer(basicQuery));
		registerAnalyzer(new SentenceTermAnalyzer(basicQuery));
	}
	
	private void registerAnalyzer(TermAnalyzer analyzer) {
		if (!analyzers.contains(analyzer))
			analyzers.add(analyzer);
	}

	@Override
	public String[] expand(Vector<QueryRecord> results, String[] query) {
		// sum up the scores of each term in all analyzers
		Map<String, Double> termRates = new HashMap<String, Double>();				
		for (TermAnalyzer analyzer : analyzers) {	
			// Rate each term with one analyzer
			Map<String, Double> singleRate = analyzer.rateTerms(results, query);
			
			// Update the score sum
			for (Map.Entry<String, Double> tr : singleRate.entrySet()){
				if (!termRates.containsKey(tr.getKey()))
					termRates.put(tr.getKey(), tr.getValue());
				else
					termRates.put(tr.getKey(), tr.getValue() + termRates.get(tr.getKey()));	
			}
		}	

		TreeMap<String, Double> tmap = new TreeMap<String, Double>(
				new MapValueComparator(termRates) );

		tmap.putAll(termRates);
		Logger myLogger = Logger.getInstance();		
		
		List<String> currentQuery = Arrays.asList(query);
		Global.sanitizeList(currentQuery);
		
		//write the top 10 words to the log
		Iterator<Entry<String,Double>> itr = tmap.entrySet().iterator();
		int count = 0;
		
		List<String> relevantTerms = new ArrayList<String>();
		
		while (itr.hasNext() && count < 10)
		{
			String term = itr.next().getKey();
			term = term.trim().toLowerCase();			
			if (!currentQuery.contains(term) )
			{ 
				relevantTerms.add(term); 
				count++;
			}
		}
			
		if(Global.DEBUG)
			myLogger.write("Relevant Terms by Term Rate Expander: " + 
					relevantTerms.toString(), MsgType.ERROR);
		
		Global.setRelevantTerms(relevantTerms);
		
		KeyWordFinder finder = new KeyWordFinder();
		String[] retval = finder.anlalyzeKeyWords(query);
		
		return retval;		
	}

}
