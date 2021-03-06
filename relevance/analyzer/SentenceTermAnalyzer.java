package analyzer;

import indexer.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.sentdetect.*;

import query.QueryRecord;
import util.Global;
import util.Logger;
import util.MapValueComparator;
import util.Logger.MsgType;


/* Sentence scope term analyzer will only 
 * analyze those terms that appear in the same sentence
 * as at least one of the keywords in the last iteration
 */
public class SentenceTermAnalyzer extends TermAnalyzer {

	public SentenceTermAnalyzer(String[] query) {
		super(query);
	}
		
	private String concatStrings(Vector<String> strings) {
		StringBuilder sb = new StringBuilder();
		for (String s : strings)
			sb.append(s);
		
		return sb.toString();
	}
	
	private String[] extractSentences(String text) {
		SentenceModel model = null;
		try {
			InputStream modelIn = new FileInputStream("lib/en-sent.bin");
			model = new SentenceModel(modelIn);		
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
		String[] sentences = sentenceDetector.sentDetect(text);
		return sentences;
	}
	
	private Vector<String> selectSentences(String[] sentences, String[] query) {
			
		StringBuilder regexBuilder = new StringBuilder();
		regexBuilder.append(".*(");
		regexBuilder.append(query[0]);
		for (int i = 1; i < query.length; i++) {
			regexBuilder.append("|");
			regexBuilder.append(query[i]);
		}
		regexBuilder.append(").*");
		Pattern pattern = Pattern.compile(regexBuilder.toString(), 
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		
		Vector<String> selected = new Vector<String>();
		for (String s : sentences) {
			 Matcher m = pattern.matcher(s);
			 if (m.matches()) {
				 selected.add(s);
			 }
		}
		return selected;
	}
	
	@Override
	public Map<String, Double> rateTerms(Vector<QueryRecord> results,
			String[] query) {			
		// find relevant docs		
		Vector<QueryRecord> positives = Global.getPositives();
		
		Vector<String> docs = new Vector<String>();
		for (QueryRecord r : positives) {
			String text = r.getHtmlPage();
			String[] sentences = extractSentences(text);
		
			Vector<String> relevantSentences = selectSentences(sentences, query);
			
//			for (String s : relevantSentences)
//				System.out.println(s);
//			System.out.println("total: " + sentences.length);
//			System.out.println("refined: " + relevantSentences.size());	
			String doc = concatStrings(relevantSentences);
			docs.add(doc);
		}
		Indexer indexer = null;
		try {
			indexer = new Indexer();			
			indexer.buildAggregate(docs);
		}
		catch (IOException e){
			e.printStackTrace();
		}	
		
		// Build lucene index
		Vector<TermFreq> termFreqs = indexer.getTermFreqs();
		
		int totalFreq = 0;
		for (TermFreq tf : termFreqs) {
			totalFreq += tf.getFreq();
		}
		
		Collections.sort(termFreqs);
		Collections.reverse(termFreqs);
		
//		for (int i = 0; i < 50; i++)
//			System.err.println(termFreqs.get(i).getTerm() + " - " + termFreqs.get(i).getFreq());
		
		Map<String, Double> rates = new HashMap<String, Double>();
		for (TermFreq tf : termFreqs) {
			rates.put(tf.getTerm(), 1.0 * tf.getFreq() / totalFreq);
		}	
		
		TreeMap<String, Double> tmap = new TreeMap<String, Double>(
				new MapValueComparator(rates) );
		tmap.putAll(rates);
		
		Logger myLogger = Logger.getInstance();		
		
		Iterator<Entry<String,Double>> itr = tmap.entrySet().iterator();
		if (Global.DEBUG)
		{
			int count = 0;
			StringBuilder sb = new StringBuilder();
			sb.append("Top 10 words by Sentence Term Analyzer: [ ");
			while (itr.hasNext() && count < 10)
			{
				sb.append(itr.next().getKey() + " ");
				count++;
			}
			sb.append("]");
			myLogger.write(sb.toString(), MsgType.DEBUG);
		}

		return rates;
	}

}
