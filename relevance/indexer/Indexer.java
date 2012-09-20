package indexer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
import org.apache.lucene.util.Version;


public class Indexer {
	private IndexWriter writer;
	private String indexDir;
	
	public Indexer(String indexDir) throws IOException{
		this.indexDir = indexDir;
		Directory dir = FSDirectory.open(new File(indexDir));
		writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_CURRENT), 
				true, IndexWriter.MaxFieldLength.UNLIMITED);
		
	}
		
	public void buildCorpus(Vector<String> docs) throws IOException {
		writer.deleteAll();
		for (String doc : docs) {
			Document d = new Document();
			d.add(new Field("content", doc, Field.Store.YES, Field.Index.ANALYZED));
			writer.addDocument(d);
		}
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
	
	public Vector<String> getTopTerms() {

		try {
			IndexReader reader = null; 
			reader = IndexReader.open(FSDirectory.open(new File(indexDir)));
			Vector<TermFreq> termFreqs = new Vector<TermFreq>();
			assert(reader.numDocs() == 1);
			for (int i = 0; i < reader.numDocs(); i++) {				
				TermFreqVector termVector = reader.getTermFreqVector(i, "content");
				assert(termVector != null);
				String[] terms = termVector.getTerms();
				int[] freqs = termVector.getTermFrequencies();

				for (int j = 0; j < terms.length; j++) {		
					termFreqs.add(new TermFreq(terms[j], freqs[j]));
				}			
			}
			Collections.sort(termFreqs);
			Vector<String> terms = new Vector<String>();
			for (int i = termFreqs.size() -1; i >0 ; i--)
				terms.add(termFreqs.get(i).term);
			return terms;
		}
		catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	class TermFreq implements Comparable<TermFreq> {
		private String term;
		private int freq;
		
		public TermFreq(String term, int freq) {
			this.term = term;
			this.freq = freq;
		}
		@Override
		public int compareTo(TermFreq ot) {
			if (freq > ot.freq)
				return 1;
			else if (freq < ot.freq)
				return -1;
			else
				return 0;
		}
	}
}
