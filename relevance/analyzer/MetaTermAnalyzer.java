package analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import query.QueryRecord;

// Meta term analyzer will assign score to word according to the description
public class MetaTermAnalyzer extends TermAnalyzer {

	// there are four components in the meta data
	// 1. title 2. url 3. display url 4. description
	double[] weights = {0.7, 0, 0, 0.3};
	
	public MetaTermAnalyzer(String[] query) {
		super(query);
	}

	@Override
	public Map<String, Double> rateTerms(Vector<QueryRecord> results, String[] query) {
		
		List<Map<String, Double>> rates = new ArrayList<Map<String, Double>>();
		rates.add(parseTitle(results));
		rates.add(parseUrl(results));
		return null;
	}
	
	private Map<String, Double> parseTitle(Vector<QueryRecord> results) {
		
		return null;
	}
	private Map<String, Double> parseUrl(Vector<QueryRecord> results) {
		return null;
	}
	private Map<String, Double> parseDisplayUrl(Vector<QueryRecord> results) {
		return null;
	}
	private Map<String, Double> parseDescription(Vector<QueryRecord> results) {
		return null;
	}
	

}