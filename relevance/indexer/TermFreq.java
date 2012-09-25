package indexer;

public class TermFreq implements Comparable<TermFreq> {

	private String term;
	private int freq;
	
	public TermFreq(String term, int freq) {
		this.term = term;
		this.freq = freq;
	}
	
	public String getTerm() {
		return term;
	}
	
	public int getFreq() {
		return freq;
	}
	
	public String toString() {
		return (term + " - " + freq);
	}
	
	//object o1 is greater than object o2, if 
	//o1.freq > o2.freq	
	public int compareTo(TermFreq o) {
		int retval = 0;
		if (freq > o.freq)
			return 1;
		else if (freq < o.freq)
			return -1;
		
		return retval;
	}

}
