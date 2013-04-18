package Similarity;


import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import Similarity.Matcher.EntityMatcher;
import Similarity.Matcher.WebOfDataMatcher;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class EntitySimilarityComputation {
	
	public static void main(String [] args){
		int level = 2;
		String onto1 = "../iimb/000/onto.owl";
		String onto2 = "../iimb/onto.owl";
		String output = "000.txt";
		
		Model model1 = ModelFactory.createDefaultModel();
		Model model2 = ModelFactory.createDefaultModel();
		BufferedWriter out = null;
		try{
        	FileInputStream fstream = new FileInputStream(onto1);
        	model1.read(fstream,"");
        	FileInputStream fstream2 = new FileInputStream(onto2);
        	model2.read(fstream2,"");    
        	
        	FileWriter ofstream = new FileWriter(output);
        	out = new BufferedWriter(ofstream);
        	
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Similarity sim = new EntityFeatureModelSimilarity();

		
		ResIterator subjects = model1.listSubjects();
		Hashtable<String, ArrayList<String>> descriptions1 = new Hashtable<String, ArrayList<String>>();
		getAllDescriptions(model1,  descriptions1);
		
		ResIterator subjects2 = model2.listSubjects();
		Hashtable<String, ArrayList<String>> descriptions2 = new Hashtable<String, ArrayList<String>>();
		getAllDescriptions(model2,  descriptions2);
		
		sim.setFirstObjectDescription(descriptions1);
		sim.setSecondObjectDescription(descriptions2);
		
		String object1 = "http://oaei.ontologymatching.org/2012/IIMBDATA/en/gaborone";
		String object2 = "http://oaei.ontologymatching.org/2012/IIMBDATA/en/gaborone";

//		for(String t:descriptions1.get(object1)){
//			System.out.println(t);
//		}	
//		
//		
//		System.out.println();
//		
//		for(String t:descriptions2.get(object2)){
//			System.out.println(t);
//		}	
//		
//		System.out.println("========================================");
		
		
		double score = sim.computeSimilarity(object1, object2, 2);
		
		System.out.println(object1+" "+object2+": "+score);
		

	}
	
	public static void getAllDescriptions(Model model, Hashtable<String, ArrayList<String>> descriptions){

		StmtIterator stmtItr = model.listStatements();
						
		while(stmtItr.hasNext()){
			Statement stmt = stmtItr.next();		
			String stmtString = stmt.toString().replaceAll(",", "").replaceAll("\\[", "").replaceAll("\\]", "");
			String subject = stmt.getSubject().toString();
			
			if(descriptions.keySet().contains(subject)){
				ArrayList<String> statements = descriptions.get(subject);;
				statements.add(stmtString);
				descriptions.put(subject, statements);			
			}else{
				ArrayList<String> statements = new ArrayList<String>();
				statements.add(stmtString);
				descriptions.put(subject, statements);					
			}

		}


	}

}
