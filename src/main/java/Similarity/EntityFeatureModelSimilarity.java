package Similarity;

import java.util.ArrayList;
import java.util.Hashtable;

public class EntityFeatureModelSimilarity implements Similarity {
	
	double alpha;
	double beta;
	Similarity textSim;
	Hashtable<String, ArrayList<String>> statements1;
	Hashtable<String, ArrayList<String>> statements2;
	
	
	public EntityFeatureModelSimilarity(){
		alpha = 1;
		beta = 1;
		textSim = new JaccardSimilarity();
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
		
//		System.out.println(object1 +" "+ object2);
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
			}
		}
		
		double object1_unique = topStmt1.size() - pvSimSum;
		double object2_unique = topStmt2.size() - object2_common;
		double EFMSimilarity = pvSimSum / (pvSimSum + alpha * (object1_unique) + beta * (object2_unique));
		
		return EFMSimilarity;
	}

	private double pvSimilarity(String stmt1, String stmt2,int level){
		
		String [] triple1 = stmt1.split(" ",3);
		String [] triple2 = stmt2.split(" ",3);
		
		
		if(! (triple1[1].equals(triple2[1]) || isSameProperty(triple1[1],triple2[1])))
			return 0;
			
		String object1 = triple1[2];
		String object2 = triple2[2];
		
		boolean object1IsURL = object1.startsWith("http");
		boolean object2IsURL = object2.startsWith("http");
		
		if(object1IsURL && object2IsURL){
			return computeSimilarity(object1,object2, level-1);
		}
		
		if((!object1IsURL) && (!object2IsURL)){
			return textSim.computeSimilarity(object1, object2,1);
		}
		
		
		if(object1IsURL)
			object1 = getStatmentString(object1,statements1);
		
		if(object2IsURL)
			object2 = getStatmentString(object2,statements1);
			
		return textSim.computeSimilarity(object1, object2,1); // jaccard similarity doesn't consider depth (level)
		
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
		// TODO Auto-generated method stub
		
	}

	public Hashtable<String, Double> getFeatureSim(String object1,
			String object2, int level) {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isSameProperty(String p1, String p2){
		if(p1.equals("http://www.w3.org/2004/02/skos/core#definition") && p2.equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			return true;
		
		if(p1.equals("http://www.w3.org/2004/02/skos/core#prefLabel") && p2.equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			return true;
		
		if(p1.equals("http://www.w3.org/2004/02/skos/core#prefLabel") && p2.equals("http://www.w3.org/2000/01/rdf-schema#label"))
			return true;		
		
		if(p1.equals("http://www.w3.org/2004/02/skos/core#definition") && p2.equals("http://www.w3.org/2000/01/rdf-schema#label"))
			return true;
		
		if(p2.equals("http://www.w3.org/2004/02/skos/core#definition") && p1.equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			return true;
		
		if(p2.equals("http://www.w3.org/2004/02/skos/core#prefLabel") && p1.equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			return true;
		
		if(p2.equals("http://www.w3.org/2004/02/skos/core#prefLabel") && p1.equals("http://www.w3.org/2000/01/rdf-schema#label"))
			return true;		
		
		if(p2.equals("http://www.w3.org/2004/02/skos/core#definition") && p1.equals("http://www.w3.org/2000/01/rdf-schema#label"))
			return true;
//		
		return false;
		
	}
}
