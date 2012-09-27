package util;

import java.util.Comparator;
import java.util.Map;

public class MapValueComparator implements Comparator<String> {

    public MapValueComparator(Map<String, Double> theMap) {
        this.theMap = theMap;
    }

	Map<String, Double> theMap;
	@Override
	public int compare(String o1, String o2) {	
        if (theMap.get(o1) >= theMap.get(o2)) {
            return -1;
        } else {
            return 1;
        }

	}

}
