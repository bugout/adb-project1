import searcher.*;

public class RelevanceFeedback {

	public static void main(String[] args) throws Exception {
		SearchProvider sp = new BingSearchProvider();
		
		String[] query = new String[]{"bill", "gates"};
		sp.search(query);
	}
}
