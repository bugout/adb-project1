
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import analyzer.Expander;
import analyzer.TermRateExpander;
import query.*;
import searcher.*;
import util.Global;
import util.Logger;
import util.Logger.MsgType;

public class RelevanceFeedback {
	private static String apiKey;
	private static int topK = 10;
	private static double targetPrecision;
	private static String[] basicQuery;
	
	// Usage: RelevanceFeedback <ApiKey> <topK> <precision> <'query'>
	public static void main(String[] args) throws Exception {					
		
		Logger myLogger = Logger.getInstance();
		
		//get current date
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		 
		myLogger.write("Started: " + dateFormat.format(new Date()), MsgType.LOG);
		myLogger.write("Started: " + dateFormat.format(new Date()), MsgType.ERROR);
		
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
			
			if(Global.DEBUG)
			{
				for (QueryRecord r : parsedResult)
					myLogger.write(r.toString(), MsgType.ERROR);
			}
			
			if (rounds == 1 && parsedResult.size() < topK) {  // terminate if less than topk result
				System.out.println("Initial query result less than topK. Expansion exit!");
				myLogger.close();
				System.exit(1);
			}
			
			// get feedbacks from the user
			getFeedbacks(parsedResult);	
			
			precision = computePrecision(parsedResult);
			
			printTranscript(rounds, parsedResult, precision, query);
			
			if (stopExpansion(precision))
				break;
			
			//set positives to be accessed by different analyzers
			Global.setPositives(parsedResult);
			query = expander.expand(parsedResult, query);
		} 
		
		// Output
		printSummary(rounds, precision, query);
		
		myLogger.close();
	}
	
	private static void printTranscript(int rounds, Vector<QueryRecord> parsedResult, 
													double precision, String[] query) 
	{
		
		//get the Logger instance
		
		Logger myLogger = Logger.getInstance();
		
		myLogger.write("\n===========================================", MsgType.LOG);
		myLogger.write("Round " + rounds, MsgType.LOG);
		
		StringBuffer queryText = new StringBuffer();
		
		for (int i = 0; i < query.length; i++)
			queryText.append(query[i] + " ");
		
		myLogger.write("Query: " + queryText.toString() + "\n", MsgType.LOG);
		
		int count = 1;
		for (QueryRecord result : parsedResult)
		{
			myLogger.write("Result " + count, MsgType.LOG);
			
			StringBuffer sb = new StringBuffer();
			
			sb.append("Relevant: ");
			
			if (result.isRelevant())
				sb.append("YES\n[\n");
			else
				sb.append("NO\n[\n");
			
			sb.append("    URL: " + result.getUrl() + "\n");
			sb.append("    Title: " + result.getTitle() + "\n");
			sb.append("    Summary: " + result.getDescription() + "\n");
			
			sb.append("]\n");
			
			myLogger.write(sb.toString(), MsgType.LOG);
			
			count++;
		}
		
		myLogger.write("Precision : " + precision + "\n", MsgType.LOG);
		
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
		System.out.printf("Final Query: %s\n", Arrays.toString(query));
	}
}
