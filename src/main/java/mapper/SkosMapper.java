package mapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;

import util.Utils;
import Similarity.EntityFeatureModelSimilarity;
import Similarity.JaccardSimilarity;
import Similarity.Similarity;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class SkosMapper {
	
	public static void main(String[] args) {
//		mainTestEntryAndPriorityQueue();
//		mainTest();
		mainMap(args);
	}

	public static void mainTestEntryAndPriorityQueue() {
		Model m = ModelFactory.createDefaultModel();
		PriorityQueue<Map.Entry<Resource, Double>> pq= new PriorityQueue<Map.Entry<Resource, Double>>(
				3, new Comparator<Map.Entry<Resource, Double>>() {

					public int compare(Entry<Resource, Double> e1,
							Entry<Resource, Double> e2) {
						double v1 = e1.getValue();
						double v2 = e2.getValue();
						if (v1 > v2) return 1;
						if (v1 < v2) return -1;
						return 0;
					}
					
				});
		Random rand = new Random();
		for (int i = 0; i < 9; ++i) {
			Resource r = m.createResource("http://www.example.com/resource"+i);
			double s = rand.nextDouble();
			System.out.println("adding " + r + "," + s);
			AbstractMap.SimpleEntry<Resource, Double> e = new AbstractMap.SimpleEntry<Resource, Double>(r, s);
			pq.add(e);
			if (pq.size() > 3) pq.poll();
			for (Map.Entry<Resource, Double> ei : pq) System.out.println(ei.getKey().toString() + " : " + ei.getValue());
		}
		
	}
	
	public static void mainTest() {
		Model model1 = null;
		Model model2 = null;
		try {
			model1 = Utils.createSkosModel("gcmd-sciencekeywords.rdf");
			for (StmtIterator i = model1.listStatements(); i.hasNext(); ) System.out.println(i.next().toString());		
			model1.close();
			model2 = Utils.createSkosModel("nims.ttl");
			for (StmtIterator i = model2.listStatements(); i.hasNext(); ) System.out.println(i.next().toString());		
			model2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void mainMap(String[] args) {
		
		if (args.length < 5) {
			System.out.println("Usage: java mapper.SkosMapper onto1 onto2 output level prefix");
			System.out.println("onto1, onto2: for each subject in onto2, find 4 subjects in onto1 with the highest matching scores");
			System.out.println("output: a html file to hold the mapping results");
			System.out.println("level: the distance of consulting other resources when trying to map two resources");
			System.out.println("prefix: the prefix of URL to consider in onto2");
			System.out.println("example: java mapper.SkosMapper gcmd-sciencekeywords.rdf nims.ttl NIMS-GCMD-Mapping4.html 1 http://cmspv.tw.rpi.edu/rdf/");
			System.exit(0);
		}
		
		String onto1 = args[0]; // "gcmd-sciencekeywords.rdf";
		String onto2 = args[1]; // "nims.ttl";
		String output = args[2]; // "NIMS-GCMD-Mapping4.html";
		int level = Integer.parseInt(args[3]); // int level = 2;
		String prefix = args[4]; // "http://cmspv.tw.rpi.edu/rdf/";
		
		Model model1 = null;
		Model model2 = null;
		PrintWriter out = null;
		try {
			model1 = Utils.createSkosModel(onto1);
			model2 = Utils.createSkosModel(onto2);

			FileWriter ofstream = new FileWriter(output);
			out = new PrintWriter(new BufferedWriter(ofstream));
			out.println("<!doctype html>");
			out.println("<html><head><title>"+onto2+" to "+onto1+" mapping</title></head><body>");
			out.println("<table border=1>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		Similarity sim = new EntityFeatureModelSimilarity();
		ResIterator subjects = model2.listSubjects();

		// for each subject in model2 (args[1]), find 4 matches from model1 (args[0])
		while (subjects.hasNext()) {
			Resource subject = subjects.next();
			if (!subject.toString().startsWith(prefix)) continue;
			System.out.println("matching... (" + subject.toString() + ")");

			PriorityQueue<Map.Entry<Resource, Double>> matches = new PriorityQueue<Map.Entry<Resource, Double>>(
					4, new Comparator<Map.Entry<Resource, Double>>() {

						public int compare(Entry<Resource, Double> e1,
								Entry<Resource, Double> e2) {
							double v1 = e1.getValue();
							double v2 = e2.getValue();
							if (v1 > v2) return 1;
							if (v1 < v2) return -1;
							return 0;
						}
				
					});
			HashMap<Resource, String> explanation = new HashMap<Resource, String>();
			findMatches(subject, model2, model1, sim, level, 4, matches, explanation);

			for (Map.Entry<Resource, Double> match : matches) {
				Resource s = match.getKey();
				Property p = ResourceFactory
						.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel");
				RDFNode v = null;

				StmtIterator itr = model1.listStatements(s, p, v);
				String labels = "";
				while (itr.hasNext()) {
					Statement stmt = itr.next();
					String object = stmt.getObject().toString();
					labels += object + " ";
				}

				try {
					StmtIterator gitr = model2.listStatements(subject, p, v);
					String glabels = "";
					while (gitr.hasNext()) {
						Statement stmt = gitr.next();
						String object = stmt.getObject().toString();
						glabels += object + " ";
					}
					out.println("<tr>");
					out.println(tableFormat(subject.toString(), s.toString(), glabels,
							labels, match.getValue(), explanation.get(s)));
					out.println("</tr>");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		try {
			out.println("</table></body></html>");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param subject a resource we'd like to find matches for
	 * @param model the model subject comes from
	 * @param target the model we'd like to find matching resources (with subject) from
	 * @param sim the Similarity object used to calculate resource similarity (e.g., EntityFeatureModelSimilarity)
	 * @param level the depth we'd like to trace down the property links to consider related resources for the matching
	 * @param nummatch the number of matches we'd like to find from the resources in target for the resource subject
	 * @param matches a map used to store matching results, keys are resources from model target, values are double matching scores
	 * @param explanation a map used to store matching explanations, keys are resources from model target, values are textual 
	 * explanations for matching scores
	 */
	private static void findMatches(Resource subject, Model model, Model target, Similarity sim,
			int level, int nummatch, PriorityQueue<Map.Entry<Resource, Double>> matches,
			HashMap<Resource, String> explanation) {
		ResIterator i = target.listSubjects();
		while (i.hasNext()) {
			Resource r2 = i.next();
			double score = sim.computeSimilarity(subject, model, r2, target, level);
			matches.add(new AbstractMap.SimpleEntry<Resource, Double>(r2, score));
			if (matches.size() > 4) matches.poll();
		}
		for (Map.Entry<Resource, Double> e : matches) {
			String exp = sim.explainSimilarity(subject, model, e.getKey(), target, level, e.getValue());
			explanation.put(e.getKey(), exp);
		}
	}

	public static String updateVotes(String myconcept,
			Hashtable<String, ArrayList<String>> broaders,
			LinkedHashMap<String, Double> votes, double voteScore) {
		double topVote = 0;
		String topVoteString = "";
		// System.out.println(narrower+" "+narrowers.contains(narrower));

		// System.out.println("%"+narrower+"%");
		// for(String key:narrowers.keySet()){
		// if(key.contains("SOLAR")){
		// System.out.println("%"+key+"%:");
		// for(String value:narrowers.get(key))
		// System.out.print(value+", ");
		// System.out.println();
		// // try{
		// // System.in.read();
		// // }catch(Exception e){
		// //
		// // }
		// }
		// }
		if (!broaders.keySet().contains(myconcept))
			return topVoteString;

		for (String broader : broaders.get(myconcept)) {
			if (votes.keySet().contains(broader)) {
				double vote = votes.get(broader) + voteScore;

				votes.put(broader, vote);
			} else {
				votes.put(broader, voteScore);
				topVoteString = broader;
			}
		}

		for (String concept : votes.keySet()) {
			if (votes.get(concept) > topVote) {
				topVoteString = concept;
				topVote = votes.get(concept);
			}
		}
		return topVoteString;
	}

	public static String tableFormat(String e1, String e2, String notes,
			String notes2, double scores, String explain) {
//		System.out.println(e1 + ", " + e2 + ", " + explain);
		String output = "";
		output += "<td>" + e1 + "</td>";
		output += "<td>" + notes + "</td>";
		output += "<td>" + e2 + "</td>";
		output += "<td>" + notes2 + "</td>";
		output += "<td title=\""+explain+"\">" + scores + "</td>";
		return output;
	}

	public static String formatOutput(String e1, String e2, String notes) {
		String output = "<match>\n";
		output += "<entity1 rdf:resource=\"";
		output += e1;
		output += "\" />\n";
		output += "<entity2 rdf:resource=\"";
		output += e2;
		output += "\" />\n";
		output += "<Note>";
		output += notes;
		output += "</Note>\n";
		output += "</match>\n";
		return output;

	}

	public static void getAllDescriptions(Model model,
			Hashtable<String, ArrayList<String>> descriptions) {

		StmtIterator stmtItr = model.listStatements();

		while (stmtItr.hasNext()) {
			Statement stmt = stmtItr.next();
			String stmtString = stmt.toString().replaceAll(",", "")
					.replaceAll("\\[", "").replaceAll("\\]", "");
			String subject = stmt.getSubject().toString();

			if (descriptions.keySet().contains(subject)) {
				ArrayList<String> statements = descriptions.get(subject);
				statements.add(stmtString);
				descriptions.put(subject, statements);
			} else {
				ArrayList<String> statements = new ArrayList<String>();
				statements.add(stmtString);
				descriptions.put(subject, statements);
			}

		}

	}

	public static String getLiterals(String object,
			Hashtable<String, ArrayList<String>> descriptions, int level) {
		String rStr = "";
		if (!descriptions.containsKey(object) || level == 0)
			return object;

		for (String t : descriptions.get(object)) {
			// System.out.println(t);
			String nobject = t.split(" ", 3)[2];
			rStr = rStr + " " + getLiterals(nobject, descriptions, level - 1);
		}
		return rStr;
	}

	public static ArrayList<String> sortedKey(Hashtable<String, Double> matches) {
		ArrayList<String> keys = new ArrayList<String>(matches.keySet());
		for (int i = 0; i < keys.size(); i++) {
			for (int j = i + 1; j < keys.size(); j++) {
				if (matches.get(keys.get(j)) > matches.get(keys.get(i))) {
					String tkey = keys.get(i);
					keys.set(i, keys.get(j));
					keys.set(j, tkey);
				}
			}
		}
		return keys;

	}

}
