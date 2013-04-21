package Similarity.Matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import util.Utils;
import Similarity.Similarity;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

public class EntityMatcher implements WebOfDataMatcher {

	Model model;
	Similarity similarity;
	int level;
	String url;
	double score;
	int numMatches;

	public EntityMatcher(Model m, Similarity s, int level, int numMatches) {
		model = m;
		similarity = s;
		this.level = level;
		score = 0;
		url = "";
		this.numMatches = numMatches;
	}

	public String findMatch(String url, Map<String, List<String>> description) {
		Hashtable<String, ArrayList<String>> d = new Hashtable<String, ArrayList<String>>();
		for (String key : description.keySet())
			d.put(key, (ArrayList<String>) description.get(key));
		return findMatch(url, d);
	}

	public String findMatch(String url,
			Hashtable<String, ArrayList<String>> description) {
		double maxSimScore = 0.0;
		String matchRes = "";
		ResIterator subjects = model.listSubjects();
		Map<String, List<String>> thisDescriptions = Utils.getAllDescriptions(model, new HashMap<String, List<String>>());

		while (subjects.hasNext()) {
			Resource subject = subjects.next();
			similarity.setFirstObjectDescription(thisDescriptions);
			similarity.setSecondObjectDescription(description);
			float start = System.currentTimeMillis();
			// System.out.println("start: "+start);
			double score = similarity.computeSimilarity(subject.toString(),
					url, level);
			float end = System.currentTimeMillis();
			System.out.println("time: " + (end - start));
			if (score > maxSimScore) {
				maxSimScore = score;
				matchRes = subject.toString();

			}
		}
		score = maxSimScore;
		this.url = url;
		return matchRes;
	}

	public double getMatchScore(String url,
			Map<String, List<String>> description) {
		Hashtable<String, ArrayList<String>> d = new Hashtable<String, ArrayList<String>>();
		for (String key : description.keySet())
			d.put(key, (ArrayList<String>) description.get(key));
		return getMatchScore(url, d);
	}

	public double getMatchScore(String url,
			Hashtable<String, ArrayList<String>> description) {
		if (!this.url.equals(url))
			findMatch(url, description);
		return score;
	}

	public Map<String, Double> findMatches(String url,
			Map<String, List<String>> description) {
		Hashtable<String, ArrayList<String>> d = new Hashtable<String, ArrayList<String>>();
		for (String key : description.keySet())
			d.put(key, (ArrayList<String>) description.get(key));
		return findMatches(url, d);
	}

	public Hashtable<String, Double> findMatches(String url,
			Hashtable<String, ArrayList<String>> description) {
		double maxSimScore = 0.0;
		// ArrayList<String> matches = new ArrayList<String> ();
		Hashtable<String, Double> match_score = new Hashtable<String, Double>();
		// String matchRes = "";
		ResIterator subjects = model.listSubjects();
		Map<String, List<String>> thisDescriptions = Utils.getAllDescriptions(model, new HashMap<String, List<String>>());
		ArrayList<String> processed = new ArrayList<String>();
		while (subjects.hasNext()) {
			Resource subject = subjects.next();
			similarity.setFirstObjectDescription(thisDescriptions);
			similarity.setSecondObjectDescription(description);
			// System.out.println(System.currentTimeMillis());
			String sub = subject.toString();
			// if(processed.contains(sub))
			// continue;

			double score = similarity.computeSimilarity(sub, url, level);
			// System.out.println(System.currentTimeMillis());
			updateMatches(match_score, sub, score);
			// processed.add(sub);
		}
		for (String key : match_score.keySet()) {
			System.out.println(match_score.get(key));
		}
		return match_score;
	}

	private void updateMatches(Hashtable<String, Double> matches, String url,
			double score) {

		if (matches.keySet().size() < numMatches
				&& !matches.keySet().contains(url)) {
			matches.put(url, score);
			return;
		}
		String minKey = "";
		double minScore = score;
		for (String key : matches.keySet()) {
			if (matches.get(key) < minScore && !matches.keySet().contains(url)) {
				minKey = key;
				minScore = matches.get(key);
			}
		}
		if (!minKey.equals("") && !matches.keySet().contains(url)) {
			matches.remove(minKey);
			matches.put(url, score);
		}
	}
}
