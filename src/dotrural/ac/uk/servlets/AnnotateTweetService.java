package dotrural.ac.uk.servlets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import dotrural.ac.uk.utils.JenaUtils;
import dotrural.ac.uk.utils.KimUtils;
import dotrural.ac.uk.utils.SparqlUtils;
import dotrural.ac.uk.utils.Utils;

public class AnnotateTweetService extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OntModel domainModel;
	private Utils utils; 
	
	public AnnotateTweetService () {
		super();
		
		this.utils = new Utils();
		try {
			JenaUtils.initialiseDomainModel(domainModel,utils);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("text/html");//setting the content type  
		PrintWriter pw=res.getWriter();//get the stream to write the data  
		  
		
		//writing html in the stream  
		pw.println("<html><body>");  
		pw.println("this servlet accepts post parameter uri that represents the resource uri of a tweet described using bottari ontology (https://raw.githubusercontent.com/SocialJourneys/SocialJourneysOntologies/master/bottari.n3)."); 
		pw.println("<br>"); 
		pw.println("<br>");  
		pw.println("For testing only..."); 
		pw.println("<br>"); 
		pw.println("<br>");  
		pw.println("Enter the uri of the tweet want to process"); 
		pw.println("<br>"); 
		pw.println("<br>");  
		pw.println("<form action='/kim/annotate' method='post'>"); 
		pw.println("<b>Tweet URI : </b><input name='uri' size='100' value='http://sj.abdn.ac.uk/ozStudyD2R/resource/ozstudy/twitter/tweet/596272738580484098'></input>"); 
		pw.println("<br>"); 
		pw.println("<b>SPARQL endpoint URI : </b><input name='sparqEndpoint' size='40' value='http://sj.abdn.ac.uk/ozStudyD2R/sparql'></input>"); 
		pw.println("<br>"); 
		pw.println(" <input type='checkbox' name='includeInference' > Show Inferences<br>");
		pw.println("<input type='submit' value='Submit'>"); 
		pw.println("</form>"); 
		pw.println("</body></html>");  
		
		pw.close();//closing the stream  
	}
	
	
	
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("text/rdf+n3");//setting the content type  
		PrintWriter pw=res.getWriter();//get the stream to write the data  
		String uri = null;
		String sparqlEndPointUrl = null;
		
		if  ( (request.getParameter("uri")!= null)&&(request.getParameter("sparqEndpoint")!= null)) {
			uri = request.getParameter("uri");
			sparqlEndPointUrl = request.getParameter("sparqEndpoint");
			
			OntModel ontologyModel = ModelFactory
					.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
			
			//pw.println("initial instance model size: " + ontologyModel.size());
			ontologyModel =  SparqlUtils.getSingleTweetBottariData(uri,sparqlEndPointUrl,ontologyModel);
			//pw.println("after tweet data added instance model size: " + ontologyModel.size());
			String messageToAnnotate =  SparqlUtils.getMessageBodyOfTheTweetByURI (uri,sparqlEndPointUrl);
			if (messageToAnnotate != null) {
				
			
			ontologyModel =  JenaUtils.addAnnotationsToModel(ontologyModel, KimUtils.annotateText(messageToAnnotate), uri,messageToAnnotate);
			// pw.println("after annotations added instance model size: " + ontologyModel.size());
			//if  ( (request.getParameter("includeInference")!= null)) {
				
			if  ( request.getParameter("includeInference")!= null) {
					
		         
				JenaUtils  jenaUtils = new  JenaUtils ();    
			    jenaUtils.performSPINinferences(ontologyModel,domainModel ).write(pw);
			}
					
			else {
				ontologyModel.write(pw);
			}
			}
			       
		}
		
		pw.close();//closing the stream  
		
	}
	
	
	
}
