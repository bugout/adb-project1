package analyzer;

import java.util.List;

public abstract class QueryReorderer {
	public abstract String[] reorder(List<String> query);
}
