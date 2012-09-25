package analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import query.QueryRecord;

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
		
		// Pick the term that has the highest sum ratings
		String expandTerm = null;
		double maxScore = Double.MIN_VALUE;		
		
		List<String> currentQuery = Arrays.asList(query);
		for (Map.Entry<String, Double> tr : termRates.entrySet()) {			
			if (tr.getValue() > maxScore && !currentQuery.contains(tr.getKey())) {
				maxScore = tr.getValue();
				expandTerm = tr.getKey();
			}
		}
		
		System.out.println("Expanding terms :" + expandTerm);
		
		// Append the expanded term to the end of the previous query
		List<String> newquery = new ArrayList(Arrays.asList(query));
		newquery.add(expandTerm);
		
		return newquery.toArray(new String[0]);		
	}

}
