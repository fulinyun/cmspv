package Similarity;

import java.util.Hashtable;

public interface Similarity {
	
	double computeSimilarity(String object1, String object2, int depth);
	
	void setFirstObjectDescription(Object description1);
	
	void setSecondObjectDescription(Object description2);
	
	void setWeights(Hashtable<String, Double> weightVector);
	
	Hashtable<String, Double> getFeatureSim(String object1, String object2, int depth);

}
