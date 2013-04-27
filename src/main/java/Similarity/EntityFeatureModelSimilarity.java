package Similarity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import util.Utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

public class EntityFeatureModelSimilarity implements Similarity {

	double alpha;
	double beta;
	Similarity textSim;

	public EntityFeatureModelSimilarity() {
		alpha = 1;
		beta = 1;
		textSim = new JaccardSimilarity();
	}

	public double computeSimilarity(RDFNode r1, Model m1, RDFNode r2, Model m2, int level) {

		if (level == 0 || !r1.isResource() || !r2.isResource())
			return textSim.computeSimilarity(r1, m1, r2, m2, level);

		List<Statement> topStmt1 = Utils.getDescriptions(m1, r1.asResource(), new ArrayList<Statement>());
		List<Statement> topStmt2 = Utils.getDescriptions(m2, r2.asResource(), new ArrayList<Statement>());

		if (topStmt1.isEmpty() || topStmt2.isEmpty())
			return textSim.computeSimilarity(r1, m1, r2, m2, level);

		HashSet<Statement> matchedStmt2 = new HashSet<Statement>();

		double pvSimSum = 0.0;
		double object2_common = 0.0;
		for (Statement stmt1 : topStmt1) {
			double maxPVSim = 0.0;
			Statement currentProcessedStmt2 = null;
			for (Statement stmt2 : topStmt2) {
				double pvSim = pvSimilarity(stmt1, m1, stmt2, m2, level);
				if (pvSim > maxPVSim) {
					maxPVSim = pvSim;
					currentProcessedStmt2 = stmt2;
				}
			}
			if (maxPVSim != 0.0) {

				if (!matchedStmt2.contains(currentProcessedStmt2)) {
					matchedStmt2.add(currentProcessedStmt2);
					object2_common += maxPVSim;
				}
				pvSimSum += maxPVSim;
			}
		}

		double object1_unique = topStmt1.size() - pvSimSum;
		double object2_unique = topStmt2.size() - object2_common;
		double EFMSimilarity = pvSimSum
				/ (pvSimSum + alpha * (object1_unique) + beta
						* (object2_unique));

		return EFMSimilarity;
	}

	/**
	 * compute similarity score between statement stmt1 from model m1 and statement stmt2 from model m2 at level level
	 * @param stmt1
	 * @param m1
	 * @param stmt2
	 * @param m2
	 * @param level
	 * @return
	 */
	private double pvSimilarity(Statement stmt1, Model m1, Statement stmt2, Model m2, int level) {

		String p1 = stmt1.getPredicate().toString();
		String p2 = stmt2.getPredicate().toString();
		if (!p1.equals(p2) && !isSameProperty(p1, p2)) return 0.0;

		RDFNode object1 = stmt1.getObject();
		RDFNode object2 = stmt2.getObject();

		boolean object1IsURL = object1.isResource();
		boolean object2IsURL = object2.isResource();

		if (object1IsURL && object2IsURL) {
			return computeSimilarity(object1.asResource(), m1, object2.asResource(), m2, level-1);
		}

		if ((!object1IsURL) && (!object2IsURL)) {
			return textSim.computeSimilarity(object1, m1, object2, m2, 1);
		}

		if (object1IsURL) {
			String object1str = Utils.getDescriptionString(object1.toString(), m1);
			return textSim.computeSimilarity(m1.createLiteral(object1str), m1, object2, m2, 1); // jaccard similarity doesn't consider depth (level)
		}
		if (object2IsURL) {
			String object2str = Utils.getDescriptionString(object2.toString(), m2);
			return textSim.computeSimilarity(object1, m1, m2.createLiteral(object2str), m2, 1);
		}
		System.err.println("should never reach here - EntityFeatureModelSimilarity.pvSimilarity()");
		return 0.0; // should never reach here

	}

	public void setWeights(Map<Property, Double> weightVector) {
		// TODO Auto-generated method stub

	}

	public Hashtable<String, Double> getFeatureSim(String object1,
			String object2, int level) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSameProperty(String p1, String p2) {
		if (p1.equals("http://www.w3.org/2004/02/skos/core#definition")
				&& p2.equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			return true;

		if (p1.equals("http://www.w3.org/2004/02/skos/core#prefLabel")
				&& p2.equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			return true;

		if (p1.equals("http://www.w3.org/2004/02/skos/core#prefLabel")
				&& p2.equals("http://www.w3.org/2000/01/rdf-schema#label"))
			return true;

		if (p1.equals("http://www.w3.org/2004/02/skos/core#definition")
				&& p2.equals("http://www.w3.org/2000/01/rdf-schema#label"))
			return true;

		if (p2.equals("http://www.w3.org/2004/02/skos/core#definition")
				&& p1.equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			return true;

		if (p2.equals("http://www.w3.org/2004/02/skos/core#prefLabel")
				&& p1.equals("http://www.w3.org/2000/01/rdf-schema#comment"))
			return true;

		if (p2.equals("http://www.w3.org/2004/02/skos/core#prefLabel")
				&& p1.equals("http://www.w3.org/2000/01/rdf-schema#label"))
			return true;

		if (p2.equals("http://www.w3.org/2004/02/skos/core#definition")
				&& p1.equals("http://www.w3.org/2000/01/rdf-schema#label"))
			return true;
		
		return false;

	}

	public String explainSimilarity(RDFNode r1, Model m1, RDFNode r2, Model m2,
			int level, double score) {
		if (level == 0 || !r1.isResource() || !r2.isResource())
			return textSim.explainSimilarity(r1, m1, r2, m2, level, score);

		List<Statement> topStmt1 = Utils.getDescriptions(m1, r1.asResource(), new ArrayList<Statement>());
		List<Statement> topStmt2 = Utils.getDescriptions(m2, r2.asResource(), new ArrayList<Statement>());

		if (topStmt1.isEmpty() || topStmt2.isEmpty())
			return textSim.explainSimilarity(r1, m1, r2, m2, level, score);

		StringBuilder ret = new StringBuilder();
		HashSet<Statement> matchedStmt2 = new HashSet<Statement>();

		// System.out.println(object1 +" "+ object2);
		double pvSimSum = 0.0;
		double object2_common = 0.0;
		for (Statement stmt1 : topStmt1) {
			double maxPVSim = 0.0;
			Statement currentProcessedStmt2 = null;
			for (Statement stmt2 : topStmt2) {
				double pvSim = pvSimilarity(stmt1, m1, stmt2, m2, level);
				if (pvSim > maxPVSim) {
					maxPVSim = pvSim;
					currentProcessedStmt2 = stmt2;
				}
			}
			if (maxPVSim != 0.0) {

				if (!matchedStmt2.contains(currentProcessedStmt2)) {
					matchedStmt2.add(currentProcessedStmt2);
					object2_common += maxPVSim;
				}
				pvSimSum += maxPVSim;
				ret.append(stmt1.toString() + " vs. " + currentProcessedStmt2.toString() + " : " + maxPVSim + "&#13;");
			}
		}
		ret.append("sum (sum1) : " + pvSimSum + "&#13;");
		ret.append("sum on " + r2.toString() + " (sum2) : " + object2_common + "&#13;");
		ret.append(r1.toString() + " statement size (size1) : " + topStmt1.size() + "&#13;");
		ret.append(r2.toString() + " statement size (size2) : " + topStmt2.size() + "&#13;");
		ret.append("alpha : " + alpha + "&#13;");
		ret.append("beta : " + beta + "&#13;");
		double object1_unique = topStmt1.size() - pvSimSum;
		double object2_unique = topStmt2.size() - object2_common;
		double EFMSimilarity = pvSimSum
				/ (pvSimSum + alpha * (object1_unique) + beta
						* (object2_unique));
		ret.append("sum1/(sum1+alpha*(size1-sum1)+beta*(size2-sum2)) = " + EFMSimilarity);
		return ret.toString().replace("\"", "");
	}
}
