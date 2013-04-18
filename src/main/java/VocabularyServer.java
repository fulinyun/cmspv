import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class VocabularyServer {

//	public static void main(String[] args) throws Exception {
//		String ontology_service = "http://localhost:3030/ds/query";
//		String endpointsSparql = "select ?s ?p ?o where {?s ?p ?o .}";
//
//		QueryExecution x = QueryExecutionFactory.sparqlService(
//				ontology_service, endpointsSparql);
//		ResultSet results = x.execSelect();
//		ResultSetFormatter.out(System.out, results);
//		x.close();
//
//	}
//	public static OntModel model;
	
	public static void main(String[] args) throws Exception {

//		model = ModelFactory.createOntologyModel(
//				new OntModelSpec(null, null, null, ProfileRegistry.OWL_LANG));
//		OntDocumentManager.getInstance().setProcessImports(false);
//		model.read(new FileInputStream(args[0]), null, "TTL");
		System.out.println("Starting web service...");
		HttpServer server=null;
		Executor exec = Executors.newFixedThreadPool(30);
		try {
			server = HttpServer.create(new InetSocketAddress(14490), 0);
		}
		catch(IOException e) {
			e.printStackTrace();
			return;
		}
		server.setExecutor(exec);
		server.createContext("/cmspv").setHandler(new VocabularyHandler());
		server.start();
		System.out.println("Web service started...");
		
	}

}
