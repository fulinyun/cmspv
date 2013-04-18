
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import Similarity.WeightedFeatureModelSimilarity;

public class SemSIM {

	public static void main(String[] args) {

		String mappingFile = null;
		String indexFile = null;
		String instanceFile = null;
		String sampleData = null;
		String referenceData = null;
		String selection = null;
		String outputfile = null;
		String featurelist = null;
		String weightFile = null;
		int numIteration = 1;
		int numInstance = -1;
		int startindex = -1;
		int endindex = 10000;
		double threshold = 0.9;
		boolean learnOnly = false;

		String usage = "usage: SemSIM.java -m mappingfile -i index -e list-of-entities startindex endindex "
				+ "-w sampledata referencedata featurelist threshold -s selection -o outputfile -lonly numIteration "
				+ "-wf weightFile";
		try {
			for (int i = 0; i < args.length; ++i) {
				if (args[i].equals("-m")) {
					++i;
					mappingFile = args[i];
				} else if (args[i].equals("-i")) {
					++i;
					indexFile = args[i];
				} else if (args[i].equals("-e")) {
					++i;
					instanceFile = args[i];
					if (args[i + 1].charAt(0) == '-') {
						continue;
					}
					++i;
					startindex = Integer.parseInt(args[i]);
					++i;
					endindex = Integer.parseInt(args[i]);
				} else if (args[i].equals("-w")) {
					++i;
					sampleData = args[i];
					++i;
					referenceData = args[i];
					++i;
					featurelist = args[i];
					++i;
					threshold = Double.parseDouble(args[i]);
				} else if (args[i].equals("-i")) {
					++i;
					selection = args[i];
				} else if (args[i].equals("-o")) {
					++i;
					outputfile = args[i];
				} else if (args[i].equals("-lonly")) {
					++i;
					numIteration = Integer.parseInt(args[i]);
					learnOnly = true;
				} else if (args[i].equals("-wf")) {
					++i;
					weightFile = args[i];
				}
			}
		} catch (Exception e) {
			System.err.println(usage);
			return;
		}

		if (mappingFile == null || indexFile == null || outputfile == null) {
			System.err.println(usage);
			return;
		}

		if (!learnOnly && instanceFile == null) {
			System.err.println(usage);
			return;
		}

		Hashtable<String, Double> weightVector = null;
		if (weightFile != null) {
			weightVector = readWeight(weightFile);
		} else if (sampleData != null && featurelist != null
				&& referenceData != null) {
			weightVector = learnWeight(mappingFile, indexFile, sampleData,
					featurelist, referenceData, numIteration, threshold);
		}

		System.out.println("Processing...");
		if (learnOnly) {
			try {
				printToWeightVectorFile(outputfile, weightVector);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		try {
			DirectoryReader reader = DirectoryReader.open(FSDirectory
					.open(new File(indexFile)));
			findMatch(reader, mappingFile, indexFile, "", outputfile,
					instanceFile, startindex, endindex, weightVector);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Hashtable<String, Double> learnWeight(String mappingfile,
			String indexFile, String sampledata, String featurefile,
			String referenceFile, int numIteration, double threshold) {

		FeatureWeightLearning FWL = new FeatureWeightLearning();
		FWL.initialize(mappingfile, indexFile, sampledata, featurefile,
				referenceFile);
		Hashtable<String, String> SampleData = new Hashtable<String, String>();
		SampleData = FWL.getMatchingDocs(sampledata, "entity1", "entity2");
		ArrayList<String> features = FWL.getFeatures();
		FWL.initializeWeightVector(features);
		FWL.initReferenceModel();
		DirectoryReader reader = null;
		try {
			reader = DirectoryReader
					.open(FSDirectory.open(new File(indexFile)));
			for (int i = 0; i < numIteration; i++) {
				Enumeration<String> keys = SampleData.keys();
				while (keys.hasMoreElements()) {
					String entity1 = keys.nextElement();
					// System.out.println(entity);
					FWL.adaptWeight(reader, entity1, SampleData.get(entity1),
							threshold);

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Hashtable<String, Double> weightVector = FWL.getWeightVector();

		return weightVector;
	}

	public static Hashtable<String, Double> readWeight(String weightFile) {

		Hashtable<String, Double> weights = new Hashtable<String, Double>();

		try {
			FileInputStream fstream = new FileInputStream(weightFile);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null) {
				String weight[] = strLine.split(" ");
				weights.put(weight[0], Double.parseDouble(weight[1]));
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return weights;
	}

	public static void printToWeightVectorFile(String outputfile,
			Hashtable<String, Double> weightVector) throws Exception {
		FileWriter fstream = new FileWriter(outputfile);
		BufferedWriter out = new BufferedWriter(fstream);
		Enumeration<String> keys = weightVector.keys();
		while (keys.hasMoreElements()) {
			String entity1 = keys.nextElement();
			out.write(entity1 + " " + weightVector.get(entity1) + "\n");
		}
		out.flush();
		out.close();
	}

	public static void findMatch(IndexReader reader, String map, String index,
			String directory, String resultfile, String instanceData,
			int startindex, int endindex, Hashtable<String, Double> weightVector) {
		InstanceMatching im = new InstanceMatching();
		im.initMapAndIndex(map, index, directory, resultfile);
		int[] docs = new int[reader.numDocs()];
		for (int i = 0; i < reader.numDocs(); i++) {
			docs[i] = i;
		}

		ArrayList<String> matchDocs1 = getMatchingDocs(instanceData, "entity1",
				reader, startindex, endindex);
		ArrayList<String> matchDocs2 = getMatchingDocs(instanceData, "entity2",
				reader, startindex, endindex);

		try {
			im.findMatch(reader, docs, matchDocs1, matchDocs2, weightVector);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getMatchingDocs(String filename,
			String entityType, IndexReader reader, int startindex, int endindex) {
		ArrayList<String> entities = new ArrayList<String>();
		// System.out.println(filename);
		try {

			FileInputStream fstream = new FileInputStream(filename);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int currentindex = 1;
			while ((strLine = br.readLine()) != null
					&& currentindex <= endindex) {

				if (strLine.contains(entityType)) {

					if (currentindex >= startindex) {
						String entity = "";
						if (strLine.contains("\"")) {
							entity = "<"
									+ strLine.substring(
											strLine.indexOf('"') + 1,
											strLine.lastIndexOf('"')) + ">";
						}
						if (strLine.contains("'")) {
							entity = "<"
									+ strLine.substring(
											strLine.indexOf('\'') + 1,
											strLine.lastIndexOf('\'')) + ">";
						}
						// for(int i =0 ; i < reader.numDocs(); i++){
						// if(reader.document(i).get("URI").equals(entity)){
						// System.out.println(docs[i]+" in: "+reader.document(docs[i]).get("URI")+" "+reader.document(i).get("type"));
						// break;
						// }
						// }
						entities.add(URLDecoder.decode(entity, "UTF-8"));
						System.out.println(URLDecoder.decode(entity, "UTF-8"));
					}
					currentindex++;
					// count = entities.size();
					// count++;
				}
			}

			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return entities;
	}

}
