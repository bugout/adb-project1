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

}
