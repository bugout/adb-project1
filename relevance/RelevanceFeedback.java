
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Vector;

import analyzer.Expander;
import analyzer.KeyWordFinder;
import analyzer.TermRateExpander;
import query.*;
import searcher.*;
import util.Global;

public class RelevanceFeedback {
	private static String apiKey;
	private static int topK = 10;
	private static double targetPrecision;
	private static String[] basicQuery;
	
	// Usage: RelevanceFeedback <ApiKey> <topK> <precision> <'query'>
	public static void main(String[] args) throws Exception {					
		
		readArguments(args);
				
		// searcher
		SearchProvider seacher = new BingSearchProvider(apiKey, topK);		
		// analyzer
		Expander expander = new TermRateExpander(basicQuery);				
		// query parser
		QueryResultParser queryParser = new QueryResultParser();

		int rounds = 0;
		double precision = 0;
		String[] query = basicQuery;
		while (true)  {				
						
			// begin a new round
			rounds++;
			System.out.println("Searching with keywords: " + Arrays.toString(query));
			
			// get search result from a search provider
			String result = seacher.search(query);
			
			// Parse query result using XML Parser, extract fields
			Vector<QueryRecord> parsedResult = queryParser.parseQueryResult(result); 
			
			if (rounds == 1 && parsedResult.size() < topK) {  // terminate if less than topk result
				System.out.println("Initial query result less than topK. Expansion exit!");
				System.exit(1);
			}
			
			// get feedbacks from the user
			getFeedbacks(parsedResult);	
			
			//Setting these to set global positive so that they can be accessed
			//from any analyzer
			Global.setPositives(parsedResult);
			
			query = expander.expand(parsedResult, query);
			
			precision = computePrecision(parsedResult);
			if (stopExpansion(precision))
				break;
		} 
		
		// Output
		printSummary(rounds, precision, query);
	}
	
	
	private static boolean stopExpansion(double precision) {
		if (precision == 0) // if precision is zero, terminate at once
			return true;		
		if (precision >= targetPrecision)
			return true;
		else
			return false;
	}

	private static double computePrecision(Vector<QueryRecord> results) {
	
		double positives = 0.0;
		
		for (QueryRecord result : results)
			if ( result.isRelevant() ) 
				positives++;
		
		return 1.0 * positives / results.size();
	}

	// list each result and ask for a feedback
	private static void getFeedbacks(Vector<QueryRecord> parsedResults) {		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		for (int i = 0; i < parsedResults.size(); i++) {
			System.out.printf("Result %d\n", i + 1);
			System.out.println("-----------------------------");
			System.out.printf("Title: %s\n", parsedResults.get(i).getTitle());
			System.out.printf("Url: %s\n", parsedResults.get(i).getUrl());
			System.out.printf("Description: %s\n", parsedResults.get(i).getDescription());
			while (true) {
				System.out.print("Relevance(Y/N)? :");
				try {
					String answer = br.readLine();
					if (answer.startsWith("Y") || answer.startsWith("y")) {
						parsedResults.get(i).setFeedback(true);
						break;
					}
					else if (answer.startsWith("N") || answer.startsWith("n")) {
						parsedResults.get(i).setFeedback(false);
						break;
					}
					else
						System.err.println("Sorry. I don't understand your feedback, please say again");				
				}
				catch (IOException e){
					System.err.println("Input Error!");				
				}
			}
			System.out.println("-----------------------------");
		}
	}
	
	/**
	 * Read apikey, topk, precision, query from command line arguments 
	 */
	private static void readArguments(String[] args) {
		// check argument length
		if (args.length < 4) {
			System.err.println("Usage: RelevanceFeedback <ApiKey> <topK> <precision> <'query'>");
			System.exit(1);
		}

		// get command line arguments
		apiKey = args[0];
		try {
			topK = Integer.parseInt(args[1]);
			targetPrecision = Double.parseDouble(args[2]);
			if (targetPrecision < 0 || targetPrecision > 1) { // check precision scope
				System.err.println("Please input a valid precision (0 <= precision <= 1)");
				System.exit(1);
			}
		}
		catch (NumberFormatException e) {
			System.err.println("Please input a valid argument");
			System.exit(1);
		}
		Vector<String> q = new Vector<String>();
		for (int i = 3; i < args.length; i++) {
			if (i == 3) {
				if (!args[i].startsWith("'")) {
					System.err.println("Please input a valid query, the query should be enclosed with \"'\"");
					System.exit(1);
				}
			
				q.add(args[i].replaceAll("'", ""));
				//q.add(args[i].substring(1, args[i].length()));
			}
			else if ( i == args.length - 1 ) {
				if (!args[i].endsWith("'")) {
					System.err.println("Please input a valid query, the query should be enclosed with \"'\"");
					System.exit(1);
				}
				q.add(args[i].replaceAll("'", ""));
			//q.add(args[i].substring(0, args[i].length()-1));
			}
			else
				q.add(args[i]);
			}
			basicQuery = q.toArray(new String[0]);
	}
	
	// Print summary after we meet the precision requirement
	private static void printSummary(int rounds, double precision, String[] query) {
		System.out.printf("Target precision reached in %d rounds\n", rounds);
		System.out.printf("Precision: %.2f\n", precision);
		System.out.printf("Querys: %s\n", Arrays.toString(query));
	}
}
