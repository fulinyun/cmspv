package gcis.imsap;

import Similarity.JaccardSimilarity;
import Similarity.Similarity;

public class JustForTest {
	
	public static void main(String [] args){
		
		Similarity sim = new JaccardSimilarity();
		double score = sim.computeSimilarity("Ocean", "Oceans", 3);
		System.out.println(score);
	}

}
