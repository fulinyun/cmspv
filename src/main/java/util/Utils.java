package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class Utils {

	// sample usage: Map<Resource, List<Statement>> ret = getAllDescriptions(model, new HashMap<Resource, List<Statement>>());
	public static Map<Resource, List<Statement>> getAllDescriptions(Model model, Map<Resource, List<Statement>> descriptions) {
		StmtIterator stmtItr = model.listStatements();

		while (stmtItr.hasNext()) {
			Statement stmt = stmtItr.next();
			Resource subject = stmt.getSubject();

			if (descriptions.keySet().contains(subject)) descriptions.get(subject).add(stmt);
			else {
				ArrayList<Statement> statements = new ArrayList<Statement>();
				statements.add(stmt);
				descriptions.put(subject, statements);
			}

		}

		return descriptions;
	}

	// sample usage: Map<String, List<String>> ret = getAllDescriptionStrings(model, new HashMap<String, List<String>>());
	public static Map<String, List<String>> getAllDescriptionStrings(Model model, Map<String, List<String>> descriptions) {
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

	// sample usage: List<Statement> ret = getDescriptions(model, subject, new ArrayList<Statement>());
	public static List<Statement> getDescriptions(Model model,
			Resource subject, List<Statement> descriptions) {
		StmtIterator stmtItr = model.listStatements(subject, (Property)null, (RDFNode)null);

		while (stmtItr.hasNext()) {
			Statement stmt = stmtItr.next();
			descriptions.add(stmt);
		}

		return descriptions;
	}
	
	// sample usage: List<String> ret = getDescriptionStrings(model, subject, new ArrayList<String>());
	public static List<String> getDescriptionStrings(Model model,
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

	public static String getDescriptionString(String s, Model m) {
		List<String> stmts = getDescriptionStrings(m, s, new ArrayList<String>());

		StringBuilder stmtString = new StringBuilder();
		for (String stmt : stmts) {
			String object = stmt.split(" ", 3)[2];
			object = object.replaceAll(
					"\\^\\^http://www.w3.org/2001/XMLSchema#.*", "")
					.replaceAll("http:/.*(#|/)", "");
			stmtString.append(object);
			stmtString.append(" ");
		}
		return stmtString.toString();
	}

	public static void testGetDescriptions(String filename, String[] subjects) {
		Model model = null;
		try {
			model = Utils.createSkosModel(filename);
			Map<String, List<String>> allDescriptionStrings = getAllDescriptionStrings(model, new HashMap<String, List<String>>());
			Map<Resource, List<Statement>> allDescriptions = getAllDescriptions(model, new HashMap<Resource, List<Statement>>());
			
			for (String subjectString : subjects) {
				Resource subject = model.createResource(subjectString);
			
				List<String> descriptionStrings = getDescriptionStrings(model, subjectString, new ArrayList<String>());
				Collections.sort(descriptionStrings);
				List<Statement> descriptions = getDescriptions(model, subject, new ArrayList<Statement>());
			
				for (String s : descriptionStrings) System.out.println(s);
				System.out.println("==^==getDescriptionStrings==v==getAllDescriptionStrings==");
				List<String> t = allDescriptionStrings.get(subjectString);
				Collections.sort(t);
				for (String s : t) System.out.println(s);
				System.out.println();
			
				class StmtComp implements Comparator<Statement> {
					public int compare(Statement a, Statement b) {
						String as = a.toString();
						String bs = b.toString();
						return as.compareTo(bs);
					}					
				}
				
				Collections.sort(descriptions, new StmtComp());

				for (Statement s : descriptions) System.out.println(s.toString());
				System.out.println("==^==getDescriptions==v==getAllDescriptions==");
				List<Statement> tt = allDescriptions.get(subject);
				Collections.sort(tt, new StmtComp());
				for (Statement s : tt) System.out.println(s);
				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testGetPrefLabelOrLocalNames(String filename, String[] subjects) {
		Model model = null;
		try {
			model = Utils.createSkosModel(filename);
			
			for (String subjectString : subjects) {
				Resource subject = model.createResource(subjectString);
				String label = getPrefLabelOrLocalName(model, subject);
				System.out.println(subject.getURI() + " : " + label);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testLabelify(String filename, String[] subjects) {
		Model model = null;
		try {
			model = Utils.createSkosModel(filename);
			
			for (String subjectString : subjects) {
				Resource subject = model.createResource(subjectString);
				StmtIterator si = model.listStatements(subject, (Property)null, (RDFNode)null);
				while (si.hasNext()) {
					System.out.println(labelify(model, si.next()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * get prefLabel, or local name if prefLabel is not available, of resource from model 
	 * @param model
	 * @param resource
	 * @return
	 */
	public static String getPrefLabelOrLocalName(Model model, Resource resource) {
		NodeIterator ni = model.listObjectsOfProperty(resource, model.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"));
		if (ni.hasNext()) {
			Literal lv = ni.next().asLiteral();
			return lv.getString();
		}
		else return resource.getLocalName();
	}

	public static void main(String[] args) {
		String[] gcmdStrings = {"http://gcmdservices.gsfc.nasa.gov/kms/concept/cabd97d6-aa6c-48b8-963b-79248634ce5d",
			"http://gcmdservices.gsfc.nasa.gov/kms/concept/22c14e35-48a4-40b5-a503-add48c2d4cd4"};
		testLabelify("gcmd-sciencekeywords.rdf", gcmdStrings);
		System.out.println();
		String[] nimsStrings = {"http://cmspv.tw.rpi.edu/rdf/vocab/nims/term/0082", 
			"http://cmspv.tw.rpi.edu/rdf/vocab/nims/term/0053"};
		testLabelify("nims.ttl", nimsStrings);

//		String[] gcmdStrings = {"http://gcmdservices.gsfc.nasa.gov/kms/concept/cabd97d6-aa6c-48b8-963b-79248634ce5d",
//			"http://gcmdservices.gsfc.nasa.gov/kms/concept/22c14e35-48a4-40b5-a503-add48c2d4cd4"};
//		testGetPrefLabelOrLocalNames("gcmd-sciencekeywords.rdf", gcmdStrings);
//		System.out.println();
//		String[] nimsStrings = {"http://cmspv.tw.rpi.edu/rdf/vocab/nims/term/0082", 
//			"http://cmspv.tw.rpi.edu/rdf/vocab/nims/term/0053"};
//		testGetPrefLabelOrLocalNames("nims.ttl", nimsStrings);

//		String[] gcmdStrings = {"http://gcmdservices.gsfc.nasa.gov/kms/concept/cabd97d6-aa6c-48b8-963b-79248634ce5d",
//				"http://gcmdservices.gsfc.nasa.gov/kms/concept/22c14e35-48a4-40b5-a503-add48c2d4cd4"};
//		testGetDescriptions("gcmd-sciencekeywords.rdf", gcmdStrings);
//		String[] nimsStrings = {"http://cmspv.tw.rpi.edu/rdf/vocab/nims/term/0082", 
//				"http://cmspv.tw.rpi.edu/rdf/vocab/nims/term/0053"};
//		testGetDescriptions("nims.ttl", nimsStrings);
	}

	/**
	 * extract labels from subject, predicate and object of a statement with getPrefLabelOrLocalName() to form a succinct representation of the statement
	 * @param m
	 * @param stmt
	 * @return
	 */
	public static String labelify(Model model, Statement stmt) {
		String obj;
		RDFNode r = stmt.getObject();
		if (r.isResource()) obj = getPrefLabelOrLocalName(model, r.asResource());
		else obj = r.toString();
		return getPrefLabelOrLocalName(model, stmt.getSubject()) + " <" + getPrefLabelOrLocalName(model, stmt.getPredicate()) + 
				"> " + obj;
	}
}
