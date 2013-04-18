package Similarity;

import java.util.ArrayList;
import java.util.Hashtable;

public class WeightedFeatureModelSimilarity implements Similarity{
	double alpha;
	double beta;
	Similarity textSim;
	Hashtable<String, Double> weightVector;
	Hashtable<String, Double> featureSimVec;
	int highlevel = 2;
	
	Hashtable<String, ArrayList<String>> statements1;
	Hashtable<String, ArrayList<String>> statements2;
	
	double weightSum;
	
	
	public WeightedFeatureModelSimilarity(){
		alpha = 1;
		beta = 1;
		textSim = new JaccardSimilarity();
		weightVector = new Hashtable<String, Double>();
		featureSimVec = new Hashtable<String, Double>(); 
		weightSum = 0;
		
	}

	public void setFirstObjectDescription(Object description1) {
		statements1 = (Hashtable<String, ArrayList<String>>)description1;	
	}

	public void setSecondObjectDescription(Object description2) {
		statements2 = (Hashtable<String, ArrayList<String>>)description2;	
	}

	public double computeSimilarity(String object1,  String object2, int level) {

		
		if(level ==0)
			return textSim.computeSimilarity(object1,object2,level);
		
		ArrayList<String> topStmt1 = statements1.get(object1);
		ArrayList<String> topStmt2 = statements2.get(object2);

		if(topStmt1==null || topStmt2==null)
			return textSim.computeSimilarity(object1,object2,level);
		
		ArrayList<String> matchedStmt2 =  new ArrayList<String>();
		
		double pvSimSum = 0.0;
		double object2_common = 0;
		for(String stmt1:topStmt1){
			double maxPVSim = 0;
			String currentProcessedStmt2 = null;
			for(String stmt2:topStmt2){
				
				double pvSim = pvSimilarity(stmt1,stmt2, level);

				if(pvSim > maxPVSim){
					maxPVSim = pvSim;
					currentProcessedStmt2 = stmt2;
				}
			}
			if(maxPVSim != 0){

				if(!matchedStmt2.contains(currentProcessedStmt2)){
					matchedStmt2.add(currentProcessedStmt2);
					object2_common += maxPVSim;
				}	
				pvSimSum += maxPVSim;
				featureSimVec.put(stmt1, maxPVSim);
			}
		}
		
		double object1_unique = weightSum - pvSimSum;
		double object2_unique = weightSum - object2_common;

		double EFMSimilarity = pvSimSum / (pvSimSum + alpha * (object1_unique) + beta * (object2_unique));
		
		return EFMSimilarity;
	}

	private double pvSimilarity(String stmt1, String stmt2,int level){
 
		String [] triple1 = stmt1.split(" ",3);
		String [] triple2 = stmt2.split(" ",3);
			
		if(! (triple1[1].equals(triple2[1]) || isSameProperty(triple1[1],triple2[1])))
			return 0;
		
		double weight = weightVector.get(triple1[1]);
		if(weight ==0 )
			weight = weightVector.get(triple2[1]);
		
		if(weight == 0)
			return 0;
		
		if(level == highlevel)
			weightSum += weight;
		
		String object1 = triple1[2];
		String object2 = triple2[2];	
		boolean object1IsURL = object1.startsWith("http");
		boolean object2IsURL = object2.startsWith("http");
		
		if(object1IsURL && object2IsURL){
			return weight*computeSimilarity(object1,object2, level-1);
		}
		
		if((!object1IsURL) && (!object2IsURL)){
			return weight*textSim.computeSimilarity(object1, object2,1);
		}
		
		if(object1IsURL)
			object1 = getStatmentString(object1,statements1);
		
		if(object2IsURL)
			object2 = getStatmentString(object2,statements1);
			
		return weight*textSim.computeSimilarity(object1, object2,1);
		
	}
	
	public String getStatmentString(String url,Hashtable<String, ArrayList<String>> stmts){
		ArrayList<String> thisStmts = stmts.get(url);
		
		if(thisStmts == null)
			return "";
		
		StringBuilder stmtString = new StringBuilder();
		for(String stmt:thisStmts){
			String object = stmt.split(" ",3)[2];
			object = object.replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#.*","").replaceAll("http:/.*(#|/)", "");
			stmtString.append(object);
			stmtString.append(" ");
		}
		return stmtString.toString();
	}
	
	
	public void setWeights(Hashtable<String, Double> weightVector) {
		this.weightVector = weightVector;	
	}

	public Hashtable<String, Double> getFeatureSim(String object1,  String object2, int level) {
		if(featureSimVec.isEmpty())
			computeSimilarity(object1,object2,level);
		return featureSimVec;
	}

	private boolean isSameProperty(String p1, String p2){
		p1 = p1.replaceAll("http://www.instancematching.org/IIMB2012/ADDONS#", "http://oaei.ontologymatching.org/2012/IIMBTBOX/");
		p2 = p2.replaceAll("http://www.instancematching.org/IIMB2012/ADDONS#", "http://oaei.ontologymatching.org/2012/IIMBTBOX/");
		return p1.equals(p2);
	}

}
