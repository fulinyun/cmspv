package gcis.imsap;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class BroaderVoting {
	public static void main(String [] args){
		
		try{
        	FileInputStream fstream = new FileInputStream("scienceKeywords.txt");
    		Model m = ModelFactory.createDefaultModel();
    		m.read(fstream,"");
    		Hashtable<String, ArrayList<String>> output = new Hashtable<String, ArrayList<String>>();   		
    		processNarrower(output,m);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public static Hashtable<String,ArrayList<String>> processNarrower(Hashtable<String,ArrayList<String>> output, Model m){
		try{
			String queryString = "SELECT DISTINCT ?blabel ?nlabel WHERE {" +
					"{" +
					"?narrower <http://www.w3.org/2004/02/skos/core#broader> ?broader. " +
					"?broader <http://www.w3.org/2004/02/skos/core#prefLabel> ?blabel. " +
					"?narrower <http://www.w3.org/2004/02/skos/core#prefLabel> ?nlabel. " +
					"}" +
					"UNION" +
					"{" +
					"?broader <http://www.w3.org/2004/02/skos/core#narrower> ?narrower. " +
					"?broader <http://www.w3.org/2004/02/skos/core#prefLabel> ?blabel. " +
					"?narrower <http://www.w3.org/2004/02/skos/core#prefLabel> ?nlabel. " +
					"}" +
					"}";
			ArrayList<String> topLevel = new ArrayList<String>();
			output = getQueryResult(m, queryString, topLevel);	
			Hashtable<String, ArrayList<String>> finaltree = new Hashtable<String, ArrayList<String>>();
			depthFirstConstruct(topLevel, output,1, finaltree);

			return finaltree;
//			printToFile("gcmd.js",output,topLevel);
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	

	public static void printToFile(String filename, Hashtable<String, ArrayList<String>> output, ArrayList<String> topLevel){
		try{
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			depthFirstPrint(topLevel, output, 1, "", out);
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static ArrayList<String> depthFirstConstruct(ArrayList<String> topLevel, Hashtable<String, ArrayList<String>> output, int level, Hashtable<String, ArrayList<String>> finaltree) throws Exception{
	//	Hashtable<String, ArrayList<String>> out = new Hashtable<String, ArrayList<String>> ();
		Set<String> thisLevelOutput = new LinkedHashSet<String>();
		if(topLevel.size() <= 0){
			return null;
		}
		int currentIndex = 0;
		for(String thisLevel:topLevel){
			currentIndex ++;
//			System.out.println(level+" "+thisLevel);
//			out.write("{ "+
//					"\"name\":\""+thisLevel+"\"");
			thisLevelOutput = new LinkedHashSet<String>();
			thisLevelOutput.add(thisLevel);
			if(output.get(thisLevel)!= null && output.get(thisLevel).size()>0){
//				out.put(",\n\"parents\": [\n");
//				thisLevelOutput.add(thisLevel);
				thisLevelOutput.addAll(depthFirstConstruct(output.get(thisLevel),output,level +1, finaltree));
//				thisLevelOutput.add(thisLevel, output.get(thisLevel));
				
				
			}
			finaltree.put(thisLevel, new ArrayList<String>(thisLevelOutput));

//			out.write("}");
//			if(currentIndex < topLevel.size()&&level != 1){
//				out.write(",\n");
//			}
		}
//		System.out.println(outputString);
		//return outputString;

		return  new ArrayList<String>(thisLevelOutput);
	}
	public static void depthFirstPrint(ArrayList<String> topLevel, Hashtable<String, ArrayList<String>> output, int level, String outputString, BufferedWriter out) throws Exception{
		if(topLevel.size() <= 0)
			return;
		int currentIndex = 0;
		for(String thisLevel:topLevel){
			currentIndex ++;
//			System.out.println(level+" "+thisLevel);
			out.write("{ "+
					"\"name\":\""+thisLevel+"\"");		
			if(output.get(thisLevel)!= null && output.get(thisLevel).size()>0){
//				System.out.println(" hasNarrower ");
				out.write(",\n\"parents\": [\n");
				depthFirstPrint(output.get(thisLevel),output,level +1, outputString, out);
				out.write("]\n");
			}
			out.write("}");
			if(currentIndex < topLevel.size()&&level != 1){
				out.write(",\n");
			}
		}
//		System.out.println(outputString);
		//return outputString;
	}
	public static Hashtable<String, ArrayList<String>> getQueryResult(Model model, String queryString, ArrayList<String> topLevel)
	{
		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
		Hashtable<String,ArrayList<String>> output = new Hashtable<String,ArrayList<String>>();
//		ArrayList<String> broaders = new ArrayList<String>();
		try {
			ResultSet queryResults = qe.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String broader = qs.get("?blabel").toString().replaceAll("@en", "");
				String narrower = qs.get("?nlabel").toString().replaceAll("@en", "");
//				broaders.add(broader);
				if(output.keySet().contains(narrower)){
					output.get(narrower).add(broader);			
				}else{
					ArrayList<String> broaderlist = new ArrayList<String> ();
					broaderlist.add(broader);
					output.put(narrower, broaderlist);
				}
			}
			
			qe.close();
			int count = 0;
			for(String narrower:output.keySet()){
//				if(!broaders.contains(narrower)){
					topLevel.add(narrower);
					System.out.println(narrower);
//				}else
//					count++;
			}
			System.out.println(topLevel.size()+" vs "+count);

		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}
