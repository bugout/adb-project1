package analyzer;

import indexer.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import opennlp.tools.sentdetect.*;

import query.QueryRecord;
import util.Global;
import util.HtmlParser;


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

	private String extractText(QueryRecord r) {
		Document htmlDoc = null;
		try {
			// Download & Parse the webpage
			htmlDoc = Jsoup.connect(r.getUrl()).get();			
		}
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		String text = htmlDoc.text();
		return text;
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
			String text = extractText(r);
			String[] sentences = extractSentences(text);
		
			Vector<String> relevantSentences = selectSentences(sentences, query);
			
//			for (String s : relevantSentences)
//				System.out.println(s);
			System.out.println("total: " + sentences.length);
			System.out.println("refined: " + relevantSentences.size());	
			String doc = concatStrings(relevantSentences);
			docs.add(doc);
		}
		Indexer indexer = null;
		try {
			indexer = new Indexer();			
			indexer.buildAsSingle(docs);
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
		
		for (int i = 0; i < 50; i++)
			System.err.println(termFreqs.get(i).getTerm() + " - " + termFreqs.get(i).getFreq());
		
		Map<String, Double> rates = new HashMap<String, Double>();
		for (TermFreq tf : termFreqs) {
			rates.put(tf.getTerm(), 1.0 * tf.getFreq() / totalFreq);
		}	
		return rates;
	}
	
	public static void main(String[] args) {		
		SentenceTermAnalyzer a = new SentenceTermAnalyzer(new String[]{"bill"});
		Vector<QueryRecord> results = new Vector<QueryRecord>();
		QueryRecord r = new QueryRecord("aa", "http://en.wikipedia.org/wiki/Melinda_Gates", "xxx", 
				"aaaa");
		r.setFeedback(true);
		results.add(r);
		a.rateTerms(results, new String[]{"bill"});
	}
}
