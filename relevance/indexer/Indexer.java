package indexer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import util.StopWord;


public class Indexer {
	private IndexWriter writer;
	private Directory indexDir; 
	
	public Indexer() throws IOException{
		this.indexDir = new RAMDirectory();
		writer = new IndexWriter(this.indexDir, 
				new StandardAnalyzer(Version.LUCENE_CURRENT, StopWord.StopWordList()), 
				true, IndexWriter.MaxFieldLength.UNLIMITED);
		
	}
	
	public void buildCorpus(String doc) throws IOException {
		writer.deleteAll();
		Document d = new Document();
		d.add(new Field("content", doc, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
		writer.addDocument(d);
		writer.close();
	}
		
	public void buildCorpus(Vector<String> docs) throws IOException {
		writer.deleteAll();
		for (String doc : docs) {
			Document d = new Document();
			d.add(new Field("content", doc, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			writer.addDocument(d);
		}
		writer.close();
	}
	
	public void buildAsSingle(Vector<String> docs) throws IOException {
		writer.deleteAll();
		StringBuilder sb = new StringBuilder();
		for (String doc : docs) {
			sb.append(doc);
		}
		
		Document d = new Document();
		d.add(new Field("content", sb.toString(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
		writer.addDocument(d);
		writer.close();
	}
	
	
	// get term frequency
	public Vector<TermFreq> getTermFreqs() {
		try {
			IndexReader reader = IndexReader.open(indexDir);	
			Vector<TermFreq> termFreqs = new Vector<TermFreq>();
			assert(reader.numDocs() == 1);
			// hard coding here...
			TermFreqVector termVector = reader.getTermFreqVector(0, "content");
			assert(termVector != null);
			String[] terms = termVector.getTerms();
			int[] freqs = termVector.getTermFrequencies();
				
			for (int j = 0; j < terms.length; j++) {		
				termFreqs.add(new TermFreq(terms[j], freqs[j]));
			}
			return termFreqs;
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
}
