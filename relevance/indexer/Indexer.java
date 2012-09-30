package indexer;

import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import util.Logger;
import util.StopWord;


public class Indexer {
	private IndexWriter writer;
	private Directory indexDir; 
	private IndexWriterConfig config;
	
	public Indexer() throws IOException{
		
		config = new IndexWriterConfig(Version.LUCENE_36, 
				new StandardAnalyzer(Version.LUCENE_36, StopWord.StopWordList()));		
		this.indexDir = new RAMDirectory();		
		writer = new IndexWriter(indexDir, config);		
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
	
	public void buildAggregate(Vector<String> docs) throws IOException {
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
		Vector<TermFreq> termFreqs = new Vector<TermFreq>();
		try {
			IndexReader reader = IndexReader.open(indexDir);				
			
			assert(reader.numDocs() == 1);
			
			TermFreqVector termVector = reader.getTermFreqVector(0, "content");
			if (termVector == null) {
				Logger.getInstance().write("Fetching TermFreqVector failed.", Logger.MsgType.DEBUG);
				return termFreqs;
			}
			String[] terms = termVector.getTerms();
			int[] freqs = termVector.getTermFrequencies();
				
			for (int j = 0; j < terms.length; j++) {		
				termFreqs.add(new TermFreq(terms[j], freqs[j]));
			}
			return termFreqs;
		}
		catch (Exception e){
			Logger.getInstance().write("Reading index failed.", Logger.MsgType.DEBUG);
			e.printStackTrace();
			return termFreqs;
		}
	}
	
	public Vector<Vector<TermFreq> > getDocTermFreqs() {
		
		Vector<Vector<TermFreq> > retVal = new Vector<Vector<TermFreq> >();
		
		try {
			IndexReader reader = IndexReader.open(indexDir);
			for (int i = 0; i < reader.numDocs(); i++) {
				TermFreqVector tfv = null;
				tfv = reader.getTermFreqVector(i, "content");
				
				if (tfv == null)
				{
					Logger.getInstance().write("Fetching TermFreqVector Failed.", Logger.MsgType.DEBUG);
					break;
				}
				
				String[] terms = tfv.getTerms();
				int[] freq = tfv.getTermFrequencies();
				Vector<TermFreq> tfVec = new Vector<TermFreq>();
				
				for (int k = 0; k < terms.length; k++)
					tfVec.add(new TermFreq(terms[k], freq[k]));
				
				Collections.sort(tfVec);
				Collections.reverse(tfVec);
				
				retVal.add(tfVec);
				
			}
		}
		catch (CorruptIndexException e) {
			Logger.getInstance().write("Reading index failed. DEBUG Message: " 
					+ e.toString(), Logger.MsgType.DEBUG);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.getInstance().write("Reading index failed. DEBUG Message: " +
						e.toString() , Logger.MsgType.DEBUG);
			e.printStackTrace();
		}
		
		return retVal;
	
	}	
	
}
