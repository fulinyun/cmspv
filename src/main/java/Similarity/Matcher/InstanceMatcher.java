package Similarity.Matcher;



import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Similarity.EntityFeatureModelSimilarity;
import Similarity.Similarity;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class InstanceMatcher {
	
	public static void main(String [] args){

		int level = 2;
		String onto1 = "../iimb/000/onto.owl";
		String onto2 = "../iimb/onto.owl";
		String output = "000.txt";
		
		
		Model GCMDModel = ModelFactory.createDefaultModel();
		Model CleanModel = ModelFactory.createDefaultModel();
		BufferedWriter out = null;
		try{
        	FileInputStream fstream = new FileInputStream(onto1);
        	GCMDModel.read(fstream,"");
        	FileInputStream fstream2 = new FileInputStream(onto2);
        	CleanModel.read(fstream2,"","TTL");    
        	
        	FileWriter ofstream = new FileWriter(output);
        	out = new BufferedWriter(ofstream);
        	
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Similarity sim = new EntityFeatureModelSimilarity();
		WebOfDataMatcher WDM = new EntityMatcher(GCMDModel,sim,level,1);
		
		ResIterator subjects = CleanModel.listSubjects();
		Hashtable<String, ArrayList<String>> descriptions = new Hashtable<String, ArrayList<String>>();
		getAllDescriptions(CleanModel,  descriptions);

		while(subjects.hasNext()){
			Resource subject = subjects.next();
			System.out.println("matching... ("+subject.toString()+")");
			Map<String, List<String>> d = new Hashtable<String, List<String>>();
			for (String key : descriptions.keySet()) d.put(key, descriptions.get(key));
			String match = WDM.findMatch(subject.toString(),d);

			try{
				System.out.println(subject.toString()+" "+match);
				out.write(subject.toString()+" "+match+"\n");
				out.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		try{
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
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
