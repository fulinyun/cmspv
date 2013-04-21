package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class Utils {
	
	// sample usage: Map<String, List<String>> ret = getAllDescriptions(model, new HashMap<String, List<String>>());
	public static Map<String, List<String>> getAllDescriptions(Model model, Map<String, List<String>> descriptions) {
		StmtIterator stmtItr = model.listStatements();

		while (stmtItr.hasNext()) {
			Statement stmt = stmtItr.next();
			String stmtString = stmt.toString().replaceAll(",", "")
					.replaceAll("\\[", "").replaceAll("\\]", "");
			String subject = stmt.getSubject().toString();

			if (descriptions.keySet().contains(subject)) descriptions.get(subject).add(stmtString);
			else {
				ArrayList<String> statements = new ArrayList<String>();
				statements.add(stmtString);
				descriptions.put(subject, statements);
			}

		}

		return descriptions;
	}

	// sample usage: List<String> ret = getDescriptions(model, subject, new ArrayList<String>());
	public static List<String> getDescriptions(Model model,
			String subject, List<String> descriptions) {
		StmtIterator stmtItr = model.listStatements(model.createResource(subject), (Property)null, (RDFNode)null);

		while (stmtItr.hasNext()) {
			Statement stmt = stmtItr.next();
			String stmtString = stmt.toString().replaceAll(",", "")
					.replaceAll("\\[", "").replaceAll("\\]", "");

			descriptions.add(stmtString);
		}

		return descriptions;
	}
	
	public static Model createSkosModel(String filename) throws FileNotFoundException {
		Model base = ModelFactory.createDefaultModel();
		
		FileInputStream fstream = new FileInputStream(filename);
		String lang = null;
		if (filename.toLowerCase().endsWith(".ttl")) lang = "TTL"; 
		base.read(fstream, "", lang);
		
		base.read("http://www.w3.org/2009/08/skos-reference/skos.rdf");
		
		List<Rule> ruleList = Rule.rulesFromURL("file:skos.rules");
		GenericRuleReasoner reasoner = new GenericRuleReasoner(ruleList);

		return ModelFactory.createInfModel(reasoner, base);

	}

	public static void main(String[] args) {
		Model model1 = null;
		Model model2 = null;
		try {
			model1 = Utils.createSkosModel("gcmd-sciencekeywords.rdf");
			Map<String, List<String>> allDescriptions = getAllDescriptions(model1, new HashMap<String, List<String>>());
			String subject = "http://gcmdservices.gsfc.nasa.gov/kms/concept/cabd97d6-aa6c-48b8-963b-79248634ce5d";
			List<String> descriptions = getDescriptions(model1, subject, new ArrayList<String>());
			for (String s : descriptions) System.out.println(s);
			System.out.println("==^==getDescriptions==v==getAllDescriptions==");
			for (String s : allDescriptions.get(subject)) System.out.println(s);
			System.out.println();
			
			subject = "http://gcmdservices.gsfc.nasa.gov/kms/concept/22c14e35-48a4-40b5-a503-add48c2d4cd4";
			descriptions = getDescriptions(model1, subject, new ArrayList<String>());
			for (String s : descriptions) System.out.println(s);
			System.out.println("==^==getDescriptions==v==getAllDescriptions==");
			for (String s : allDescriptions.get(subject)) System.out.println(s);
			System.out.println();
			model1.close();
			
			model2 = Utils.createSkosModel("nims.ttl");
			allDescriptions = getAllDescriptions(model2, new HashMap<String, List<String>>());
			subject = "http://cmspv.tw.rpi.edu/rdf/vocab/nims/term/0082";
			descriptions = getDescriptions(model2, subject, new ArrayList<String>());
			for (String s : descriptions) System.out.println(s);
			System.out.println("==^==getDescriptions==v==getAllDescriptions==");
			for (String s : allDescriptions.get(subject)) System.out.println(s);
			System.out.println();

			subject = "http://cmspv.tw.rpi.edu/rdf/vocab/nims/term/0053";
			descriptions = getDescriptions(model2, subject, new ArrayList<String>());
			for (String s : descriptions) System.out.println(s);
			System.out.println("==^==getDescriptions==v==getAllDescriptions==");
			for (String s : allDescriptions.get(subject)) System.out.println(s);
			System.out.println();
			model2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
