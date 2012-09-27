package indexer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class DocumentIndexer {

	private IndexWriterConfig config;
	private IndexWriter writer;
	private IndexReader reader;
	private Directory dir;
	
	public DocumentIndexer() throws IOException {
		config = new IndexWriterConfig(Version.LUCENE_36, 
				new StandardAnalyzer(Version.LUCENE_36));
		dir = new RAMDirectory();
		writer = new IndexWriter(dir, config);
		writer.deleteAll();
		reader = null;
	}
	
	public void addDocuments(Vector<Vector<String> > allDocumentWords) {	
		
		for (Vector<String> docWords : allDocumentWords)
		{
			Document theDoc = new Document();
			
			for (String s : docWords)
				theDoc.add(new Field("content", s, Field.Store.YES, 
						Field.Index.ANALYZED, Field.TermVector.YES));
			
			try {
				writer.addDocument(theDoc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public Vector<Vector<TermFreq> > getTermFrequencies() {
		
		Vector<Vector<TermFreq> > retVal = new Vector<Vector<TermFreq> >();
		try {
			reader = IndexReader.open(dir);
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < reader.numDocs(); i++) {
			TermFreqVector tfv = null;
			try {
				tfv = reader.getTermFreqVector(i, "content");
				String[] terms = tfv.getTerms();
				int[] freq = tfv.getTermFrequencies();
				Vector<TermFreq> tfVec = new Vector<TermFreq>();
				
				for (int k = 0; k < terms.length; k++)
					tfVec.add(new TermFreq(terms[k], freq[k]));
				
				Collections.sort(tfVec);
				Collections.reverse(tfVec);
				
				retVal.add(tfVec);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return retVal;
	}	
}
