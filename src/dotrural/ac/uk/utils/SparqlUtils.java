package dotrural.ac.uk.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

public class SparqlUtils {
	
	public static String sendPost( String sparqlEndPointUrl,String sparqlQuery ){
		String resultString;
		try{

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(sparqlEndPointUrl);
		 
		// add header
		post.setHeader("Accept","application/json");

		//System.out.println("Content Length : " + params.length());
		List urlParameters = new ArrayList();
		urlParameters.add(new BasicNameValuePair("query", sparqlQuery));
		urlParameters.add(new BasicNameValuePair("output", "json"));

		post.setEntity(new UrlEncodedFormEntity(urlParameters));
		//post.setParams(params);

		HttpResponse response = client.execute(post);

		System.out.println("SPARQL Request Response Code : " + response.getStatusLine().getStatusCode());
		 
		BufferedReader rd = new BufferedReader(
		                        new InputStreamReader(response.getEntity().getContent()));
		 
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
		result.append(line);
		}
		 
	    resultString = result.toString();
		}
		catch(Exception e){
		resultString = e.getMessage();
		}
		return resultString;

		}
	
	public static OntModel getSingleTweetBottariData (String uri, String sparqlEndPointUrl, OntModel modelToWriteResults) {
		
		
		
		String prefix = "PREFIX bottari: <http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#> ";
				
				
		String sparqlQuery = prefix + "SELECT ?message ?sender ?timestamp "
				+ "WHERE {"
				+ "<"+uri+">"+" bottari:message ?message."
				+ "<"+uri+">"+" bottari:messageTimeStamp ?timestamp."
				+ " ?sender bottari:posts <"+uri +">.}";
		
		
		String resultJSON = SparqlUtils.sendPost(sparqlEndPointUrl, sparqlQuery);
		
		JSONObject jsonObject = null;
		JSONArray jsonArray = null;
		JSONObject timestampObject = null;
		JSONObject senderObject = null;
		JSONObject messageObject = null;
		
		
		
		try {
			jsonObject = new JSONObject (resultJSON);
			
			jsonObject = (JSONObject) jsonObject.get("results");
			jsonArray = (JSONArray) jsonObject.get("bindings");
			
			timestampObject = (JSONObject) jsonArray.getJSONObject(0).get("timestamp");
			senderObject = (JSONObject) jsonArray.getJSONObject(0).get("sender");
			messageObject = (JSONObject) jsonArray.getJSONObject(0).get("message");
			
			
			
			//add tweet instance 
			
			
			Individual 	tweetIndividual = modelToWriteResults.createIndividual(uri,modelToWriteResults.createClass("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#Tweet"));
			
			
			Literal message = modelToWriteResults.createTypedLiteral((String) messageObject.get("value"));
			tweetIndividual.setPropertyValue(modelToWriteResults.createProperty("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#message"), message);
			
			Literal timestamp =  modelToWriteResults.createTypedLiteral((String) timestampObject.get("value"),XSDDatatype.XSDdateTime) ;
			tweetIndividual.setPropertyValue(modelToWriteResults.createProperty("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#messageTimeStamp"),timestamp);
			
			//modelToWriteResults.add (tweetIndividual,RDF.type, );
			
			//add sender instance 
		
			
			Individual 	senderIndividual = modelToWriteResults.createIndividual((String) senderObject.get("value"),modelToWriteResults.createClass("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#TwitterUser"));
	
			//add tweet message value		
						
			modelToWriteResults.addLiteral (tweetIndividual,modelToWriteResults.createProperty ("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#message"),message);
			
			//add sender's relationship to tweet 
					
			modelToWriteResults.add (senderIndividual,modelToWriteResults.createProperty("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#posts"),tweetIndividual);

			//add twitter timestamp 

			modelToWriteResults.addLiteral (tweetIndividual,modelToWriteResults.createProperty("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#messageTimeStamp"),timestamp);
						
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return modelToWriteResults; 
	}

	public static OntModel getSingleDirectMessageBottariData (String uri, String sparqlEndPointUrl, OntModel modelToWriteResults) {
		
		
		
		String prefix = "PREFIX bottari: <http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#> ";
				
				
		String sparqlQuery = prefix + "SELECT ?message ?sender ?timestamp "
				+ "WHERE {"
				+ "<"+uri+">"+" bottari:message ?message."
				+ "<"+uri+">"+" bottari:messageTimeStamp ?timestamp."
				+ " ?sender bottari:posts <"+uri +">.}";
		
		
		String resultJSON = SparqlUtils.sendPost(sparqlEndPointUrl, sparqlQuery);
		
		JSONObject jsonObject = null;
		JSONArray jsonArray = null;
		JSONObject timestampObject = null;
		JSONObject senderObject = null;
		JSONObject messageObject = null;
		
		
		
		try {
			jsonObject = new JSONObject (resultJSON);
			
			jsonObject = (JSONObject) jsonObject.get("results");
			jsonArray = (JSONArray) jsonObject.get("bindings");
			
			timestampObject = (JSONObject) jsonArray.getJSONObject(0).get("timestamp");
			senderObject = (JSONObject) jsonArray.getJSONObject(0).get("sender");
			messageObject = (JSONObject) jsonArray.getJSONObject(0).get("message");
			
			
			
			//add tweet instance 
			
			
			Individual 	tweetIndividual = modelToWriteResults.createIndividual(uri,modelToWriteResults.createClass("http://www.dotrural.ac.uk/irp/uploads/ontologies/vocab#DirectMessage"));
			
			
			Literal message = modelToWriteResults.createTypedLiteral((String) messageObject.get("value"));
			tweetIndividual.setPropertyValue(modelToWriteResults.createProperty("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#message"), message);
			
			Literal timestamp =  modelToWriteResults.createTypedLiteral((String) timestampObject.get("value"),XSDDatatype.XSDdateTime) ;
			tweetIndividual.setPropertyValue(modelToWriteResults.createProperty("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#messageTimeStamp"),timestamp);
			
			//modelToWriteResults.add (tweetIndividual,RDF.type, );
			
			//add sender instance 
		
			
			Individual 	senderIndividual = modelToWriteResults.createIndividual((String) senderObject.get("value"),modelToWriteResults.createClass("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#TwitterUser"));
	
			//add tweet message value		
						
			modelToWriteResults.addLiteral (tweetIndividual,modelToWriteResults.createProperty ("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#message"),message);
			
			//add sender's relationship to tweet 
					
			modelToWriteResults.add (senderIndividual,modelToWriteResults.createProperty("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#posts"),tweetIndividual);

			//add twitter timestamp 

			modelToWriteResults.addLiteral (tweetIndividual,modelToWriteResults.createProperty("http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#messageTimeStamp"),timestamp);
						
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return modelToWriteResults; 
	}
	
	public static String getMessageBodyOfTheTweetByURI (String utweetUriri, String sparqlEndPointUrl) {
		
		String message = "error";
		
		String prefix = "PREFIX bottari: <http://www.dotrural.ac.uk/irp/uploads/ontologies/bottari#> ";
		
		
		String sparqlQuery = prefix + "SELECT ?message ?sender ?timestamp "
				+ "WHERE {"
				+ "<"+utweetUriri+">"+" bottari:message ?message."
				+ "<"+utweetUriri+">"+" bottari:messageTimeStamp ?timestamp."
				+ " ?sender bottari:posts <"+utweetUriri +">.}";
		
		
		String resultJSON = SparqlUtils.sendPost(sparqlEndPointUrl, sparqlQuery);
		
		JSONObject jsonObject = null;
		JSONArray jsonArray = null;
		JSONObject messageObject = null;

		try {
			jsonObject = new JSONObject (resultJSON);
			
			jsonObject = (JSONObject) jsonObject.get("results");
			jsonArray = (JSONArray) jsonObject.get("bindings");
			
			messageObject = (JSONObject) jsonArray.getJSONObject(0).get("message");
		
			message = (String) messageObject.get("value");
					
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return message;
	}
	
	
}
