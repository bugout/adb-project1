package global;

import java.util.Set;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import query.QueryRecord;

public class Global {
	
	public static Set stopWords;
	
	public static Vector<QueryRecord> positives;
	public static Vector<QueryRecord> negatives;

	public static void setPositives(Vector<QueryRecord> results) {
		
		positives = new Vector<QueryRecord>();
		
		for (QueryRecord result : results)
			if ( result.isRelevant() )
				positives.add(result);
	}
	
	public static void setNegatives(Vector<QueryRecord> results) {
		
		negatives = new Vector<QueryRecord>();
		
		for (QueryRecord result : results)
			if ( !result.isRelevant() )
				negatives.add(result);
	}
	
	//update method to use a more comprehensive stop words list
	public static void setStopWords() {
		stopWords = StandardAnalyzer.STOP_WORDS_SET;
	}

}
