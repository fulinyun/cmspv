package Similarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class JaccardSimilarity implements Similarity {
	int highlevel = 2;
	ArrayList<String> stopWords;
	ArrayList<String> garbagePattern;

	public JaccardSimilarity() {
		stopWords = new ArrayList<String>(Arrays.asList((
		// "able," +
		// "about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,"
		// +
		// "been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,"
		// +
		// "get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,"
		// +
		// "least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,"
		// +
		// "only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,"
		// +
		// "their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,"
		// +
		// "when,where,which,while,who,whom,why,will,with,would,yet,you,your," +
				"a,b,c,d,e,g,h,i,j,k,l,n,o,p,q,r,s,t,u,v,w,x,y,z").split(",")));
		garbagePattern = new ArrayList<String>(
				Arrays.asList(("aa,bb,cc,dd,ee,ff,gg,hh,ii,jj,kk,oo,pp,qq,rr,uu,vv,ww,"
						+ "xx,yy,zz").split(",")));

	}

	public double computeSimilarity(RDFNode r1, Model m1, RDFNode r2, Model m2, int level) {
		String concept1 = r1.toString().toLowerCase();
		String concept2 = r2.toString().toLowerCase();

		concept1 = concept1.replaceAll(
				"\\^\\^http://www.w3.org/2001/xmlschema#.*", "").replaceAll(
				"http:/.*?#|/", "");
		// .replaceAll("item", "")
		// .replaceAll("[^a-z0-9\\s\\-\\.]+"," ")
		// .replaceAll(" \\.", "");
		String[] org_words1 = concept1.replaceAll("[^a-z\\s]+", "").split(
				"\\s+");

		concept2 = concept2.replaceAll(
				"\\^\\^http://www.w3.org/2001/xmlschema#.*", "").replaceAll(
				"http:/.*?#|/", "");
		// .replaceAll("item", "")
		// .replaceAll("[^a-z0-9\\s]+"," ")
		// .replaceAll(" \\.", "");
		String[] org_words2 = concept2.replaceAll("[^a-z\\s]+", "").split(
				"\\s+");

		if (StringCompare(concept1, concept2) > 0)
			return 1;

		// ArrayList<String> words1 = removeMeaningless(org_words1);
		// ArrayList<String> words2 = removeMeaningless(org_words2);
		ArrayList<String> words1 = new ArrayList<String>(
				Arrays.asList(org_words1));
		ArrayList<String> words2 = new ArrayList<String>(
				Arrays.asList(org_words2));

		double commons = 0;
		for (int i = 0; i < words1.size(); i++) {
			for (int j = 0; j < words2.size(); j++) {
				if ((words1.get(i).equals(words2.get(j)))) {// ||
															// specialMatch(words1.get(i),words2.get(j))==1)){
					words2.set(j, " ");
					commons = commons + 2;
					break;
				}
			}

		}

		return commons / ((double) (words1.size() + words2.size()));

	}

	public void setLevel(int level) {
		highlevel = level;
	}

	private double StringCompare(String str1, String str2) {

		if (isNumeric(str1) && isNumeric(str2)) {
			return numberCompare(Double.parseDouble(str1),
					Double.parseDouble(str2));
		}

		// double sm = specialMatch(str1,str2);
		// if(sm > 0)
		// return sm;

		if (str1.equals(str2) || StringDistance.getSim(str1, str2) > 0)
			return 1.0;

		return 0.0;

	}

	private double specialMatch(String v1, String v2) {

		// gender match
		if ((v1.equals("f") || v1.equals("m"))
				&& (v1.equals("" + v2.charAt(0)) && (v2.equals("male") || v2
						.equals("female")))) {
			return 1;
		} else if ((v2.equals("f") || v2.equals("m"))
				&& (v2.equals("" + v1.charAt(0)) && (v1.equals("male") || v1
						.equals("female")))) {
			return 1;
		}
		// if(v1.length()==1 && v1.equals(""+v2.charAt(0))){
		// return 1;
		// }
		// else if(v2.length()==1 && v2.equals(""+v1.charAt(0))){
		// return 1;
		// }
		v1 = v1.replaceAll("Jan.*", "1").replaceAll("Feb.*", "2")
				.replaceAll("Mar.*", "3").replaceAll("Apr.*", "4")
				.replaceAll("May", "5").replaceAll("Jun.*", "6")
				.replaceAll("Jul.*", "7").replaceAll("Aug.*", "8")
				.replaceAll("Sep.*", "9").replaceAll("Oct.*", "10")
				.replaceAll("Nov.*", "11").replaceAll("Dec.*", "12");
		v2 = v2.replaceAll("Jan.*", "1").replaceAll("Feb.*", "2")
				.replaceAll("Mar.*", "3").replaceAll("Apr.*", "4")
				.replaceAll("May", "5").replaceAll("Jun.*", "6")
				.replaceAll("Jul.*", "7").replaceAll("Aug.*", "8")
				.replaceAll("Sep.*", "9").replaceAll("Oct.*", "10")
				.replaceAll("Nov.*", "11").replaceAll("Dec.*", "12");
		return 0;
	}

	private boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private double numberCompare(double num1, double num2) {
		if (Math.abs(num1 - num2) < 1e-3)
			return 1;
		else
			return 0;
	}

	private ArrayList<String> removeMeaningless(String[] str) {

		ArrayList<String> returnString = new ArrayList<String>();
		for (int i = 0; i < str.length; i++) {
			boolean inGarbage = false;
			for (String gstr : garbagePattern) {
				if (str[i].contains(gstr)) {
					inGarbage = true;
					break;
				}
			}

			if (inGarbage)
				continue;

			boolean inStopWord = false;
			for (String substr : stopWords) {
				if (str[i].equals(substr)) {
					inStopWord = true;
					break;
				}
			}
			if (!inStopWord && str[i].length() > 0)
				returnString.add(str[i]);
		}

		return returnString;
	}

	public void setWeights(Map<Property, Double> weightVector) {
		// TODO Auto-generated method stub

	}

//	public Hashtable<String, Double> getFeatureSim(String object1,
//			String object2, int level) {
//
//		return null;
//	}

//	public void setFirstObjectDescription(Object description1) {
//		// TODO Auto-generated method stub
//
//	}

//	public void setSecondObjectDescription(Object description2) {
//		// TODO Auto-generated method stub
//
//	}

}
