package Similarity;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface Similarity {

	/**
	 * 
	 * @param r1 resource from model m1
	 * @param m1 
	 * @param r2 resource from model m2
	 * @param m2
	 * @param depth the depth we'd like to trace down the property links to consider related resources for the matching
	 * @return a double similarity score
	 */
	double computeSimilarity(Resource r1, Model m1, Resource r2, Model m2, int depth);
	
	/**
	 * 
	 * @param weightVector a map mapping properties to their corresponding weights
	 */
	void setWeights(Map<Property, Double> weightVector);
	
	
//	Hashtable<String, Double> getFeatureSim(String object1, String object2, int depth);

}
