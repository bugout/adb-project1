package analyzer;

import global.Global;

import indexer.DocumentIndexer;
import indexer.TermFreq;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import query.QueryRecord;

public class DocumentComparator extends TermAnalyzer {
	
	private Vector<Vector<TermFreq> > tf;

	public DocumentComparator(String[] query) {
		super(query);
		
		//This vector contains the term frequency (sorted) for each document 
		//in the positives
		tf = new Vector<Vector<TermFreq> >();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, Double> rateTerms(Vector<QueryRecord> results, String[] query) {
		// TODO Auto-generated method stub
	
		DocumentIndexer indexer;
		
		try {
			indexer = new DocumentIndexer("TestDirectory");
			for (QueryRecord result : Global.positives) 
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
	
		analyzeTermFreq();
		
		return null;
	}
	
	public void analyzeTermFreq() {
		
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
				
				if( theMap.containsKey( term ) )
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
		
		System.out.println("------------------");
		for (TermFreq theTF : theVec)
			System.out.println(theTF.toString());
		
	}
}