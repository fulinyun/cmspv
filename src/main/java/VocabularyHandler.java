import java.io.IOException;
import java.io.OutputStream;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


@SuppressWarnings("restriction")
public class VocabularyHandler implements HttpHandler {

	public void handle(HttpExchange arg0) throws IOException {
		String ontology_service = "http://localhost:3030/ds/query";
		String endpointsSparql = "select ?s ?p ?o where {?s ?p ?o .}";

		QueryExecution x = QueryExecutionFactory.sparqlService(
				ontology_service, endpointsSparql);
		ResultSet results = x.execSelect();
		
		arg0.getResponseHeaders().add("Content-type", "text/html");
		arg0.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		String result = "<html><head><title>results</title>" +
				"<style> h1 {font-size: 150%;} " +
				"table {text-align: left;border: 1px;border-collapse: collapse;}" +
				"table th {border: 1px solid rgb(37, 64, 143);" +
				"background-color: rgb(31, 126, 192);" +
				"color: rgb(255, 255, 255);" +
				"font-weight: normal;}" +
				"table td {border: 1px solid rgb(31, 126, 192);" +
				"background-color: rgb(255, 255, 255);}" +
				"li {margin-top: 0.5em;}</style>" +
				"</head><body><table>" +
				"<thead><tr><th>s</th><th>p</th><th>o</th></tr></thead><tbody>";
		while (results.hasNext()) {
			result += "<tr>";
			QuerySolution qs = results.next();
			result += "<td>"+qs.get("s").toString()+"</td>"
			+"<td>"+qs.get("p").toString()+"</td>"
			+"<td>"+qs.get("o").toString()+"</td>";
			result += "</tr>";
		}
		result += "</tbody></table></body></html>";
		arg0.sendResponseHeaders(200, result.length());
		OutputStream m = arg0.getResponseBody();
		m.write(result.getBytes("UTF-8"));
		arg0.getResponseBody().flush();
		arg0.getResponseBody().close();
		x.close();
		

//		String uri = arg0.getRequestURI().toString();
//		/* e.g.
//		 * input "http://localhost:14490/cmspv/hello/world" in the address bar, 
//		 * uri = "/cmspv/hello/world"
//		 */
//		arg0.getResponseHeaders().add("Content-type", "text/html");
//		arg0.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
//		String result = "<html><head><title>results</title>" +
//				"<style> h1 {font-size: 150%;} " +
//				"table {text-align: left;border: 1px;border-collapse: collapse;}" +
//				"table th {border: 1px solid rgb(37, 64, 143);" +
//				"background-color: rgb(31, 126, 192);" +
//				"color: rgb(255, 255, 255);" +
//				"font-weight: normal;}" +
//				"table td {border: 1px solid rgb(31, 126, 192);" +
//				"background-color: rgb(255, 255, 255);}" +
//				"li {margin-top: 0.5em;}</style>" +
//				"</head><body><table>" +
//				"<thead><tr><th>URI</th><th>labels</th></tr></thead><tbody>";
//		if (uri.endsWith("/")) uri = uri.substring(0, uri.length()-1);
//		if (uri.equalsIgnoreCase("/cmspv/concepts")) {
//			ExtendedIterator<Individual> it = VocabularyServer.model.listIndividuals(
//					VocabularyServer.model.createClass(
//							"http://www.w3.org/2004/02/skos/core#Concept"));
//			while (it.hasNext()) {
//				Individual i = it.next();
//				result += "<tr><td>"+i.getURI()+"</td><td>";
//				NodeIterator nit = VocabularyServer.model.
//						listObjectsOfProperty(i, RDFS.label);
//				boolean first = true;
//				while (nit.hasNext()) {
//					RDFNode node = nit.next();
//					if (node.isLiteral()) {
//						if (!first) result += "<br />";
//						else first = false;
//						result += node.asLiteral().getString();
//					}
//				}
//				result += "</td></tr>";
//			}
//		}
//		result += "</tbody></table></body></html>";
//		arg0.sendResponseHeaders(200, result.length());
//		OutputStream m = arg0.getResponseBody();
//		m.write(result.getBytes("UTF-8"));
//		arg0.getResponseBody().flush();
//		arg0.getResponseBody().close();

	}

}
