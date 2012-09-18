package searcher;

public abstract class SearchProvider {
	protected static String apiKey;
	protected static int topK;
	
	
	public SearchProvider(String apikey, int topK) {
		this.apiKey = apikey;
		this.topK = topK;
	}
	public abstract String search(String[] query) throws Exception;
}
