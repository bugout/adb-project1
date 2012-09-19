package query;

public class QueryRecord {
	
	private String title;
	private String url;
	private String displayUrl;
	private String description;
	
	public QueryRecord(String title, String url, String displayUrl, String description)
	{
		this.title= title;
		this.url = url;
		this.displayUrl = displayUrl;
		this.description = description;
	}
	public String getTitle() {
		return title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String displayUrl() {
		return displayUrl;
	}
	
	public String getDescription() {
		return description;
	}

}
