package searcher;

public abstract class SearchProvider {
	protected String apiKey;
	protected int topK;
	
	
	public SearchProvider(String apikey, int topK) {
		this.apiKey = apikey;
		this.topK = topK;
	}
	public abstract String search(String[] query) throws Exception;
}
