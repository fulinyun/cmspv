package Similarity.Matcher;

import java.util.List;
import java.util.Map;

public interface WebOfDataMatcher {
	
	String findMatch(String url, Map<String, List<String>> statements);

	double getMatchScore(String url, Map<String, List<String>> statements);
	
	Map<String, Double> findMatches(String url, Map<String, List<String>> statements);
}
