package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StopWord {
	
	private final static String wordListFile = "common-english-words.txt";
	private static Set<String> stopWords = null;
	
	public static Set<String> StopWordList() {
		if (stopWords != null)
			return stopWords;
		stopWords = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(wordListFile));
			String line = br.readLine();
			String[] words = line.split(",");
			
			for (String word : words) {
				stopWords.add(word);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return stopWords;
	}
	
	public static void main(String[] args) {
		Set<String> words = StopWord.StopWordList();
		for (String word : words) {
			System.out.println(word);
		}
	}
}
